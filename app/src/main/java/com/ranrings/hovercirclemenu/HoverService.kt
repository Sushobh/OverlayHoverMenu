package com.ranrings.hovercirclemenu

import android.app.Service
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ranrings.hovercirclelib.HoverCircleMenu

class HoverService : Service() {


    lateinit var hoverCircleMenu: HoverCircleMenu

    override fun onBind(intent: Intent?): IBinder? {
        return null!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(!this::hoverCircleMenu.isInitialized){

            val buttonIcons = listOf<Drawable>(
                    ContextCompat.getDrawable(baseContext,R.drawable.ic_beanie)!!,
                    ContextCompat.getDrawable(baseContext,R.drawable.ic_clipboard)!!,
                    ContextCompat.getDrawable(baseContext,R.drawable.ic_color_pencils)!!,
                    ContextCompat.getDrawable(baseContext,R.drawable.ic_earth)!!,
                    ContextCompat.getDrawable(baseContext,R.drawable.ic_color_wheel)!!,
                    ContextCompat.getDrawable(baseContext,R.drawable.ic_lightbulb)!!
            )
            val mainButton =  ContextCompat.getDrawable(baseContext,R.drawable.ic_crown)!!
            val removeButton = ContextCompat.getDrawable(baseContext,R.drawable.ic_delete)

            val hoverCircleMenu = HoverCircleMenu.Builder()
                    .setMainButton(mainButton)
                    .setButtonIcons(buttonIcons)
                    .setDimen(convertToPx(60))
                    .setRemoveButton(removeButton)
                    .setButtonClickListener(object : HoverCircleMenu.ButtonClickListener {
                        override fun buttonClicked(index: Int) {
                            Toast.makeText(baseContext,"Clicked at ${index}",Toast.LENGTH_SHORT).show()
                        }
                        override fun onRemoved() {
                             hoverCircleMenu.remove()
                        }

                    })
                    .build(baseContext)
            hoverCircleMenu.start()
        }
        return super.onStartCommand(intent, flags, startId)

    }

    fun convertToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}