package com.financetracker.core.ui.components.piechart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Ui component responsible for drawing Pie chart
 * @param data - data to be presented as a pie chart. IMPORTANT - amount values should sum up to 100
 * @param label - main label to show in the middle of the pie chart
 * @param secondaryLabel - secondary label to show in the middle of the pie chart, below main one
 * @param attributes - various attributes to customize pie chart behavior and appearance
 */
@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    data: List<PieChartEntry>,
    label: String,
    secondaryLabel: String? = null,
    attributes: PieChartAttributes = PieChartAttributes()
) {

    val calculatedEntries = calculateAnimatableValues(data)

    if (attributes.animationParameters.isEnabled) {
        LaunchedEffect(Unit) {
            runAnimations(
                calculatedEntries = calculatedEntries,
                durationMillis = attributes.animationParameters.durationMillis
            )
        }
    }

    val textMeasurer = rememberTextMeasurer()

    val labelLayoutResult = remember(label) {
        textMeasurer.measure(label, attributes.labelStyle)
    }

    val secondaryLayoutResult = secondaryLabel?.let {
        remember(secondaryLabel) {
            textMeasurer.measure(secondaryLabel, attributes.secondaryStyle)
        }
    }

    Box(
        modifier = modifier
            .background(Color.White)
            .size(300.dp)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
            onDraw = {
                drawArcs(
                    data = calculatedEntries,
                    attributes = attributes
                )

                drawLabels(
                    textMeasurer = textMeasurer,
                    label = label,
                    labelLayoutResult = labelLayoutResult,
                    secondaryLabel = secondaryLabel,
                    secondaryLayoutResult = secondaryLayoutResult,
                    attributes = attributes
                )
            }
        )
    }
}

private fun DrawScope.drawLabels(
    textMeasurer: TextMeasurer,
    label: String,
    labelLayoutResult: TextLayoutResult,
    secondaryLabel: String? = null,
    secondaryLayoutResult: TextLayoutResult? = null,
    attributes: PieChartAttributes
) {
    val isSecondaryPresent = secondaryLabel != null && secondaryLayoutResult != null

    val offset = if (isSecondaryPresent) attributes.betweenLabelsOffset / 2 else 0f

    drawText(
        textMeasurer = textMeasurer,
        text = label,
        style = attributes.labelStyle,
        topLeft = Offset(
            x = center.x - labelLayoutResult.size.width / 2f,
            y = center.y - labelLayoutResult.size.height / 2f - offset
        )
    )

    if (isSecondaryPresent)
        drawText(
            textMeasurer = textMeasurer,
            text = secondaryLabel,
            style = attributes.secondaryStyle,
            topLeft = Offset(
                x = center.x - secondaryLayoutResult.size.width / 2f,
                y = center.y - secondaryLayoutResult.size.height / 2f + offset
            )
        )
}

private fun DrawScope.drawArcs(
    data: List<CalculatedEntry>,
    attributes: PieChartAttributes
) {
    data.forEach {
        drawArc(
            color = it.color,
            startAngle = if (attributes.animationParameters.isEnabled) it.animatedStartAngle.value else it.targetStartAngle,
            sweepAngle = if (attributes.animationParameters.isEnabled) it.animatedSweepAngle.value else it.targetSweepAngle,
            useCenter = false,
            style = Stroke(
                width = attributes.strokeWidth,
                cap = attributes.strokeCap,
                join = attributes.strokeJoin
            )
        )
    }
}

@Composable
private fun calculateAnimatableValues(
    data: List<PieChartEntry>,
): List<CalculatedEntry> {
    var sweepAngle = 0f
    var startAngle = 0f

    val result = mutableListOf<CalculatedEntry>()
    data.forEach {
        sweepAngle = (it.amount / 100) * 360
        result.add(
            CalculatedEntry(
                color = it.color,
                animatedStartAngle = remember { Animatable(0f) },
                animatedSweepAngle = remember { Animatable(0f) },
                targetSweepAngle = sweepAngle,
                targetStartAngle = startAngle
            )
        )
        startAngle += sweepAngle
    }
    return result
}

private fun CoroutineScope.runAnimations(
    calculatedEntries: List<CalculatedEntry>,
    durationMillis: Int
) {
    calculatedEntries.forEach {
        launch {
            it.animatedStartAngle.animateTo(
                targetValue = it.targetStartAngle,
                animationSpec = tween(durationMillis = durationMillis)
            )
        }
        launch {
            it.animatedSweepAngle.animateTo(
                targetValue = it.targetSweepAngle,
                animationSpec = tween(durationMillis = durationMillis)
            )
        }
    }
}

data class PieChartEntry(
    val color: Color,
    val amount: Float
)

private data class CalculatedEntry(
    val color: Color,
    val animatedStartAngle: Animatable<Float, AnimationVector1D>,
    val animatedSweepAngle: Animatable<Float, AnimationVector1D>,
    val targetStartAngle: Float,
    val targetSweepAngle: Float,
)

@Preview
@Composable
private fun PieChartPreview() {
    PieChart(
        label = "SPENT",
        secondaryLabel = "$ 2.345",
        data = listOf(
            PieChartEntry(color = Color.Red, amount = 15f),
            PieChartEntry(color = Color.Yellow, amount = 30f),
            PieChartEntry(color = Color.Blue, amount = 20f),
            PieChartEntry(color = Color.Magenta, amount = 15f),
            PieChartEntry(color = Color.Green, amount = 20f),
        )
    )
}
