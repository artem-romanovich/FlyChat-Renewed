package com.artrom.flychat.system;

public interface ItemMoveHelperApi {
    /**
     * Положение переключателя позиции
     *
     * @param fromPosition начальная позиция
     * @param toPosition   конечная позиция
     */
    void onItemMoved(int fromPosition, int toPosition);

    /**
     * Начать движение
     */
    void onMoveStart();

    /**
     * Прекратить движение
     */
    void onMoveEnd();
}
