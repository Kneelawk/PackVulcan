package com.kneelawk.packvulcan.ui.attributor

import androidx.compose.runtime.*
import com.kneelawk.packvulcan.engine.packwiz.PackwizMod
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.io.PrintWriter
import java.io.StringWriter

@Composable
fun rememberAttributorController(modsList: List<PackwizMod>): AttributorInterface {
    val scope = rememberCoroutineScope()

    val textState = remember { mutableStateOf("") }
    val loadingState = remember { mutableStateOf(false) }

    return remember {
        object : AttributorInterface {
            override val attributionText by textState
            override val loading by loadingState

            override fun generateAttributions() {
                scope.launch {
                    supervisorScope {
                        loadingState.value = true
                        textState.value = ""

                        val str = StringWriter()
                        val writer = PrintWriter(str)

                        val simpleModPromises = modsList.map {
                            async {
                                it to it.getSimpleInfo()
                            }
                        }

                        writer.println("${modsList.size} Mods:")

                        val simpleModList = simpleModPromises.awaitAll()
                        for ((mod, simpleMod) in simpleModList) {
                            if (simpleMod == null) {
                                writer.println("* ${mod.displayName}")
                            } else {
                                if (simpleMod.projectUrl == null) {
                                    writer.println("* ${mod.displayName} by ${simpleMod.author}")
                                } else {
                                    writer.println(
                                        "* [${mod.displayName}](${simpleMod.projectUrl}) by ${simpleMod.author}"
                                    )
                                }
                            }
                        }

                        textState.value = str.toString()

                        loadingState.value = false
                    }
                }
            }
        }
    }
}
