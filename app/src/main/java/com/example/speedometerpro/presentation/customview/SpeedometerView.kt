package com.example.speedometerpro.presentation.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class SpeedometerMeterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val segmentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val railBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerCasingPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val totalSegments = 13
    private val startAngle = 135f
    private val sweepAngle = 270f
    private val gapBetweenSegments = 0.5f

    // Current speed (0 to 13)
    var speed: Float = 0f
        set(value) {
            field = value.coerceIn(0f, totalSegments.toFloat())
            invalidate()
        }

    private val colors = intArrayOf(
        Color.parseColor("#F6A81D"), // Amber Yellow
        Color.parseColor("#E65100"), // Mid Orange
        Color.parseColor("#BF360C")  // Deep Burnt Orange
    )

    private val offColor = Color.parseColor("#2F2F2F") // Grey for inactive segments

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val widthF = width.toFloat()
        val heightF = height.toFloat()
        val centerX = widthF / 2
        val centerY = heightF / 2

        val baseRadius = min(widthF, heightF) / 2 * 0.60f
        val segmentThickness = baseRadius * 0.45f
        val outerRadius = baseRadius + (segmentThickness / 2)
        val innerRadius = baseRadius - (segmentThickness / 2)

        // 1. OUTER CASING
        val casingGap = baseRadius * 0.08f
        val casingRadius = outerRadius + casingGap
        outerCasingPaint.style = Paint.Style.STROKE
        outerCasingPaint.strokeWidth = 12f
        outerCasingPaint.color = Color.parseColor("#4C4C4C")
        canvas.drawArc(RectF(centerX - casingRadius, centerY - casingRadius, centerX + casingRadius, centerY + casingRadius), startAngle - 3, sweepAngle + 6, false, outerCasingPaint)

        // 2. WHITE RAILS
        railBorderPaint.style = Paint.Style.STROKE
        railBorderPaint.strokeWidth = 3f
        railBorderPaint.color = Color.WHITE
        val outerRect = RectF(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius)
        val innerRect = RectF(centerX - innerRadius, centerY - innerRadius, centerX + innerRadius, centerY + innerRadius)
        canvas.drawArc(outerRect, startAngle - 1, sweepAngle + 2, false, railBorderPaint)
        canvas.drawArc(innerRect, startAngle - 1, sweepAngle + 2, false, railBorderPaint)

        // 3. PERMANENT START/END CAPS
        val pokeDepth = baseRadius * 0.08f
        drawCapLine(canvas, centerX, centerY, outerRadius, innerRadius - pokeDepth, startAngle.toDouble())
        drawCapLine(canvas, centerX, centerY, outerRadius, innerRadius - pokeDepth, (startAngle + sweepAngle).toDouble())

        // 4. SEGMENTS
        val segmentSweep = (sweepAngle - (gapBetweenSegments * (totalSegments - 1))) / totalSegments
        segmentPaint.style = Paint.Style.STROKE
        segmentPaint.strokeWidth = segmentThickness
        segmentPaint.strokeCap = Paint.Cap.BUTT
        val segmentRect = RectF(centerX - baseRadius, centerY - baseRadius, centerX + baseRadius, centerY + baseRadius)

        for (i in 0 until totalSegments) {
            val currentStartAngle = startAngle + i * (segmentSweep + gapBetweenSegments)

            // Linear Logic: Fill segments partially
            val fillFactor = (speed - i).coerceIn(0f, 1f)

            // Draw Background (Off Color)
            segmentPaint.shader = null
            segmentPaint.color = offColor
            canvas.drawArc(segmentRect, currentStartAngle, segmentSweep, false, segmentPaint)

            if (fillFactor > 0f) {
                val baseColor = when {
                    i < 5 -> colors[0]
                    i in 5..7 -> interpolate(colors[0], colors[1], (i - 5).toFloat() / 2f)
                    else -> interpolate(colors[1], colors[2], (i - 8).toFloat() / (totalSegments - 9))
                }

                val shadowColor = if (fillFactor > 0) darkenColor(baseColor, 0.55f) else Color.BLACK
                val lightColor = if (fillFactor > 0) lightenColor(baseColor, 1.1f) else Color.parseColor("#333333")

                segmentPaint.shader = RadialGradient(
                    centerX, centerY, outerRadius,
                    intArrayOf(shadowColor, baseColor, lightColor),
                    floatArrayOf(innerRadius / outerRadius, (innerRadius + segmentThickness * 0.45f) / outerRadius, 1f),
                    Shader.TileMode.CLAMP
                )
                // Linear fill of the segment
                canvas.drawArc(segmentRect, currentStartAngle, segmentSweep * fillFactor, false, segmentPaint)
            }
        }

        // 5. LINEAR NEEDLE (Outside the loop for smooth movement)
        // This calculates the exact angle based on the float speed
        if (speed > 0) {
            // totalGaps = totalSegments - 1
            val currentNeedleAngle = startAngle + (speed * segmentSweep) + (floor(speed) * gapBetweenSegments)

            railBorderPaint.strokeWidth = 5f
            railBorderPaint.color = Color.WHITE
            // Add a small glow effect to the needle
            railBorderPaint.setShadowLayer(10f, 0f, 0f, Color.WHITE)

            drawCapLine(canvas, centerX, centerY, outerRadius + 2f, innerRadius - (pokeDepth * 0.8f), currentNeedleAngle.toDouble())

            railBorderPaint.clearShadowLayer() // clean up
            railBorderPaint.strokeWidth = 3f
        }
    }

    private fun drawCapLine(canvas: Canvas, cx: Float, cy: Float, rOut: Float, rIn: Float, angleDeg: Double) {
        val rad = Math.toRadians(angleDeg)
        canvas.drawLine(
            (cx + rOut * cos(rad)).toFloat(), (cy + rOut * sin(rad)).toFloat(),
            (cx + rIn * cos(rad)).toFloat(), (cy + rIn * sin(rad)).toFloat(),
            railBorderPaint
        )
    }

    private fun interpolate(c1: Int, c2: Int, f: Float): Int {
        val r = (Color.red(c1) + (Color.red(c2) - Color.red(c1)) * f).toInt()
        val g = (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * f).toInt()
        val b = (Color.blue(c1) + (Color.blue(c2) - Color.blue(c1)) * f).toInt()
        return Color.rgb(r, g, b)
    }

    private fun darkenColor(color: Int, factor: Float): Int = Color.rgb(
        (Color.red(color) * factor).toInt(),
        (Color.green(color) * factor).toInt(),
        (Color.blue(color) * factor).toInt()
    )

    private fun lightenColor(color: Int, factor: Float): Int = Color.rgb(
        (Color.red(color) * factor).toInt().coerceAtMost(255),
        (Color.green(color) * factor).toInt().coerceAtMost(255),
        (Color.blue(color) * factor).toInt().coerceAtMost(255)
    )
}