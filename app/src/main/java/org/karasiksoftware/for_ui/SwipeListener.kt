package org.karasiksoftware.for_ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

// Custom swipe listener
// If user swiped with distance and velocity more than SWIPE_THRESHOLD and SWIPE_VELOCITY_THRESHOLD
// it`s detect right ot left swipe
open class SwipeListener(ctx: Context) : View.OnTouchListener {

    private val gestureDetector: GestureDetector

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }


    private inner class GestureListener : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onShowPress(e: MotionEvent) {
            return Unit
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffX = e2.x - e1.x
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight()
                    } else {
                        onSwipeLeft()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick()

            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            return Unit
        }

    }

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}

    open fun onClick() {}
}