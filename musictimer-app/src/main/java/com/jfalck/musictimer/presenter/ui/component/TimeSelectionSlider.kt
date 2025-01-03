package com.jfalck.musictimer.presenter.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionSlider(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit = {},
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
) {

    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy
        ), label = "animatedValue"
    )

    val interaction = remember { MutableInteractionSource() }
    val isDragging by interaction.collectIsDraggedAsState()
    val density = LocalDensity.current

    Slider(
        value = animatedValue,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        thumb = { },
        modifier = modifier,
        track = { sliderState ->
            var width by remember { mutableIntStateOf(0) }
            val fraction by remember {
                derivedStateOf {
                    (animatedValue - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                }
            }

            val thumbSize = 72.dp

            val offsetHeight by animateFloatAsState(
                targetValue = with(density) { if (isDragging) (thumbSize / 2).toPx() else 0.dp.toPx() },
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioLowBouncy
                ), label = "offsetAnimation"
            )

            TimeSelectionTrack(
                thumbSize = thumbSize,
                onSizeChanged = { width = it.width },
                value = animatedValue,
                sliderState = sliderState,
                width = width,
                fraction = fraction,
                offsetHeight = offsetHeight
            )
        },
        interactionSource = interaction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionTrack(
    thumbSize: Dp,
    onSizeChanged: (IntSize) -> Unit,
    value: Float,
    sliderState: SliderState,
    width: Int,
    fraction: Float,
    offsetHeight: Float,
) {
    Box(
        Modifier
            .clearAndSetSemantics { }
            .height(thumbSize)
            .fillMaxWidth()
            .onSizeChanged(onSizeChanged),
    ) {
        Box(
            Modifier
                .zIndex(10f)
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(
                        x = lerp(
                            start = -(thumbSize / 2).toPx(),
                            stop = width - (thumbSize / 2).toPx(),
                            fraction = fraction
                        ).roundToInt(),
                        y = -offsetHeight.roundToInt(),
                    )
                }
        ) {
            Box(
                Modifier
                    .size(thumbSize)
                    .padding(10.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = CircleShape,
                    )
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center,
            ) {
                val df = DecimalFormat("##")
                df.setRoundingMode(RoundingMode.DOWN)
                Text(
                    df.format(value).toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        val strokeColor = MaterialTheme.colorScheme.onSurface
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        Box(
            Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .drawWithCache {
                    onDrawBehind {
                        scale(
                            scaleY = 1f,
                            scaleX = if (isLtr) 1f else -1f
                        ) {
                            drawSliderPath(
                                fraction = fraction,
                                offsetHeight = offsetHeight,
                                color = strokeColor,
                                steps = sliderState.steps
                            )
                        }
                    }
                }
        )
    }
}

fun DrawScope.drawSliderPath(
    fraction: Float,
    offsetHeight: Float,
    color: Color,
    steps: Int,
) {
    val path = Path()
    val activeWidth = size.width * fraction
    val midPointHeight = size.height / 2
    val curveHeight = midPointHeight - offsetHeight
    val beyondBounds = size.width * 2
    val ramp = 72.dp.toPx()

    // Point far beyond the right edge
    path.moveTo(
        x = beyondBounds,
        y = midPointHeight
    )

    // Line to the "base" right before the curve
    path.lineTo(
        x = activeWidth + ramp,
        y = midPointHeight
    )

    // Smooth curve to the top of the curve
    path.cubicTo(
        x1 = activeWidth + (ramp / 2),
        y1 = midPointHeight,
        x2 = activeWidth + (ramp / 2),
        y2 = curveHeight,
        x3 = activeWidth,
        y3 = curveHeight,
    )

    // Smooth curve down the curve to the "base" on the other side
    path.cubicTo(
        x1 = activeWidth - (ramp / 2),
        y1 = curveHeight,
        x2 = activeWidth - (ramp / 2),
        y2 = midPointHeight,
        x3 = activeWidth - ramp,
        y3 = midPointHeight
    )

    // Line to a point far beyond the left edge
    path.lineTo(
        x = -beyondBounds,
        y = midPointHeight
    )

    val variation = .1f

    // Line to a point far beyond the left edge
    path.lineTo(
        x = -beyondBounds,
        y = midPointHeight + variation
    )

    // Line to the "base" right before the curve
    path.lineTo(
        x = activeWidth - ramp,
        y = midPointHeight + variation
    )

    // Smooth curve to the top of the curve
    path.cubicTo(
        x1 = activeWidth - (ramp / 2),
        y1 = midPointHeight + variation,
        x2 = activeWidth - (ramp / 2),
        y2 = curveHeight + variation,
        x3 = activeWidth,
        y3 = curveHeight + variation,
    )

    // Smooth curve down the curve to the "base" on the other side
    path.cubicTo(
        x1 = activeWidth + (ramp / 2),
        y1 = curveHeight + variation,
        x2 = activeWidth + (ramp / 2),
        y2 = midPointHeight + variation,
        x3 = activeWidth + ramp,
        y3 = midPointHeight + variation,
    )

    // Line to a point far beyond the right edge
    path.lineTo(
        x = beyondBounds,
        y = midPointHeight + variation
    )

    val exclude = Path().apply {
        addRect(Rect(-beyondBounds, -beyondBounds, 0f, beyondBounds))
        addRect(Rect(size.width, -beyondBounds, beyondBounds, beyondBounds))
    }

    val trimmedPath = Path()
    trimmedPath.op(path, exclude, PathOperation.Difference)

    val pathMeasure = PathMeasure()
    pathMeasure.setPath(trimmedPath, false)

    val graduations = steps + 1
    for (i in 0..graduations) {
        val pos = pathMeasure.getPosition(
            (i / graduations.toFloat()) * pathMeasure.length / 2
        )
        val height = 10f
        when (i) {
            0, graduations -> drawCircle(
                color = color,
                radius = 10f,
                center = pos
            )

            else -> drawLine(
                strokeWidth = if (pos.x < activeWidth) 4f else 2f,
                color = color,
                start = pos + Offset(0f, height),
                end = pos + Offset(0f, -height),
            )
        }
    }

    clipRect(
        left = -beyondBounds,
        top = -beyondBounds,
        bottom = beyondBounds,
        right = activeWidth,
    ) {
        drawTrimmedPath(trimmedPath, color)
    }
    clipRect(
        left = activeWidth,
        top = -beyondBounds,
        bottom = beyondBounds,
        right = beyondBounds,
    ) {
        drawTrimmedPath(trimmedPath, color.copy(alpha = .2f))
    }
}

fun DrawScope.drawTrimmedPath(path: Path, color: Color) {
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 6f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}


@Composable
@PreviewLightDark
@PreviewDynamicColors
@PreviewScreenSizes
fun TimeSelectionSliderPreview() {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        TimeSelectionSlider(
            modifier = Modifier.padding(16.dp),
            value = 50f,
            onValueChange = {},
            valueRange = 1f..90f,
            90
        )
    }
}