package de.robnice.philipstvcontrol.presentation

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave

class ArcTextView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val textToDraw = "HREEMOTE"
    private val arcPath = Path()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    var logoBitmap: Bitmap? = null
        set(value) {
            field = value
            postInvalidate()
        }

    private val logoDestRect = RectF()
    private val pathMeasure = PathMeasure()
    private val pos = FloatArray(2)
    private val tan = FloatArray(2)

    init {
        textPaint.apply {
            style = Paint.Style.FILL
            color = Color.WHITE
            textSize = 22f
            isFakeBoldText = true
            letterSpacing = 0.15f
            textAlign = Paint.Align.LEFT
        }
        logoPaint.alpha = 255
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        arcPath.reset()
        val startX = w * 0.15f
        val endX = w * 0.85f
        val startY = h * 0.70f

        arcPath.moveTo(startX, startY)
        arcPath.quadTo(w * 0.5f, -h * 0.25f, endX, startY)

        pathMeasure.setPath(arcPath, false)

        haloPaint.strokeWidth = textPaint.textSize * 1.5f

        haloPaint.shader = LinearGradient(
            w * 0.15f, 0f,
            w * 0.85f, 0f,
            intArrayOf(
                Color.TRANSPARENT,
                "#3D5AFE".toColorInt(),
                "#1A237E".toColorInt(),
                Color.TRANSPARENT
            ),
            floatArrayOf(
                0f,
                0.1f,
                0.5f,
                1f
            ),
            Shader.TileMode.CLAMP
        )

        updateLogoPosition()
    }

    private fun updateLogoPosition() {
        val textWidth = textPaint.measureText(textToDraw)
        val logoSize = textPaint.textSize * 2.2f
        val spacing = -textPaint.textSize * 0.2f
        val totalContentWidth = logoSize + spacing + textWidth
        val startOffset = ((pathMeasure.length - totalContentWidth) / 2) - 8f

        val logoCenterOnPath = startOffset + (logoSize / 2)
        pathMeasure.getPosTan(logoCenterOnPath, pos, tan)

        logoDestRect.set(
            pos[0] - (logoSize * 0.5f),
            pos[1] - (logoSize * 0.5f),
            pos[0] + (logoSize * 0.5f),
            pos[1] + (logoSize * 0.5f)
        )

        this.textStartOffset = startOffset + logoSize + spacing
    }
    private var textStartOffset = 0f

    override fun onDraw(canvas: Canvas) {
        if (arcPath.isEmpty) return

        if (logoBitmap == null) {
            haloPaint.alpha = 80
        } else {
            haloPaint.alpha = 255
        }

        canvas.drawPath(arcPath, haloPaint)

        logoBitmap?.let {
            canvas.withSave {
                val degrees =
                    Math.toDegrees(Math.atan2(tan[1].toDouble(), tan[0].toDouble())).toFloat()
                rotate(degrees, pos[0], pos[1])
                drawBitmap(it, null, logoDestRect, logoPaint)
            }

            canvas.drawTextOnPath(textToDraw, arcPath, textStartOffset, textPaint.textSize * 0.3f, textPaint)
        }
    }
}