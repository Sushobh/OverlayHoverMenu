package com.ranrings.hovercirclelib;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ranrings.circlemenutest.UtilKt;
import com.ranrings.circlemenutest.ViewParent;

import java.util.ArrayList;
import java.util.List;




/**
 * CircleMenuView
 */
public class CircleMenuView extends FrameLayout {





    private static final int DEFAULT_BUTTON_SIZE = 70;
    private static final float DEFAULT_DISTANCE = DEFAULT_BUTTON_SIZE * 1.5f;
    private static final float DEFAULT_RING_SCALE_RATIO = 1.3f;
    private static final float DEFAULT_CLOSE_ICON_ALPHA = 0.3f;
    private final int dimensionSize ;
    private final List<View> mButtons = new ArrayList<>();
    private boolean mClosedState = true;
    private boolean mIsAnimating = false;
    private ImageView mMenuButton;
    private int mDurationRing;
    private int mDurationOpen;
    private int mDurationClose;
    private int mDesiredSize;
    private int mRingRadius;
    private WindowManager mWindowManager;

    private float mDistance;



    private EventListener mListener;

    /**
     * CircleMenu event listener.
     */
    public static class EventListener {

        /**
         * Invoked on button click, after animation end.
         * @param view - current CircleMenuView instance.
         * @param buttonIndex - clicked button zero-based index.
         */
        public void onButtonClicked(@NonNull CircleMenuView view, int buttonIndex) {}

    }

