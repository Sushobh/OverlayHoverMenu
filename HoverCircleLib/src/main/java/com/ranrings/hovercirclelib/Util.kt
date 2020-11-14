package com.ranrings.circlemenutest

import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.roundToInt


/**
 * @param event The move event on the victimView
 * @param victimView The view which is dragged onto
 * @param targetView The View to which the victim view is dragged onto
 */
 fun isViewIntersectingMotionEventClick(targetView : View,  victimView : View ) : Boolean{
    val locationRemover = IntArray(2)
    targetView.getLocationOnScreen(locationRemover)
    val removeXRaw = locationRemover[0]
    val removeYRaw = locationRemover[1]

    val locationVictim = IntArray(2)
    victimView.getLocationOnScreen(locationVictim)
    val victimXRaw = locationVictim[0]
    val victimYRaw = locationVictim[1]



    val removerViewRect = Rect(removeXRaw,removeYRaw,removeXRaw+(targetView.width),
        removeYRaw+(targetView.height))
    val thisRect = Rect(victimXRaw, victimYRaw,
        victimXRaw + victimView.width, victimYRaw+ victimView.height
    )

    return removerViewRect.intersect(thisRect)
}

  fun getParentView(windowManager: WindowManager,view: View) : ViewParent {
    var displayMetrics = DisplayMetrics()
    windowManager.getDefaultDisplay().getMetrics(displayMetrics)

    var locArray = IntArray(2)
    view.getLocationOnScreen(locArray)
    return object  : ViewParent {


        override fun getCurrentXOutside(): Int {
            return  locArray[0]
        }

        override fun getCurrentYOutSide(): Int {
            return  locArray[1]
        }

        override fun getWidth(): Int {
            return displayMetrics.widthPixels
        }

        override fun getHeight(): Int {
            return displayMetrics.heightPixels
        }

    }
}

