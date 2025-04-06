package com.github.compilebulai.hack.codeTask

import com.github.compilebulai.hack.state.ToDoListService
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem

@Service(Service.Level.PROJECT)
class CodeTaskAssociator(private val project: Project) {

    private val toDoListService = project.getService(ToDoListService::class.java)

    fun associateCodeWithTask(taskText: String, codeLocation: CodeLocation) {
        val serializable = SerializableCodeLocation(
            codeLocation.virtualFile?.path ?: "",
            codeLocation.offset
        )
        toDoListService.updateTaskCodeLocation(taskText, serializable)
    }

    fun navigateToCode(taskText: String) {
        val task = toDoListService.getTask(taskText)
        val serialLocation = task?.codeLocation
        if (serialLocation == null || serialLocation.filePath.isBlank()) {
            Messages.showInfoMessage("No code associated with this task.", "Info")
            return
        }

        val virtualFile = LocalFileSystem.getInstance().findFileByPath(serialLocation.filePath)
        if (virtualFile == null) {
            Messages.showInfoMessage("File not found: ${serialLocation.filePath}", "Info")
            return
        }

        val codeLocation = CodeLocation(virtualFile, serialLocation.offset)

        val fileEditorManager = FileEditorManager.getInstance(project)
        fileEditorManager.openFile(virtualFile, true)

        val editor = fileEditorManager.selectedTextEditor
        if (editor != null) {
            editor.caretModel.moveToOffset(codeLocation.offset)
            editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        } else {
            Messages.showInfoMessage("Couldn't find an editor for this file.", "Error")
        }
    }
}
