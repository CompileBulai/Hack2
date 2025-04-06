package com.github.compilebulai.hack.actions

import com.github.compilebulai.hack.codeTask.CodeLocation
import com.github.compilebulai.hack.codeTask.CodeTaskAssociator
import com.github.compilebulai.hack.dialogs.SelectTaskDialog
import com.github.compilebulai.hack.state.ToDoListService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile

class AssociateCodeWithTaskActionWrapper : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project? = e.getData(CommonDataKeys.PROJECT)
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val psiFile: PsiFile? = e.getData(CommonDataKeys.PSI_FILE)

        if (project != null && editor != null && psiFile != null) {
            val selectedText = editor.selectionModel.selectedText
            if (selectedText == null) {
                Messages.showInfoMessage("No text selected.", "Info")
                return
            }
            // Avem text selectat -> cerem utilizatorului să aleagă un task deja existent
            val todoService = project.getService(ToDoListService::class.java)
            if (todoService.getAllTasks().isEmpty()) {
                Messages.showInfoMessage("Nu există niciun task în listă. Mai întâi adaugă unul în ToDoList.", "Info")
                return
            }

            val dialog = SelectTaskDialog(project, todoService)
            if (dialog.showAndGet()) {
                val selectedTask = dialog.getSelectedTaskText()
                if (selectedTask != null) {
                    // Asociem snippet-ul
                    val codeAssociator = project.getService(CodeTaskAssociator::class.java)
                    val codeLocation = CodeLocation(psiFile.virtualFile, editor.selectionModel.selectionStart)
                    codeAssociator.associateCodeWithTask(selectedTask, codeLocation)
                    Messages.showInfoMessage("Code associated with task: $selectedTask", "Success")
                }
            }
        }
    }
}
