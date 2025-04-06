package com.github.compilebulai.hack

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class SayHelloAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showMessageDialog(
            "Salut din pluginul tău!",
            "Hello",
            Messages.getInformationIcon()
        )
    }
}
