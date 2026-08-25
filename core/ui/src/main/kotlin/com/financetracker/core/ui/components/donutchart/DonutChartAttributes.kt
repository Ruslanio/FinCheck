package com.financetracker.core.ui.components.donutchart

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp

data class DonutChartAttributes(
    val labelStyle: TextStyle = defaultLabelTextStyle,
    val secondaryStyle: TextStyle = defaultSecondaryTextStyle,
    val animationParameters: AnimationParameters = AnimationParameters(),
    val strokeWidth: Float = DEFAULT_STROKE_WIDTH,
    val strokeCap: StrokeCap = StrokeCap.Butt,
    val strokeJoin: StrokeJoin = StrokeJoin.Round,
    val betweenLabelsOffset: Float = DEFAULT_TEXT_OFFSET
) {
    data class AnimationParameters(
        val isEnabled: Boolean = true,
        val durationMillis: Int = 2000,
    )
}

private const val DEFAULT_STROKE_WIDTH = 100f
private const val DEFAULT_TEXT_OFFSET = 100f

private val defaultLabelTextStyle = TextStyle(
    fontSize = 20.sp,
    color = Color.DarkGray,
    fontStyle = FontStyle.Normal
)

private val defaultSecondaryTextStyle = TextStyle(
    fontSize = 30.sp,
    color = Color.Black,
    fontStyle = FontStyle.Normal
)


