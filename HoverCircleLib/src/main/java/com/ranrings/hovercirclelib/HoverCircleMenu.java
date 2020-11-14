package com.ranrings.hovercirclelib;

import android.app.Service;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.List;

public class HoverCircleMenu {


    public interface ButtonClickListener {
        void buttonClicked(int index);
        void onRemoved();
    }

    private int dimen;
    private List<Drawable> buttonIcons;
    private Drawable mainButtonDrawable;
    private Drawable removeButtonDrawable;
    private WindowManager mWindowManager;
    private CircleMenuView circleMenuView;
    private DraggableHoverView draggableHoverView;
    private DragViewRemover dragViewRemover;
    private ButtonClickListener buttonClickListener;
    private Context context;


    private HoverCircleMenu(){

    }

    public void start(){
         initDragRemover();
         initCircleView();
         initDraggableView();
    }

    public void remove(){
        mWindowManager.removeViewImmediate(draggableHoverView);
        mWindowManager.removeViewImmediate(dragViewRemover.getView());
        mWindowManager.removeViewImmediate(circleMenuView);
    }

    private void initDragRemover(){
        dragViewRemover = new DragViewRemover(removeButtonDrawable,context,dimen,mWindowManager);
        dragViewRemover.init();
    }


    private void initCircleView(){
        circleMenuView = CircleMenuView.fromDrawableList(context,dimen,buttonIcons,mainButtonDrawable);
        circleMenuView.init(mWindowManager);
        circleMenuView.setEventListener(new CircleMenuView.EventListener(){
            @Override
            public void onButtonClicked(@NonNull CircleMenuView view, int buttonIndex) {
                draggableHoverView.onButtonClickInCircleView();
                buttonClickListener.buttonClicked(buttonIndex);
            }
        });
    }

    private void initDraggableView(){

         DraggableHoverView.CircularMenuDisplay circularMenuDisplay = new DraggableHoverView.CircularMenuDisplay() {
             @Override
             public void openCircleView() {
                 circleMenuView.setVisibility(View.VISIBLE);
                 circleMenuView.open(true);
             }

             @Override
             public void closeCircleView() {
                   circleMenuView.close(true);
                   circleMenuView.setVisibility(View.GONE);
             }

             @Override
             public void shutDown() {
                    buttonClickListener.onRemoved();
             }

             @Override
             public void hideCircleView() {
                 circleMenuView.setVisibility(View.GONE);
             }
         };
         draggableHoverView = new DraggableHoverView(context,mWindowManager,dimen,
                 circularMenuDisplay,circleMenuView,dragViewRemover,mainButtonDrawable);
         draggableHoverView.init();
    }

    public static class Builder {


        private final HoverCircleMenu hoverCircleMenu;

        public Builder(){
            this.hoverCircleMenu = new HoverCircleMenu();
        }

        public Builder setDimen(int dimen){
            this.hoverCircleMenu.dimen = dimen;
            return this;
        }

        public Builder setButtonIcons(List<Drawable> buttonIcons){
            this.hoverCircleMenu.buttonIcons = buttonIcons;
            return this;
        }

        public Builder setMainButton(Drawable drawable){
            this.hoverCircleMenu.mainButtonDrawable = drawable;
            return this;
        }

        public Builder setRemoveButton(Drawable drawable){
            this.hoverCircleMenu.removeButtonDrawable = drawable;
            return this;
        }

        public Builder setButtonClickListener(ButtonClickListener buttonClickListener){
            this.hoverCircleMenu.buttonClickListener = buttonClickListener;
            return this;
        }




        public HoverCircleMenu build(Context context){
            this.hoverCircleMenu.context = context;
            this.hoverCircleMenu.mWindowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
            return hoverCircleMenu;
        }

    }

}
