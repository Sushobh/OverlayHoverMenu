package com.ranrings.hovercirclelib;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;


import com.ranrings.circlemenutest.UtilKt;
import com.ranrings.circlemenutest.ViewParent;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;


 class DragViewRemover implements ViewRemover {


    private final ImageView imageView;
    private final WindowManager windowManager;
    private int dimen = 0;
    private WindowManager.LayoutParams params;
    private final int animDuration = 400;
    private int originalY = 0;
    private int animY = 0;
    private boolean isDisplayed = false;

    private ValueAnimator displayAnimator;
    private ValueAnimator shrinkAnimator;

    public DragViewRemover(Drawable drawable, Context context,int dimension, WindowManager windowManager){
        imageView = new ImageView(context);
        imageView.setImageDrawable(drawable);
        this.windowManager = windowManager;
        this.dimen = dimension;
    }

    public void init(){
        createParams();
        windowManager.addView(imageView,params);
        ViewParent viewParent = UtilKt.getParentView(windowManager,imageView);
        int centerX = viewParent.getWidth()/2 - dimen/2;
        int bottomMargin = 20;
        int centerY = viewParent.getHeight() - dimen/2 - bottomMargin;
        originalY = centerY;
        int verticalAnimDistance = 300;
        animY = originalY - verticalAnimDistance;
        params.x = centerX;
        params.y = centerY;
        params.width = dimen;
        params.height = dimen;
        windowManager.updateViewLayout(imageView,params);
        imageView.setVisibility(View.GONE);
        imageView.setTranslationZ(20f);
    }



    @Override
    public void remove() {
        if(shrinkAnimator != null && shrinkAnimator.isRunning()){
            return;
        }
        updateParams();
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofInt("y", animY, originalY);
        shrinkAnimator = ValueAnimator.ofPropertyValuesHolder(alpha, pvhY)
                .setDuration(animDuration);
        shrinkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        shrinkAnimator.addUpdateListener(animation -> {
            params.y = (int) animation.getAnimatedValue("y");
            float alphaValue = (float)animation.getAnimatedValue("alpha");
            imageView.setAlpha(alphaValue);
            imageView.setScaleX(alphaValue);
            imageView.setScaleY(alphaValue);
            updateView();
            if(animation.getAnimatedFraction() == 1f){
                imageView.setVisibility(View.GONE);
                isDisplayed = false;
            }
        });
        shrinkAnimator.start();
    }

    @Override
    public void display() {
        if(displayAnimator != null && displayAnimator.isRunning()){
            return;
        }
        if(isDisplayed){
            return;
        }
        updateParams();
        imageView.setVisibility(View.VISIBLE);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofInt("y", originalY, animY);
        isDisplayed = true;
        displayAnimator =  ValueAnimator.ofPropertyValuesHolder(alpha, pvhY)
                .setDuration(animDuration);
        displayAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        displayAnimator.addUpdateListener(animation -> {

            params.y = (int) animation.getAnimatedValue("y");

            float alphaValue = (float)animation.getAnimatedValue("alpha");
            imageView.setAlpha(alphaValue);
            imageView.setScaleX(alphaValue);
            imageView.setScaleY(alphaValue);
            updateView();
        });
        displayAnimator.start();
    }

    private void updateView(){
        windowManager.updateViewLayout(imageView,params);
    }

    @Override
    public View getView() {
        return imageView;
    }

    private void updateParams() {
        params = (WindowManager.LayoutParams) imageView.getLayoutParams();
    }

    private void createParams()
    {
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowType(), FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        params.dimAmount = 0.7F;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.width = dimen;
        params.height = dimen;
    }

    private int getWindowType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            return WindowManager.LayoutParams.TYPE_PHONE;
        }
    }


}
