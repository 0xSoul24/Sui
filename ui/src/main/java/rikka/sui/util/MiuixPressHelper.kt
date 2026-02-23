package rikka.sui.util

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.toColorInt
import androidx.core.view.get
import androidx.core.view.size
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import java.lang.ref.WeakReference

class MiuixPressHelper : View.OnTouchListener {
    private var scaleXAnimation: SpringAnimation? = null
    private var scaleYAnimation: SpringAnimation? = null
    private val viewBounds = Rect()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initAnimations(v)
                v.isPressed = true
                scaleDown()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v.isPressed = false
                scaleUp()
            }

            MotionEvent.ACTION_MOVE -> {
                v.getDrawingRect(viewBounds)
                if (!viewBounds.contains(event.x.toInt(), event.y.toInt())) {
                    scaleUp()
                }
            }
        }
        return false
    }

    private fun initAnimations(v: View) {
        if (scaleXAnimation == null) {
            scaleXAnimation = SpringAnimation(v, SpringAnimation.SCALE_X).apply {
                spring = SpringForce(1f).apply {
                    dampingRatio = 0.8f
                    stiffness = 600f
                }
            }
            scaleYAnimation = SpringAnimation(v, SpringAnimation.SCALE_Y).apply {
                spring = SpringForce(1f).apply {
                    dampingRatio = 0.8f
                    stiffness = 600f
                }
            }
        }
    }

    private fun scaleDown() {
        scaleXAnimation?.animateToFinalPosition(0.94f)
        scaleYAnimation?.animateToFinalPosition(0.94f)
    }

    private fun scaleUp() {
        scaleXAnimation?.animateToFinalPosition(1f)
        scaleYAnimation?.animateToFinalPosition(1f)
    }
}

fun androidx.appcompat.widget.PopupMenu.applyMiuixPopupStyle() {
    try {
        val mAnchorField = this.javaClass.getDeclaredField("mAnchor")
        mAnchorField.isAccessible = true
        val anchor = mAnchorField.get(this) as? View
        if (anchor != null) {
            val loc = IntArray(2)
            anchor.getLocationOnScreen(loc)
            MiuixPopupState.anchorX = loc[0]
            MiuixPopupState.anchorY = loc[1]
        }
    } catch (e: Exception) {
        android.util.Log.e("Sui", "Failed to extract popup anchor", e)
    }

    this.show()
    try {
        val mPopupField = this.javaClass.getDeclaredField("mPopup")
        mPopupField.isAccessible = true
        val mPopup = mPopupField.get(this)

        val getPopupMethod = mPopup.javaClass.getDeclaredMethod("getPopup")
        getPopupMethod.isAccessible = true
        val popup = getPopupMethod.invoke(mPopup)

        val getListViewMethod = popup.javaClass.getDeclaredMethod("getListView")
        getListViewMethod.isAccessible = true
        val listView = getListViewMethod.invoke(popup) as? android.widget.ListView

        listView?.let {
            it.clipToOutline = true
            it.isVerticalScrollBarEnabled = false
            it.overScrollMode = View.OVER_SCROLL_NEVER

            it.selector = MiuixSmoothCardDrawable.createSelectorWithOverlay(
                it.context,
                Color.TRANSPARENT,
                0f,
                false,
            )

            it.outlineProvider = MiuixSquircleProvider(16f)
        }
    } catch (e: Exception) {
        android.util.Log.e("Sui", "Failed to apply Miuix style to PopupMenu", e)
    }
}

fun androidx.appcompat.widget.PopupMenu.colorCheckedItemsMiuixBlue() {
    val highlightColor = "#277AF7".toColorInt()
    val menu = this.menu
    for (i in 0 until menu.size) {
        val item = menu[i]
        if (item.isChecked) {
            val title = item.title?.toString() ?: continue
            val spannable = android.text.SpannableString(title)
            spannable.setSpan(
                android.text.style.ForegroundColorSpan(highlightColor),
                0,
                spannable.length,
                android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE,
            )
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0,
                spannable.length,
                android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE,
            )
            item.title = spannable
        }
    }
}

object MiuixPopupDimOverlay {
    private var currentOverlayRef: WeakReference<View>? = null

    fun show(activity: android.app.Activity) {
        var overlay = currentOverlayRef?.get()

        if (overlay == null || overlay.context != activity) {
            overlay?.let { (it.parent as? ViewGroup)?.removeView(it) }

            overlay = View(activity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                setBackgroundColor(if (isDark) "#99000000".toColorInt() else "#4D000000".toColorInt())
                alpha = 0f
            }
            currentOverlayRef = WeakReference(overlay)

            val decorView = activity.window.decorView as ViewGroup
            decorView.addView(overlay)
        }

        overlay.animate().cancel()
        overlay.animate().alpha(1f).setDuration(250).setInterpolator(DecelerateInterpolator(1.5f)).start()
    }

    fun hide() {
        val overlay = currentOverlayRef?.get() ?: return

        overlay.animate().cancel()
        overlay.animate().alpha(0f).setDuration(250).setInterpolator(DecelerateInterpolator(1.5f)).withEndAction {
            (overlay.parent as? ViewGroup)?.removeView(overlay)
            if (currentOverlayRef?.get() == overlay) {
                currentOverlayRef = null
            }
        }.start()
    }

    fun cleanUp() {
        val overlay = currentOverlayRef?.get()
        overlay?.animate()?.cancel()
        (overlay?.parent as? ViewGroup)?.removeView(overlay)
        currentOverlayRef = null
    }
}
