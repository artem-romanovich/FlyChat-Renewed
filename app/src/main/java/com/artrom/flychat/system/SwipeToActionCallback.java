package com.artrom.flychat.system;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.artrom.flychat.adapters.MessageAdapter;

public class SwipeToActionCallback extends ItemTouchHelper.SimpleCallback {
    private MessageAdapter messageAdapter;

    public SwipeToActionCallback(MessageAdapter messageAdapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        messageAdapter = messageAdapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        Log.i("TAG", "SwipeToActionCallback");
    }
}
