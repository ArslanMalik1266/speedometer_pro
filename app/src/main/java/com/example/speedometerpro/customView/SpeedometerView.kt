package com.example.speedometerpro.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class SpeedometerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var maxSpeed = 180f
    private var speed = 50f

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rect = RectF()

    init {
        arcPaint.style = Paint.Style.STROKE
        arcPaint.strokeWidth = 40f
        arcPaint.color = Color.DKGRAY
        arcPaint.strokeCap = Paint.Cap.ROUND

        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = 40f
        progressPaint.strokeCap = Paint.Cap.ROUND

        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 120f

        tickPaint.color = Color.GRAY
        tickPaint.strokeWidth = 4f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 60f
        val size = min(width, height)
        val radius = size / 2 - padding

        rect.set(
            width / 2 - radius,
            height / 2 - radius,
            width / 2 + radius,
            height / 2 + radius
        )

        // Draw base arc (background)
        canvas.drawArc(rect, 180f, 180f, false, arcPaint)

        // Gradient for progress
        val gradient = SweepGradient(
            width / 2f,
            height / 2f,
            intArrayOf(Color.YELLOW, Color.rgb(255, 140, 0), Color.RED),
            floatArrayOf(0f, 0.5f, 1f)
        )
        progressPaint.shader = gradient

        val sweepAngle = (speed / maxSpeed) * 180f

        canvas.drawArc(rect, 180f, sweepAngle, false, progressPaint)

        drawTicks(canvas, radius)

        // Draw speed text
        canvas.drawText(
            speed.toInt().toString(),
            width / 2f,
            height / 2f + 40,
            textPaint
        )

        textPaint.textSize = 40f
        canvas.drawText(
            "km/h",
            width / 2f,
            height / 2f + 90,
            textPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = width / 2
        setMeasuredDimension(width, height)
    }

    private fun drawTicks(canvas: Canvas, radius: Float) {
        val centerX = width / 2f
        val centerY = height / 2f

        for (i in 0..10) {
            val angle = Math.toRadians((180 + i * 18).toDouble())

            val startX = centerX + (radius - 40) * Math.cos(angle)
            val startY = centerY + (radius - 40) * Math.sin(angle)

            val stopX = centerX + radius * Math.cos(angle)
            val stopY = centerY + radius * Math.sin(angle)

            canvas.drawLine(
                startX.toFloat(),
                startY.toFloat(),
                stopX.toFloat(),
                stopY.toFloat(),
                tickPaint
            )
        }
    }

    fun setSpeed(value: Float) {
        speed = value.coerceIn(0f, maxSpeed)
        invalidate()
    }
}