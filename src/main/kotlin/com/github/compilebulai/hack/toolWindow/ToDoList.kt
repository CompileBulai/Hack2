package com.github.compilebulai.hack.toolWindow

import com.github.compilebulai.hack.state.ToDoListService
import com.github.compilebulai.hack.state.Task
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import kotlinx.coroutines.runBlocking
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.*
import javax.swing.SwingUtilities.invokeLater

class ToDoList : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val todoService = project.getService(ToDoListService::class.java)

        val gifIcon = ImageIcon(javaClass.getResource("/cat/cat.gif"))
        val scaledWidth = (gifIcon.iconWidth * 0.7).toInt()
        val scaledHeight = (gifIcon.iconHeight * 0.7).toInt()

        val gifLabel = JLabel(gifIcon).apply {
            horizontalAlignment = SwingConstants.CENTER
            preferredSize = Dimension(scaledWidth, scaledHeight)
            maximumSize = Dimension(scaledWidth, scaledHeight)
            minimumSize = Dimension(scaledWidth, scaledHeight)
            alignmentX = Component.CENTER_ALIGNMENT
            isVisible = false
        }

        val mainPanel = JPanel(BorderLayout())
        val tasksPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
        }

        val scrollContainer = JPanel(BorderLayout()).apply {
            add(tasksPanel, BorderLayout.NORTH)
        }

        val scrollPane = JBScrollPane(scrollContainer).apply {
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
            preferredSize = Dimension(300, 250)
        }

        // Optional: Customize progress bar selection colors
        UIManager.put("ProgressBar.selectionForeground", Color.YELLOW)
        UIManager.put("ProgressBar.selectionBackground", Color.WHITE)

        val progressBar = JProgressBar(0, 100).apply {
            isStringPainted = true
            isVisible = false
            foreground = Color(144, 238, 144)
        }

        val removeButton = JButton("Remove Selected Tasks")

        fun updateProgressBar() {
            val totalTasks = tasksPanel.components.count { it is TaskWithNotePanel }
            val completedTasks = tasksPanel.components.count {
                it is TaskWithNotePanel && it.checkBox.isSelected
            }

            val shouldShowProgress = totalTasks > 0
            progressBar.isVisible = shouldShowProgress
            removeButton.isVisible = shouldShowProgress

            if (totalTasks > 0) {
                val progress = (completedTasks.toDouble() / totalTasks * 100).toInt()
                progressBar.value = progress
                gifLabel.isVisible = completedTasks == totalTasks
            } else {
                gifLabel.isVisible = false
            }
        }

        /**
         * Creates a new Task (both locally and in the service),
         * attaches it to the UI, and updates the progress bar.
         */
        fun addNewTask(text: String, note: String = "") {
            val newTask = Task(text = text, completed = false, note = note)
            todoService.addTask(newTask)

            val taskPanel = TaskWithNotePanel(project, newTask).apply {
                checkBox.addActionListener {
                    todoService.updateTask(newTask.text, checkBox.isSelected)
                    updateProgressBar()
                }
                onNoteChanged = { updatedNote ->
                    todoService.updateNote(newTask.text, updatedNote)
                }
            }

            tasksPanel.add(taskPanel)
            tasksPanel.revalidate()
            tasksPanel.repaint()
            updateProgressBar()
        }

        // UI components for manually adding tasks
        val addTaskField = JTextField()
        val addButton = JButton("Add").apply {
            addActionListener {
                val text = addTaskField.text.trim()
                if (text.isNotEmpty()) {
                    if (todoService.taskExists(text)) {
                        JOptionPane.showMessageDialog(
                            null,
                            "A task with this name already exists.",
                            "Duplicate Task",
                            JOptionPane.WARNING_MESSAGE
                        )
                        return@addActionListener
                    }
                    // Here we add a task without an automatically generated note
                    addNewTask(text)
                    addTaskField.text = ""
                }
            }
        }

        // Pressing Enter in the text field triggers the Add button
        addTaskField.addActionListener {
            addButton.doClick()
        }

        // Button that removes tasks whose checkboxes are selected
        removeButton.addActionListener {
            val toRemove = mutableListOf<Component>()
            val taskTextsToRemove = mutableListOf<String>()

            tasksPanel.components.forEach {
                if (it is TaskWithNotePanel && it.checkBox.isSelected) {
                    toRemove.add(it)
                    taskTextsToRemove.add(it.getTaskText())
                }
            }

            toRemove.forEach { tasksPanel.remove(it) }
            todoService.removeTasksByText(taskTextsToRemove)

            tasksPanel.revalidate()
            tasksPanel.repaint()
            updateProgressBar()
        }

        // Reload tasks from the previous session
        todoService.getAllTasks().forEach { savedTask ->
            val taskPanel = TaskWithNotePanel(project, savedTask).apply {
                checkBox.addActionListener {
                    todoService.updateTask(savedTask.text, checkBox.isSelected)
                    updateProgressBar()
                }
                onNoteChanged = { note ->
                    todoService.updateNote(savedTask.text, note)
                }
            }
            tasksPanel.add(taskPanel)
        }

        // If there are no tasks, automatically prompt the user and generate some
        if (todoService.getAllTasks().isEmpty()) {
            ApplicationManager.getApplication().executeOnPooledThread {
                // 1) Prompt user for a project description
                var userPrompt: String? = null
                while (true) {
                    userPrompt = JOptionPane.showInputDialog(
                        null,
                        "What project do you want to implement?",
                        "Automatic Task Generation",
                        JOptionPane.PLAIN_MESSAGE
                    )

                    // User clicked Cancel
                    if (userPrompt == null) {
                        return@executeOnPooledThread
                    }

                    if (userPrompt.isNotBlank()) {
                        break
                    }

                    JOptionPane.showMessageDialog(
                        null,
                        "You must enter a project description!",
                        "Empty Prompt",
                        JOptionPane.ERROR_MESSAGE
                    )
                }

                // 2) Choose task style: short or detailed
                val options = arrayOf("Short", "Detailed")
                val choice = JOptionPane.showOptionDialog(
                    null,
                    "How do you want tasks to be generated?",
                    "Task Style",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
                )
                val shortMode = (choice == 0)

                // 3) Generate tasks
                val finalPrompt = userPrompt!!
                val generatedTasks = runBlocking {
                    TaskGenerator.generateTasks(finalPrompt, shortMode)
                }

                // 4) For each generated task, also generate a descriptive note
                val tasksWithDescriptions = runBlocking {
                    generatedTasks.map { taskTitle ->
                        val note = TaskGenerator.generateDescriptionForTask(taskTitle)
                        taskTitle to note
                    }
                }

                // 5) Update UI & service on the Event Dispatch Thread
                invokeLater {
                    tasksWithDescriptions.forEach { (taskText, note) ->
                        if (!todoService.taskExists(taskText)) {
                            addNewTask(taskText, note)
                        }
                    }
                }
            }
        }

        tasksPanel.add(Box.createRigidArea(Dimension(0, 10)))

        // The top panel with text field & add button
        val inputPanel = JPanel(BorderLayout(5, 0)).apply {
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
            add(addTaskField, BorderLayout.CENTER)
            add(addButton, BorderLayout.EAST)
        }

        // The bottom panel with the progress bar & remove button
        val bottomPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
            add(progressBar, BorderLayout.NORTH)
            add(removeButton, BorderLayout.SOUTH)
        }

        // Add the cat GIF label and arrange everything
        tasksPanel.add(gifLabel)
        mainPanel.add(inputPanel, BorderLayout.NORTH)
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        mainPanel.add(bottomPanel, BorderLayout.SOUTH)
        mainPanel.add(scrollPane, BorderLayout.CENTER)

        updateProgressBar()

        val content = ContentFactory.getInstance().createContent(mainPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
