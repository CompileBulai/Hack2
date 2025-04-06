package com.github.compilebulai.hack.state

import com.github.compilebulai.hack.codeTask.SerializableCodeLocation
import com.intellij.openapi.components.*

@State(
    name = "ToDoListState",
    storages = [Storage("ToDoListState.xml")]
)
@Service(Service.Level.PROJECT)
class ToDoListService : PersistentStateComponent<ToDoListState> {

    private var state = ToDoListState()

    fun taskExists(text: String): Boolean {
        return state.tasks.any { it.text.equals(text, ignoreCase = true) }
    }

    override fun getState(): ToDoListState = state

    override fun loadState(state: ToDoListState) {
        this.state = state
    }

    fun addTask(task: Task) {
        state.tasks.add(task)
    }

    fun updateTask(text: String, completed: Boolean) {
        state.tasks.find { it.text == text }?.completed = completed
    }

    fun updateNote(text: String, note: String) {
        state.tasks.find { it.text == text }?.note = note
    }

    fun updateTaskCodeLocation(text: String, codeLocation: SerializableCodeLocation) {
        state.tasks.find { it.text == text }?.codeLocation = codeLocation
    }

    fun getTask(text: String): Task? = state.tasks.find { it.text == text }

    fun getAllTasks(): List<Task> = state.tasks

    fun removeTasksByText(texts: List<String>) {
        state.tasks.removeIf { it.text in texts }
    }
}
