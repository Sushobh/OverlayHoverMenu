package com.ranrings.hovercirclemenu

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.IBinder
import android.widget.TextView
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
                    ContextCompat.getDrawable(baseContext,R.drawable.ic_crown)!!,
                    ContextCompat.getDrawable(baseContext,R.drawable.ic_color_wheel)!!,
                    ContextCompat.getDrawable(baseContext,R.drawable.ic_lightbulb)!!
            )
            val mainButton =  ContextCompat.getDrawable(baseContext,R.drawable.ic_earth)!!
            val removeButton = ContextCompat.getDrawable(baseContext,R.drawable.ic_delete)

            hoverCircleMenu = HoverCircleMenu.Builder(baseContext)
                    .setMainButton(mainButton)
                    .setButtonIcons(buttonIcons)
                    .setDimenInDP(60)
                    .setRemoveButton(removeButton)
                    .setButtonClickListener(object : HoverCircleMenu.ButtonClickListener {
                        override fun buttonClicked(index: Int) {
                            showToast(index)
                        }
                        override fun onRemoved() {
                             hoverCircleMenu.remove()
                        }

                    })
                    .build()
            hoverCircleMenu.start()

        }
        return super.onStartCommand(intent, flags, startId)

    }

    fun showToast(index : Int){
        val toast =  Toast.makeText(baseContext,"Clicked at ${index}",Toast.LENGTH_SHORT)
        toast.view.setBackgroundColor(Color.BLACK)
        val text: TextView = toast.view.findViewById(android.R.id.message)
        text.setTextColor(Color.WHITE)
        toast.show()
    }


}