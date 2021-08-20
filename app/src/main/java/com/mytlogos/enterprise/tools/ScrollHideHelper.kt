package com.mytlogos.enterprise.tools

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.LinearInterpolator
import androidx.annotation.IntDef

class ScrollHideHelper {
    private var previousScrollDiffY = 0
    private var lastScrollY: Long = 0
    private var lastScrollX: Long = 0
    private var previousScrollDiffX = 0

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(value = [BOTTOM, TOP, RIGHT, LEFT])
    internal annotation class Direction

    fun hideGroups(
        oldX: Int,
        newX: Int,
        oldY: Int,
        newY: Int,
        bottom: View?,
        left: View?,
        top: View?,
        right: View?
    ) {
        val diffY = newY - oldY
        val currentTime = System.currentTimeMillis()
        val lastScrollTimeDiffY = currentTime - lastScrollY
        if (lastScrollTimeDiffY >= 100 || diffY >= 10 || Integer.signum(diffY) == Integer.signum(
                previousScrollDiffY)
        ) {
            if (bottom != null) {
                setHideViewGroupParams(diffY, bottom, BOTTOM)
            }
            if (top != null) {
                setHideViewGroupParams(diffY, top, TOP)
            }
            lastScrollY = currentTime
            previousScrollDiffY = diffY
        }
        val diffX = newX - oldX
        val lastScrollTimeDiffX = currentTime - lastScrollX
        if (lastScrollTimeDiffX >= 100 || diffX >= 10 || Integer.signum(diffX) == Integer.signum(
                previousScrollDiffX)
        ) {
            if (left != null) {
                setHideViewGroupParams(diffX, left, LEFT)
            }
            if (right != null) {
                setHideViewGroupParams(diffX, right, RIGHT)
            }
            lastScrollX = currentTime
            previousScrollDiffX = diffX
        }
    }

    fun toggleGroups(bottom: View?, left: View?, top: View?, right: View?) {
        if (bottom != null) {
            toggleDirectionGroup(bottom, BOTTOM)
        }
        if (top != null) {
            toggleDirectionGroup(top, TOP)
        }
        if (left != null) {
            toggleDirectionGroup(left, LEFT)
        }
        if (right != null) {
            toggleDirectionGroup(right, RIGHT)
        }
    }

    private fun toggleDirectionGroup(view: View, @Direction direction: Int) {
        val layoutParams = view.layoutParams as MarginLayoutParams
        val margin: Int = when (direction) {
            BOTTOM -> layoutParams.bottomMargin
            LEFT -> layoutParams.leftMargin
            TOP -> layoutParams.topMargin
            RIGHT -> layoutParams.rightMargin
            else -> throw IllegalArgumentException("unknown direction: $direction")
        }
        val start: Int
        val end: Int
        if (margin == 0) {
            start = 0
            end = if (direction == BOTTOM || direction == TOP) -view.height else -view.width
        } else {
            start = margin
            end = 0
        }
        val animator = ValueAnimator.ofInt(start, end)
        animator.interpolator = LinearInterpolator()
        animator.duration = 200
        animator.addUpdateListener { animation: ValueAnimator ->
            val value = animation.animatedValue
            if (value is Int) {
                val params = view.layoutParams as MarginLayoutParams
                when (direction) {
                    BOTTOM -> params.bottomMargin = value
                    LEFT -> params.leftMargin = value
                    TOP -> params.topMargin = value
                    else -> params.rightMargin = value
                }
                view.layoutParams = params
            } else {
                System.err.println("expected an integer, got: $value")
            }
        }
        animator.start()
    }

    fun showGroups(bottom: View?, left: View?, top: View?, right: View?) {
        if (bottom != null) {
            setShowViewGroupParams(bottom, BOTTOM)
        }
        if (top != null) {
            setShowViewGroupParams(top, TOP)
        }
        if (left != null) {
            setShowViewGroupParams(left, LEFT)
        }
        if (right != null) {
            setShowViewGroupParams(right, RIGHT)
        }
    }

    private fun setShowViewGroupParams(view: View, @Direction direction: Int) {
        val layoutParams = view.layoutParams as MarginLayoutParams
        when (direction) {
            BOTTOM -> layoutParams.bottomMargin = 0
            LEFT -> layoutParams.leftMargin = 0
            TOP -> layoutParams.topMargin = 0
            RIGHT -> layoutParams.rightMargin = 0
            else -> throw IllegalArgumentException("unknown direction: $direction")
        }
        view.layoutParams = layoutParams
    }

    private fun setHideViewGroupParams(diff: Int, view: View, @Direction direction: Int) {
        if (diff == 0) {
            return
        }
        val layoutParams = view.layoutParams as MarginLayoutParams
        var margin: Int = when (direction) {
            BOTTOM -> layoutParams.bottomMargin
            LEFT -> layoutParams.leftMargin
            TOP -> layoutParams.topMargin
            RIGHT -> layoutParams.rightMargin
            else -> throw IllegalArgumentException("unknown direction: $direction")
        }
        margin -= diff

        val minMargin = if (direction == BOTTOM || direction == TOP) -view.height else -view.width
        val maxMargin = 0
        if (margin < minMargin) {
            margin = minMargin
        } else if (margin > maxMargin) {
            margin = maxMargin
        }
        when (direction) {
            BOTTOM -> layoutParams.bottomMargin = margin
            LEFT -> layoutParams.leftMargin = margin
            TOP -> layoutParams.topMargin = margin
            else -> layoutParams.rightMargin = margin
        }
        view.layoutParams = layoutParams
    }

    companion object {
        const val BOTTOM = 1
        const val TOP = 2
        const val RIGHT = 3
        const val LEFT = 4
    }
}