    private class OnButtonClickListener implements OnClickListener {
        @Override
        public void onClick(final View view) {
            if (mIsAnimating) {
                return;
            }
            final Animator click = getButtonClickAnimation(view);
            click.setDuration(mDurationRing);
            click.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {

                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    mClosedState = true;
                    if (mListener != null) {
                        mListener.onButtonClicked(CircleMenuView.this, mButtons.indexOf(view));
                    }
                }
            });
            click.start();
        }
    }




    /**
     * Constructor for creation CircleMenuView in code, not in xml-layout.
     * @param context current context, will be used to access resources.
     * @param icons buttons icons resource ids array. Items must be @DrawableRes.
     */
    private CircleMenuView(@NonNull Context context, int dimensionSize,@NonNull List<Drawable> icons) {
        super(context);
        final float density = context.getResources().getDisplayMetrics().density;
        final float defaultDistance = DEFAULT_DISTANCE * density;
        mDurationRing = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        mDurationOpen = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        mDurationClose = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        this.dimensionSize = dimensionSize;
        mDistance = defaultDistance;
        this.mWindowManager = getWindowManger();
        initLayout(context);
        initMenu(Color.WHITE);
        initButtons(context, icons);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int w = resolveSizeAndState(mDesiredSize, widthMeasureSpec, 0);
        final int h = resolveSizeAndState(mDesiredSize, heightMeasureSpec, 0);
        setMeasuredDimension(w, h);
    }



    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private void initLayout(@NonNull Context context) {
        LayoutInflater.from(context).inflate(R.layout.circle_menu, this, true);

        setWillNotDraw(true);
        setClipChildren(false);
        setClipToPadding(false);

        final float density = context.getResources().getDisplayMetrics().density;
        final float buttonSize = DEFAULT_BUTTON_SIZE * density;

        mRingRadius = (int) (buttonSize + (mDistance - buttonSize / 2));
        mDesiredSize = (int) (mRingRadius * 2 * DEFAULT_RING_SCALE_RATIO);


    }

    private void initMenu(int menuButtonColor) {
        final AnimatorListenerAdapter animListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {

            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mClosedState = !mClosedState;
            }
        };

        mMenuButton = findViewById(R.id.circle_menu_main_button);
       // mMenuButton.setImageDrawable(mainButtonDrawable);
        LayoutParams params = new LayoutParams(dimensionSize,
                dimensionSize);
        params.gravity = Gravity.CENTER;
        mMenuButton.setLayoutParams(params);
        mMenuButton.setScaleType(ImageView.ScaleType.FIT_XY);
        mMenuButton.setOnClickListener(v -> onMainButtonClick(animListener));
        mMenuButton.setVisibility(INVISIBLE);
    }


    private void onMainButtonClick(AnimatorListenerAdapter animListener){
        if (mIsAnimating) {
            return;
        }
        final Animator animation = mClosedState ? getOpenMenuAnimation() : getCloseMenuAnimation();
        animation.setDuration(mClosedState ? mDurationClose : mDurationOpen);
        animation.addListener(animListener);
        animation.start();
    }

    private void initButtons(@NonNull Context context, @NonNull List<Drawable> icons) {
        final int buttonsCount = icons.size();

        for (int i = 0; i < buttonsCount; i++) {
            final ImageView button = new ImageView(context);
            button.setImageDrawable(icons.get(i));
            button.setClickable(true);
            button.setOnClickListener(new OnButtonClickListener());
            button.setScaleX(0);
            button.setScaleY(0);
            button.setScaleType(ImageView.ScaleType.FIT_XY);
            button.setLayoutParams(new LayoutParams(dimensionSize, dimensionSize));
            addView(button);
            mButtons.add(button);
        }
    }

    private void offsetAndScaleButtons(float centerX, float centerY, float angleStep, float offset, float scale) {
        for (int i = 0, cnt = mButtons.size(); i < cnt; i++) {
            final float angle = angleStep * i - 90;
            final float x = (float) Math.cos(Math.toRadians(angle)) * offset;
            final float y = (float) Math.sin(Math.toRadians(angle)) * offset;

            final View button = mButtons.get(i);
            button.setX(centerX + x);
            button.setY(centerY + y);
            button.setScaleX(1.0f * scale);
            button.setScaleY(1.0f * scale);
        }
    }

    private Animator getButtonClickAnimation(final @NonNull View button) {


        PropertyValuesHolder alphaChange = PropertyValuesHolder.ofFloat("alpha",1f,0f);
        PropertyValuesHolder scaleXChange = PropertyValuesHolder.ofFloat("scaleX", 2f, 0f);
        PropertyValuesHolder scaleYChange = PropertyValuesHolder.ofFloat("scaleY",2f,0f);
        ValueAnimator translator = ValueAnimator.
                ofPropertyValuesHolder(alphaChange, scaleXChange,scaleYChange);

        translator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                    button.setScaleY(1f);
                    button.setScaleX(1f);
                    button.setAlpha(1f);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                for (View view: mButtons) {
                    if(view != button){
                        view.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        translator.setDuration(mDurationClose);
        translator.addUpdateListener(animation -> {
            float scaleX1 = (float)animation.getAnimatedValue("scaleX");
            float scaleY1 = (float)animation.getAnimatedValue("scaleY");
            float alpha1 = (float)animation.getAnimatedValue("alpha");
            button.setScaleX(scaleX1);
            button.setScaleY(scaleY1);
            button.setAlpha(alpha1);
        });


        final AnimatorSet firstSet = new AnimatorSet();
        firstSet.playTogether(translator);

        final AnimatorSet result = new AnimatorSet();
        result.play(firstSet);
        result.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;
                bringChildToFront(button);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
            }
        });

        return result;
    }


    private Animator getOpenMenuAnimation() {
        final ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(mMenuButton, "alpha", DEFAULT_CLOSE_ICON_ALPHA);

        final Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
        final Keyframe kf1 = Keyframe.ofFloat(0.5f, 60f);
        final Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
        final PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
        final ObjectAnimator rotateAnimation = ObjectAnimator.ofPropertyValuesHolder(mMenuButton, pvhRotation);
        rotateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private boolean iconChanged = false;
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float fraction = valueAnimator.getAnimatedFraction();
                if (fraction >= 0.5f && !iconChanged) {
                    iconChanged = true;
                }
            }
        });

        final float centerX = mMenuButton.getX();
        final float centerY = mMenuButton.getY();

        final int buttonsCount = mButtons.size();
        final float angleStep = 360f / buttonsCount;

        final ValueAnimator buttonsAppear = ValueAnimator.ofFloat(0f, mDistance);
        buttonsAppear.setInterpolator(new OvershootInterpolator());
        buttonsAppear.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (View view: mButtons) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        });
        buttonsAppear.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float fraction = valueAnimator.getAnimatedFraction();
                final float value = (float)valueAnimator.getAnimatedValue();
                offsetAndScaleButtons(centerX, centerY, angleStep, value, fraction);
            }
        });

        final AnimatorSet result = new AnimatorSet();
        result.playTogether(alphaAnimation, rotateAnimation, buttonsAppear);
        result.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
            }
        });

        return result;
    }

    private Animator getCloseMenuAnimation() {
        final ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(mMenuButton, "scaleX", 0f);
        final ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(mMenuButton, "scaleY", 0f);
        final ObjectAnimator alpha1 = ObjectAnimator.ofFloat(mMenuButton, "alpha", 0f);
        final AnimatorSet set1 = new AnimatorSet();
        set1.playTogether(scaleX1, scaleY1, alpha1);
        set1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (View view: mButtons) {
                    view.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        final ObjectAnimator angle = ObjectAnimator.ofFloat(mMenuButton, "rotation", 0);
        final ObjectAnimator alpha2 = ObjectAnimator.ofFloat(mMenuButton, "alpha", 1f);
        final ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mMenuButton, "scaleX", 1f);
        final ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mMenuButton, "scaleY", 1f);
        final AnimatorSet set2 = new AnimatorSet();
        set2.setInterpolator(new OvershootInterpolator());
        set2.playTogether(angle, alpha2, scaleX2, scaleY2);

        final AnimatorSet result = new AnimatorSet();
        result.play(set1).before(set2);
        result.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
            }
        });
        return result;
    }



    /**
     * @param duration close animation duration in milliseconds.
     */
    public void setDurationClose(int duration) {
        mDurationClose = duration;
    }

    /**
     * @return current close animation duration.
     */
    public int getDurationClose() {
        return mDurationClose;
    }

    /**

     * @param duration open animation duration in milliseconds.
     */
    public void setDurationOpen(int duration) {
        mDurationOpen = duration;
    }

    /**

     * @return current open animation duration.
     */
    public int getDurationOpen() {
        return mDurationOpen;
    }


    /**
     * @param distance in pixels.
     */
    public void setDistance(float distance) {
        mDistance = distance;
        invalidate();
    }

    /**
     * @return current distance in pixels.
     */
    public float getDistance() {
        return mDistance;
    }

    /**
     * See {@link EventListener }
     * @param listener new event listener or null.
     */
    public void setEventListener(@Nullable EventListener listener) {
        mListener = listener;
    }

    /**
     * See {@link EventListener }
     * @return current event listener or null.
     */
    public EventListener getEventListener() {
        return mListener;
    }

    private void openOrClose(boolean open, boolean animate) {
        if (mIsAnimating) {
            return;
        }

        if (open && !mClosedState) {
            return;
        }

        if (!open && mClosedState) {
            return;
        }

        if(open){
            makeTouchable();
        }
        else {
            makeUnTouchable();
        }

        if (animate) {
            mMenuButton.performClick();
        } else {
            mClosedState = !open;

            final float centerX = mMenuButton.getX();
            final float centerY = mMenuButton.getY();

            final int buttonsCount = mButtons.size();
            final float angleStep = 360f / buttonsCount;

            final float offset = open ? mDistance : 0f;
            final float scale = open ? 1f : 0f;

            mMenuButton.setAlpha(open ? DEFAULT_CLOSE_ICON_ALPHA : 1f);

            final int visibility = open ? View.VISIBLE : View.INVISIBLE;
            for (View view: mButtons) {
                view.setVisibility(visibility);
            }

            offsetAndScaleButtons(centerX, centerY, angleStep, offset, scale);
        }
    }

    /**
     * Open menu programmatically
     * @param animate open with animation or not
     */
    public void open(boolean animate) {
        openOrClose(true, animate);
    }

    /**
     * Close menu programmatically
     * @param animate close with animation or not
     */
    public void close(boolean animate) {
        openOrClose(false, animate);
    }

    public boolean isClosed(){
        return mClosedState;
    }



    public EventListener getmListener() {
        return mListener;
    }


    public static CircleMenuView fromDrawableList(Context context,int dimensionSize ,List<Drawable> drwawbles){
        return new CircleMenuView(context,dimensionSize,drwawbles);
    }


    private void moveToCenterOfWindowManager(WindowManager windowManager) {
       ViewParent viewParent =  UtilKt.getParentView(windowManager,this);
       WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)getLayoutParams();
       int newX = (viewParent.getWidth()-mDesiredSize)/2;
       int newY = (viewParent.getHeight() - mDesiredSize)/2;
       layoutParams.x = newX;
       layoutParams.y = newY;
       windowManager.updateViewLayout(this,layoutParams);
    }


    private int getWindowType() {
        int type;
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          type =   WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
           type =  WindowManager.LayoutParams.TYPE_PHONE;
        }
         return type;
    }



    public WindowManager.LayoutParams getParams(){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowType(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        params.dimAmount = 0.7f;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        return params;
    }


    public void makeTouchable(){
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        mWindowManager.updateViewLayout(this,params);
    }

    public void makeUnTouchable(){
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mWindowManager.updateViewLayout(this,params);

    }


    public WindowManager getWindowManger(){
        return (WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE);
    }



    public void init(){
        mWindowManager.addView(this,getParams());
        moveToCenterOfWindowManager(getWindowManger());
        setVisibility(VISIBLE);
    }


    public Point getMainMenuCoord(){
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)getLayoutParams();
        float centerX = layoutParams.x+mMenuButton.getX();
        float centerY = layoutParams.y+mMenuButton.getY();
        return new Point((int)(centerX),(int)(centerY));
    }
}