package com.github.compilebulai.hack.dialogs

import com.github.compilebulai.hack.state.ToDoListService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class SelectTaskDialog(
    project: Project,
    private val todoService: ToDoListService
) : DialogWrapper(project) {

    private val taskList = JBList<String>()

    init {
        title = "Select Task"
        init() // inițializează DialogWrapper
    }

    override fun createCenterPanel(): JComponent {
        // Obținem toate task-urile din service și le afișăm după text
        val tasks = todoService.getAllTasks().map { it.text }
        taskList.setListData(tasks.toTypedArray())

        return JPanel(BorderLayout()).apply {
            add(JScrollPane(taskList), BorderLayout.CENTER)
        }
    }

    fun getSelectedTaskText(): String? {
        return taskList.selectedValue
    }
}
