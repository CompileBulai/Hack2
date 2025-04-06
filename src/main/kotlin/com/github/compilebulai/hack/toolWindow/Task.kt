package com.github.compilebulai.hack.state

import com.github.compilebulai.hack.codeTask.SerializableCodeLocation

data class Task(
    var text: String = "",
    var completed: Boolean = false,
    var note: String? = null,
    var codeLocation: SerializableCodeLocation? = null
)
