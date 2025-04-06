package com.github.compilebulai.hack.actions

import com.github.compilebulai.hack.codeTask.CodeLocation
import com.github.compilebulai.hack.codeTask.CodeTaskAssociator
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile

class AssociateCodeWithTaskAction(private val codeTaskAssociator: CodeTaskAssociator) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val project = e.getData(CommonDataKeys.PROJECT)

        if (editor != null && psiFile != null && project != null) {
            val selectedText = editor.selectionModel.selectedText
            if (selectedText != null) {
                val taskText = Messages.showInputDialog(project, "Enter the task text:", "Associate Code with Task", null)
                if (taskText != null) {
                    val codeLocation = CodeLocation(psiFile.virtualFile, editor.selectionModel.selectionStart)
                    codeTaskAssociator.associateCodeWithTask(taskText, codeLocation)
                    Messages.showInfoMessage("Code associated with task: $taskText", "Success")
                }
            } else {
                Messages.showInfoMessage("No text selected.", "Info")
            }
        }
    }
}
