package com.mytlogos.enterprise.tools


/*
 * Copyright 2016-2018 Davide Steduto, Davidea Solutions Sprl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import java.util.*


/**
 *
 * Modified Version of the Original as the original jar provider, bintray, was shutdown
 * and no new jar provider used.
 * Converted to Kotlin and cut out any functions which rely on FlexibleAdapter Library Code.
 *
 * @author Davide Steduto
 *
 * @since 14/06/2016 Created in main package
 * <br></br>17/12/2017 Moved into UI package
 */
/**
 * Helper method to set the background to a view, depending on the android version.
 *
 * @param view     the view to apply the drawable
 * @param drawable drawable object
 * @since 1.0.0-b1
 */
fun setBackgroundCompat(view: View?, drawable: Drawable?) {
    ViewCompat.setBackground(view!!, drawable)
}

/**
 * Helper method to set the background to a view, depending on the android version
 *
 * @param view        the view to apply the drawable
 * @param drawableRes drawable resource id
 * @since 1.0.0-b1
 */
fun setBackgroundCompat(view: View, @DrawableRes drawableRes: Int) {
    setBackgroundCompat(view, getDrawableCompat(view.context, drawableRes))
}

/**
 * Ultra compat method to set the background to a view, providing color resource identifiers.
 *
 * **Note:** If you already have the color in *Integer* format, please obtain the
 * `Drawable` object from [.getSelectableBackgroundCompat] and set
 * it into [.setBackgroundCompat].
 *
 * @param view            the view interested at the background
 * @param normalColorRes  the color resource id
 * @param pressedColorRes the pressed color resource id
 * @param rippleColorRes  the ripple color resource id
 * @since 1.0.0-b5
 */
fun setBackgroundCompat(
    view: View,
    @ColorRes normalColorRes: Int,
    @ColorRes pressedColorRes: Int,
    @ColorRes rippleColorRes: Int,
) {
    val context = view.context
    val drawable: Drawable = if (hasMarshmallow()) {
        getSelectableBackgroundCompat(
            context.getColor(normalColorRes),
            context.getColor(pressedColorRes),
            context.getColor(rippleColorRes))
    } else {
        getSelectableBackgroundCompat(
            context.resources.getColor(normalColorRes),
            context.resources.getColor(pressedColorRes),
            context.resources.getColor(rippleColorRes))
    }
    setBackgroundCompat(view, drawable)
}

/**
 * Helper method to get the drawable by its resource. Specific to the correct android version.
 *
 * @param context     the context
 * @param drawableRes drawable resource id
 * @return the drawable object
 * @since 1.0.0-b1
 */
fun getDrawableCompat(context: Context, @DrawableRes drawableRes: Int): Drawable? {
    return try {
        if (hasLollipop()) {
            context.resources.getDrawable(drawableRes, context.theme)
        } else {
            context.resources.getDrawable(drawableRes)
        }
    } catch (ex: Exception) {
        null
    }
}

/**
 * Helper method to get the *system (or overridden)* default `selectableItemBackground` Drawable.
 * Returns the `R.attr.selectableItemBackground` of the style attribute.
 *
 * @param context the context
 * @return Default selectable item background drawable
 * @since 1.0.0-b1
 */
fun getSelectableItemBackground(context: Context): Drawable? {
    val outValue = TypedValue()
    // It's important to not use the android.R because this wouldn't add the overridden drawable
    context.theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true)
    return getDrawableCompat(context, outValue.resourceId)
}

/**
 * Helper method to get the *system (or overridden)* default `colorControlHighlight`.
 * Returns the color of the `R.attr.colorControlHighlight` of the style attribute.
 *
 * @param context the context
 * @return Default Color Control Highlight
 * @since 1.0.0-b1
 */
@ColorInt
fun getColorControlHighlight(context: Context): Int {
    val outValue = TypedValue()
    // It's important to not use the android.R because this wouldn't add the overridden drawable
    context.theme.resolveAttribute(R.attr.colorControlHighlight, outValue, true)
    return if (hasMarshmallow()) {
        context.getColor(outValue.resourceId)
    } else {
        context.resources.getColor(outValue.resourceId)
    }
}

/**
 *
 */
fun View.setDefaultSelectableBackgroundCompat() {
    val drawable = getSelectableBackgroundCompat(
        Color.WHITE,  // normal background
        Color.GRAY,  // pressed background
        Color.BLACK
    )
    setBackgroundCompat(this, drawable)
}

/**
 * Helper method to get a custom selectable background with Ripple color, if device has at least Lollipop.
 *
 * @param normalColor  the color in normal state
 * @param pressedColor the pressed color
 * @param rippleColor  the color of the ripple
 * @return the RippleDrawable with StateListDrawable if at least Lollipop, the normal
 * StateListDrawable otherwise
 * @since 1.0.0-b1
 */
fun getSelectableBackgroundCompat(
    @ColorInt normalColor: Int,
    @ColorInt pressedColor: Int,
    @ColorInt rippleColor: Int,
): Drawable {
    return if (hasLollipop()) {
        RippleDrawable(ColorStateList.valueOf(rippleColor),
            getStateListDrawable(normalColor, pressedColor),
            getRippleMask(normalColor))
    } else {
        getStateListDrawable(normalColor, pressedColor)
    }
}

/**
 * Adds a ripple effect to any Drawable background.
 *
 * @param drawable    any background drawable
 * @param rippleColor the color of the ripple
 * @return the RippleDrawable with the chosen background drawable if at least Lollipop,
 * the provided drawable otherwise
 * @since 1.0.0-b1
 */
fun getRippleDrawable(drawable: Drawable, @ColorInt rippleColor: Int): Drawable {
    return if (hasLollipop()) {
        RippleDrawable(ColorStateList.valueOf(rippleColor),
            drawable, getRippleMask(Color.BLACK))
    } else {
        drawable
    }
}

private fun getRippleMask(@ColorInt color: Int): Drawable {
    val outerRadii = FloatArray(8)
    // 3 is the radius of final ripple, instead of 3 we can give required final radius
    Arrays.fill(outerRadii, 3f)
    val r = RoundRectShape(outerRadii, null, null)
    val shapeDrawable = ShapeDrawable(r)
    shapeDrawable.paint.color = color
    return shapeDrawable
}

private fun getStateListDrawable(
    @ColorInt normalColor: Int,
    @ColorInt pressedColor: Int,
): StateListDrawable {
    val states = StateListDrawable()
    states.addState(intArrayOf(R.attr.state_activated), getColorDrawable(pressedColor))
    if (!hasLollipop()) {
        states.addState(intArrayOf(R.attr.state_pressed), getColorDrawable(pressedColor))
    }
    states.addState(intArrayOf(), getColorDrawable(normalColor))
    // Animating across states.
    // It seems item background is lost on scrolling out of the screen on 21 <= API <= 23
    if (!hasLollipop() || hasNougat()) {
        val duration = 200 //android.R.integer.config_shortAnimTime
        states.setEnterFadeDuration(duration)
        states.setExitFadeDuration(duration)
    }
    return states
}

/**
 * Generates the `ColorDrawable` object from the provided Color.
 *
 * @param color the color
 * @return the `ColorDrawable` object
 * @since 1.0.0-b1
 */
fun getColorDrawable(@ColorInt color: Int): ColorDrawable {
    return ColorDrawable(color)
}
