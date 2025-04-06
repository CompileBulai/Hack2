package com.github.compilebulai.hack.codeTask

import com.intellij.openapi.vfs.VirtualFile

data class CodeLocation(
    val virtualFile: VirtualFile?,
    val offset: Int
)
