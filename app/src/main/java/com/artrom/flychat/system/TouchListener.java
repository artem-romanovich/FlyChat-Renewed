package com.artrom.flychat.system;

import android.util.Log;

public interface TouchListener  {
    void onSingleTap();
    default void onDoubleTap() {
        Log.i("TAG",  "Double tap");
    }
    default void onLongPress() {
        Log.i("TAG", "Long press");
    }
    default void onSwipeLeft() {
        Log.i("TAG", "Swipe left");
    }
    default void onSwipeRight() {
        Log.i("TAG", "Swipe right");
    }
    default void onSwipeUp() {
        Log.i("TAG", "Swipe up");
    }
    default void onSwipeDown() {
        Log.i("TAG", "Swipe down");
    }
}
