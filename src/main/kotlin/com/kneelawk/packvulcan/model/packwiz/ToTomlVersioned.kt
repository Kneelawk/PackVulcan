package com.kneelawk.packvulcan.model.packwiz

interface ToTomlVersioned {
    fun toToml(packFormat: FormatVersion): Map<String, Any>
}
