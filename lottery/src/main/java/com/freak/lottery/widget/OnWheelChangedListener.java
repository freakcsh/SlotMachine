

package com.freak.lottery.widget;


/**
 * 滚轮监听器。
 * 每当更改当前滚轮位置时，都会调用onChanged（）方法：
 * 设置了新的滚轮位置
 * 滚轮视图已滚动
 *
 * @author Freak
 * @date 2019/12/19.
 */
public interface OnWheelChangedListener {
    /**
     * 每当更改当前wheel位置时，都会调用onChanged（）方法：
     *
     * @param wheel    当前转动的wheel
     * @param oldValue 旧的值
     * @param newValue 新的值
     */
    void onChanged(WheelView wheel, int oldValue, int newValue);
}
