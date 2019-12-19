

package com.freak.lottery.widget;

/**
 * Wheel滚动监听
 *
 * @author Freak
 * @date 2019/12/19.
 */
public interface OnWheelScrollListener {
    /**
     * 开始滚动
     *
     * @param wheel 开始滚动的WheelView
     */
    void onScrollingStarted(WheelView wheel);

    /**
     * 滚动结束
     *
     * @param wheel 滚动结束的WheelView
     */
    void onScrollingFinished(WheelView wheel);
}
