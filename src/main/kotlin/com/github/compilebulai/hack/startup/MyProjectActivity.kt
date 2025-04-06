package com.github.compilebulai.hack.startup

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class MyProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        thisLogger().warn("Proiectul s-a încărcat! Puteți inițializa logica suplimentară aici.")
    }
}
