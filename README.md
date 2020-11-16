# OverlayHoverMenu
An overlay menu with circular layout.

<br/>
<br/>




<img src="https://raw.github.com/Sushobh/OverlayHoverMenu/master/recording2.gif" width="400" height="600"/>

## Notes

This library is meant for use with background services which **have the draw overlay permission**. It interacts
with the window manager and adds views to it and as a result this cannot be used as a normal view.



## How to get it?

In your project level gradle file, add this

```java
allprojects {
   repositories {
	  maven { url 'https://jitpack.io' }
	}
   }
```
And then add the dependency in your app level gradle file
```java
   dependencies {
	    implementation 'com.github.Sushobh:OverlayHoverMenu:version2'
    }
```



## How to use?

Initiate the menu in your background service like this. Make sure that you have   
 **SYSTEM_ALERT_WINDOW** permission in manifest and also 
 the overlay permission during  runtime. Checkout the app included in the repo for a demo.

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


