package com.ranrings.hovercirclelib;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.ranrings.circlemenutest.UtilKt;
import com.ranrings.circlemenutest.ViewParent;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;



 class DraggableHoverView extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener {


    private final WindowManager mWindowManager;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private WindowManager.LayoutParams params;
    private final int dimen;
    private final CircleMenuView circleMenuView;
    private final ViewRemover removerView;
    private final CircularMenuDisplay circularMenuDisplay;
    private boolean isMenuOpen = false;
    private boolean isFirstTime = true;

    public void onButtonClickInCircleView() {
        circularMenuDisplay.hideCircleView();
        moveToVerticalEnd();
    }



    public interface CircularMenuDisplay {
           void openCircleView();
           void closeCircleView();
           void shutDown();
           void hideCircleView();
    }

    public DraggableHoverView(Context context,WindowManager mWindowManager ,
                              int dimen,CircularMenuDisplay circularMenuDisplay,
                              CircleMenuView circleMenuView , ViewRemover removerView,Drawable image) {
        super(context);
        this.mWindowManager = mWindowManager;
        this.dimen = dimen;
        this.circularMenuDisplay = circularMenuDisplay;
        this.circleMenuView = circleMenuView;
        this.removerView = removerView;
        setImageDrawable(image);
        setElevation(10f);
    }

    public void init(){
        mWindowManager.addView(this, getNormalParams());
        params = (WindowManager.LayoutParams) getLayoutParams();
        setOnTouchListener(this);
        setScaleType(ScaleType.FIT_CENTER);
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void animateToPoint(Point point,final boolean shouldOpenMenu) {
        WindowManager.LayoutParams currentParams = (WindowManager.LayoutParams) getLayoutParams();
        Point currentPoint = new Point(currentParams.x,currentParams.y);
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofInt("x", currentPoint.x, point.x);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofInt("y", currentPoint.y, point.y);

        ValueAnimator translator = ValueAnimator.ofPropertyValuesHolder(pvhX, pvhY);

        translator.addUpdateListener(valueAnimator -> {
            params.x = (Integer) valueAnimator.getAnimatedValue("x");
            params.y = (Integer) valueAnimator.getAnimatedValue("y");
            mWindowManager.updateViewLayout(DraggableHoverView.this, params);
            if(valueAnimator.getAnimatedFraction() == 1f){
               if(shouldOpenMenu){
                   if(isFirstTime){
                       circularMenuDisplay.closeCircleView();
                       isFirstTime = true;
                   }
                   circularMenuDisplay.openCircleView();
                   isMenuOpen = true;
               }
            }
        });

        translator.setDuration(300);
        translator.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true;
            case MotionEvent.ACTION_UP:

                PointF initialPoint = new PointF(initialTouchX,initialTouchY);
                PointF newPoint = new PointF(event.getRawX(),event.getRawY());
                if(initialPoint.equals(newPoint)){
                    onClick();
                }
                else {
                    if(UtilKt.isViewIntersectingMotionEventClick(removerView.getView(),this)){
                          circularMenuDisplay.shutDown();
                    }
                    else {
                        moveToHorizontalEnd();
                    }
                }
                removerView.remove();
                return true;
            case MotionEvent.ACTION_MOVE:
                removerView.display();
                isMenuOpen = false;
                circularMenuDisplay.closeCircleView();
                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                mWindowManager.updateViewLayout(this, params);
                return true;
        }
        return false;
    }

    public void moveToHorizontalEnd() {
        WindowManager.LayoutParams currentParams = (WindowManager.LayoutParams) getLayoutParams();
        ViewParent viewParent = UtilKt.getParentView(mWindowManager,this);
        if(currentParams.x > viewParent.getWidth()/2){
            animateToPoint(new Point(viewParent.getWidth()-getWidth(),currentParams.y),false);
        }
        else {
            animateToPoint(new Point(0,currentParams.y),false);

        }
    }

    private void moveToVerticalEnd() {
        WindowManager.LayoutParams currentParams = (WindowManager.LayoutParams) getLayoutParams();
        ViewParent viewParent = UtilKt.getParentView(mWindowManager,this);
        animateToPoint(new Point(viewParent.getHeight(),viewParent.getHeight()),false);
    }


    public WindowManager.LayoutParams getNormalParams()
    {
        WindowManager.LayoutParams params =   new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowType(), FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.dimAmount = 0.7F;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.width = dimen;
        params.height = dimen;
        return params;
    }

    private int getWindowType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            return WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    public void onClick(){
        if(isMenuOpen){
            circularMenuDisplay.closeCircleView();
            isMenuOpen = false;
            moveToHorizontalEnd();
        }
        else {
            animateToPoint(circleMenuView.getMainMenuCoord(),true);
        }
    }


}
