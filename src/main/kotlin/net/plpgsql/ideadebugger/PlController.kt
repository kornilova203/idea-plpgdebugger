package net.plpgsql.ideadebugger

import com.intellij.database.connection.throwable.info.WarningInfo
import com.intellij.database.dataSource.DatabaseConnection
import com.intellij.database.dataSource.DatabaseConnectionPoint
import com.intellij.database.datagrid.DataAuditors
import com.intellij.database.datagrid.DataConsumer
import com.intellij.database.datagrid.DataRequest
import com.intellij.database.debugger.SqlDebugController
import com.intellij.database.util.SearchPath
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.sql.psi.SqlExpressionList
import com.intellij.sql.psi.SqlFunctionCallExpression
import com.intellij.sql.psi.SqlIdentifier
import com.intellij.ui.content.Content
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import java.util.regex.Pattern

class PlController(
    val project: Project,
    val connectionPoint: DatabaseConnectionPoint,
    val ownerEx: DataRequest.OwnerEx,
    val virtualFile: VirtualFile?,
    val rangeMarker: RangeMarker?,
    val searchPath: SearchPath?,
    val callExpression: SqlFunctionCallExpression?,
) : SqlDebugController() {

    private val logger = getLogger<PlController>()
    private val pattern = Pattern.compile(".*PLDBGBREAK:([0-9]+).*")
    private lateinit var plProcess: PlProcess
    private lateinit var xSession: XDebugSession
    private val queryAuditor = QueryAuditor()
    private val queryConsumer = QueryConsumer()
    private val windowLister = ToolListener()

    private var entryPoint = 0L
    private var dbgConnection = createDebugConnection(project, connectionPoint)
    private var busConnection = project.messageBus.connect()


    override fun getReady() {
        logger.debug("getReady")
    }

    override fun initLocal(session: XDebugSession): XDebugProcess {
        busConnection.subscribe(ToolWindowManagerListener.TOPIC, windowLister)
        xSession = session
        logger.debug("initLocal")
        entryPoint = searchFunction() ?: 0L
        plProcess = PlProcess(session, dbgConnection, entryPoint)
        return plProcess
    }

    override fun initRemote(connection: DatabaseConnection) {
        logger.info("initRemote")
        val ready = if (entryPoint != 0L) plDebugFunction(connection, entryPoint) == 0 else false

        if (!ready) {
            runInEdt {
                Messages.showMessageDialog(
                    project,
                    "Routine not found",
                    "PL Debugger",
                    Messages.getInformationIcon()
                )
            }
            close()
        } else {
            ownerEx.messageBus.addAuditor(queryAuditor)
        }
    }

    override fun debugBegin() {
        logger.info("debugBegin")
    }

    override fun debugEnd() {
        logger.info("debugEnd")
    }

    override fun close() {
        logger.info("close")
        windowLister.close()
        busConnection.disconnect()
    }


    private fun searchFunction(): Long? {
        val (name, args) = runReadAction {
            val identifier = PsiTreeUtil.findChildOfType(callExpression, SqlIdentifier::class.java)
            val values = PsiTreeUtil.findChildOfType(
                callExpression,
                SqlExpressionList::class.java
            )?.children?.map { it.text.trim() }?.filter { it != "" && it != "," && !it.startsWith("--") }

            Pair(first = identifier?.name ?: "", second = values ?: listOf<String>())
        }
        return searchFunctionByName(connection = dbgConnection, callable = name, callValues = args)
    }

    inner class QueryAuditor : DataAuditors.Adapter() {
        override fun warn(context: DataRequest.Context, info: WarningInfo) {
            if (context.request.owner == ownerEx) {
                val matcher = pattern.matcher(info.message)
                if (matcher.matches()) {
                    val port = matcher.group(1).toInt()
                    plProcess.startDebug(port)
                }
            }
        }
    }

    inner class QueryConsumer : DataConsumer.Adapter() {
        override fun afterLastRowAdded(context: DataRequest.Context, total: Int) {
            if (context.request.owner == ownerEx) {
                close()
            }
        }
    }

    inner class ToolListener : ToolWindowManagerListener {
        var window: ToolWindow? = null
        private var first: Boolean = false
        var acutal: Content? = null
        override fun toolWindowShown(toolWindow: ToolWindow) {
            if (toolWindow.id == "Debug") {
                window = toolWindow
                first = true
            }
            if (first && toolWindow.id != "Debug") {
                window?.show()
                acutal = window?.contentManager?.contents?.find {
                    it.tabName == xSession.sessionName
                }
                first = false
            }
        }

        fun close() {
            windowLister.acutal?.let { window?.contentManager?.removeContent(it, true) }
        }
    }

}


