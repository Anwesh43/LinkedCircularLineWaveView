package com.anwesh.uiprojects.circularlinewaveview

/**
 * Created by anweshmishra on 22/08/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.util.Log

val nodes : Int = 10

fun Canvas.drawCLWNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val index : Int = (i) % 2
    val index1 : Int = (i / 2) % 2
    val r : Float = gap / 2
    paint.style = Paint.Style.STROKE
    paint.color = Color.parseColor("#1976D2")
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(w / 2, gap / 2 + gap * i)
    val path : Path = Path()
    for (j in 270..(270 + (180 * scale).toInt())) {
        val x : Float = (r) * (1 - 2 * index1) * index * Math.cos(j *   Math.PI/180).toFloat()
        val y : Float = (r)  * Math.sin(j *   Math.PI/180).toFloat()
        if (j == 270) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    drawPath(path, paint)
    restore()
}

class CircularLineWaveView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }

        fun update(cb : (Float) -> Unit) {
            scale += 0.1f * dir
            Log.d("scale", "${scale}")
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CLWNode(var i : Int, val state : State = State()) {
        private var next : CLWNode? = null
        private var prev : CLWNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = CLWNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCLWNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CLWNode {
            var curr : CLWNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class CircularLineWave(var i : Int) {

        private var curr : CLWNode = CLWNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : CircularLineWaveView) {

        private val animator : Animator = Animator(view)
        private val clw : CircularLineWave = CircularLineWave(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            clw.draw(canvas, paint)
            animator.animate {
                clw.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            clw.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : CircularLineWaveView {
            val view : CircularLineWaveView = CircularLineWaveView(activity)
            activity.setContentView(view)
            return view
        }
    }
}