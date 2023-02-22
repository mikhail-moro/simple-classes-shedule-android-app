package org.karasiksoftware.for_ui

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

// Custom swipe listener
// If user swiped with distance more than SWIPE_THRESHOLD it`s detect right ot left swipe
open class AnimatedSwipeListener : View.OnTouchListener {

    companion object {
        private const val SWIPE_THRESHOLD = 100
        var x = 0.0
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.action){
                MotionEvent.ACTION_DOWN -> {
                    if (view != null) {
                        x = view.x.toDouble() - event.rawX
                    }
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (view != null) {
                        view.animate()
                            .x(event.rawX + x.toFloat())
                            .setDuration(0)
                            .start()
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (view != null) {
                        if (abs(view.x) < SWIPE_THRESHOLD) {
                            view.animate()
                                .x(0f)
                                .setDuration(100)
                                .start()
                        } else {
                            if (view.x > 0f) {
                                onSwipeLeft()
                            } else {
                                onSwipeRight()
                            }
                        }
                    }
                    return true
                }

                else -> return true
            }
        }
        return true
    }

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}
}