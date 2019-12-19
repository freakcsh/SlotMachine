

package com.freak.lottery.widget;

/**
 * 滚轮单击的侦听器界面。
 * 每当单击滚轮项目时，都会调用onItemClicked（）方法
 * 设置了新的车轮位置
 * 滚轮视图已滚动
 *
 * @author Freak
 * @date 2019/12/19.
 */

public interface OnWheelClickedListener {
    /**
     * Callback method to be invoked when current item clicked
     *
     * @param wheel     the wheel view
     * @param itemIndex the index of clicked item
     */
    void onItemClicked(WheelView wheel, int itemIndex);
}
