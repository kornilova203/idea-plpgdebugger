/*
 * Copyright (c) 2022. Alexandre Boyer
 */

package net.plpgsql.ideadebugger.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.util.registry.Registry
import net.plpgsql.ideadebugger.console
import net.plpgsql.ideadebugger.run.PlProcess

@Service
class PlProcessWatcherImpl: PlProcessWatcher {

    private var currentProcess: PlProcess? = null

    init {
    }

    override fun isDebugging(): Boolean {
        return currentProcess != null
    }

    override fun processStarted(process: PlProcess) {
        console("Watcher: started")
        currentProcess = process
    }

    override fun processFinished(process: PlProcess) {
        console("Watcher: finished")
        currentProcess = null
    }
}