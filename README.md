# OverlayHoverMenu
An overlay menu with circular layout.

<br/>
<br/>




<img src="https://raw.github.com/Sushobh/OverlayHoverMenu/master/recording.gif" width="400" height="600"/>

## Notes

This library is meant for use with background services which **have the draw overlay permission**. It interacts
with the window manager and adds views to it and as a result this cannot be used as normal view.


## How to use?

Initiate the menu in your background service like this. Make sure that you have   
 **<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />** permission and also 
 the overlay permission during the runtime. Checkout the app included in the repo for a demo.

```kotlin
    lateinit var hoverCircleMenu: HoverCircleMenu
    
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

```
