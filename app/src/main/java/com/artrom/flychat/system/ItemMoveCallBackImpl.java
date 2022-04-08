package com.artrom.flychat.system;


import android.annotation.SuppressLint;
import android.graphics.Canvas;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ItemMoveCallBackImpl extends ItemTouchHelper.Callback {

    private ItemMoveHelperApi mHelperApi; // Интерфейс обратного вызова мобильного API

    private boolean mDragEnabled = true; // Можете ли вы переключить позицию долгим нажатием

    private int mDragStartPosition; // Начальная позиция, которую можно перетащить

    private int mDragEndPosition; // Конечная позиция, которую можно перетащить

    private boolean mDragEndPositionFlag; // Установлена ​​ли конечная позиция перетаскивания

    public ItemMoveCallBackImpl(ItemMoveHelperApi helperApi) {
        this.mHelperApi = helperApi;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return mDragEnabled;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (mHelperApi != null) {
            mHelperApi.onItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (mHelperApi == null) {
            super.onSelectedChanged(viewHolder, actionState);
            return;
        }
        if (viewHolder == null) {
            mHelperApi.onMoveEnd();
        } else {
            mHelperApi.onMoveStart();
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        dY = getLimitedDy(recyclerView, viewHolder, dY);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    /**
     * Получите значение DY, ограниченное в RecyclerView в соответствии с направлением и условиями
     *
     * @param recyclerView список
     * @param viewHolder   перетащите ViewHolder
     * @param dY           Значение DY до ограничения
     * @return значение DY после ограничения
     */
    @SuppressLint("WrongConstant")
    private float getLimitedDy(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dY) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)
                || ((LinearLayoutManager) layoutManager).getOrientation() != OrientationHelper.VERTICAL) {
            return dY;
        }
        int position = viewHolder.getLayoutPosition();
        mDragEndPosition = mDragEndPositionFlag ?
                mDragStartPosition : recyclerView.getAdapter().getItemCount() - 1;
        if (position == mDragStartPosition) {
            return dY < 0 ? 0 : dY;
        } else if (position == mDragEndPosition) {
            return dY > 0 ? 0 : dY;
        }
        return dY;
    }

    /**
     * Установите, нужно ли перетаскивать
     */
    public void setDragEnabled(boolean dragEnabled) {
        mDragEnabled = dragEnabled;
    }

    /**
     * Установите начальную позицию перетаскивания
     *
     * @param dragStartPosition начальная позиция
     */
    public void setDragStartPosition(int dragStartPosition) {
        mDragStartPosition = dragStartPosition;
    }

    /**
     * Установите конечное положение перетаскивания
     *
     * @param dragEndPosition конечная позиция
     */
    public void setDragEndPosition(int dragEndPosition) {
        mDragEndPositionFlag = true;
        mDragEndPosition = dragEndPosition;
    }

    /**
     * Очистить метку фиксированного конечного положения
     */
    public void clearDragEndPosition() {
        mDragEndPositionFlag = false;
    }
}
