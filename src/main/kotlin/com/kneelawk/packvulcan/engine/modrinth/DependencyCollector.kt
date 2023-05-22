package com.kneelawk.packvulcan.engine.modrinth

import com.kneelawk.packvulcan.model.AcceptableVersions
import com.kneelawk.packvulcan.model.SimpleModFileInfo
import com.kneelawk.packvulcan.model.modrinth.version.result.DependencyJson
import com.kneelawk.packvulcan.model.modrinth.version.result.DependencyTypeJson
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object DependencyCollector {
    suspend fun collectDependencies(
        dependencies: List<DependencyJson>?, acceptableVersions: AcceptableVersions, alreadyInstalled: Set<String>
    ): List<SimpleModFileInfo.Modrinth> {
        if (dependencies.isNullOrEmpty()) return emptyList()

        val collected = ConcurrentHashMap<String, CollectedDependency>()

        collectDependencies(dependencies, acceptableVersions, alreadyInstalled, collected)

        val mods = collected.values.asSequence().map { it.mod }.toMutableList()
        mods.sortBy { it.slug }
        return mods
    }

    private suspend fun collectDependencies(
        dependencies: List<DependencyJson>?, acceptableVersions: AcceptableVersions, alreadyInstalled: Set<String>,
        collected: ConcurrentHashMap<String, CollectedDependency>
    ) {
        if (dependencies.isNullOrEmpty()) return

        coroutineScope {
            for (dep in dependencies) {
                launch {
                    if (dep.dependencyType != DependencyTypeJson.REQUIRED) return@launch

                    val childPav = ModrinthUtils.getProjectAndVersion(dep, acceptableVersions) ?: return@launch
                    val childMod = childPav.toModInfo()
                    val childVersion = DepVersion.fromJson(dep)

                    if (alreadyInstalled.contains(childPav.project.id)) return@launch

                    var collectChildren = false
                    collected.compute(childPav.project.id) { _, existing ->
                        // Note: this does not do any kind of conflict detection. Conflict detection and resolution are
                        // best suited for a dedicated system.
                        if (existing == null || childVersion == DepVersion.SPECIFIC_VERSION) {
                            collectChildren = true
                            CollectedDependency(childMod, childVersion)
                        } else existing
                    }

                    if (!collectChildren) return@launch

                    collectDependencies(childPav.version.dependencies, acceptableVersions, alreadyInstalled, collected)
                }
            }
        }
    }

    private data class CollectedDependency(val mod: SimpleModFileInfo.Modrinth, val version: DepVersion)

    private enum class DepVersion {
        ANY_VERSION,
        SPECIFIC_VERSION;

        companion object {
            fun fromJson(json: DependencyJson): DepVersion {
                return if (json.versionId != null) SPECIFIC_VERSION else ANY_VERSION
            }
        }
    }
}
