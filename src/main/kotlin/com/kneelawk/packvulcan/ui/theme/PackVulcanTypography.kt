package com.kneelawk.packvulcan.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


class PackVulcanTypography(
    mdH1: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp),
    mdH2: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
    mdH3: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.72.sp),
    mdH4: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    mdH5: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.28.sp),
    mdH6: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 10.72.sp)
) {
    val mdH1 by mutableStateOf(mdH1)
    val mdH2 by mutableStateOf(mdH2)
    val mdH3 by mutableStateOf(mdH3)
    val mdH4 by mutableStateOf(mdH4)
    val mdH5 by mutableStateOf(mdH5)
    val mdH6 by mutableStateOf(mdH6)
}

val PackVulcanLocalTypography = staticCompositionLocalOf { PackVulcanTypography() }
