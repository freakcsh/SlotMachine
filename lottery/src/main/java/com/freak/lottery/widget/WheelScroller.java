
package com.freak.lottery.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * 处理滚动事件
 *
 * @author Freak
 * @date 2019/12/19.
 */
public class WheelScroller {
    public interface ScrollingListener {
        /**
         * 正在滚动
         *
         * @param distance 滚动距离
         */
        void onScroll(int distance);

        /**
         * 开始滚动
         */
        void onStarted();

        /**
         * 滚动结束 在调用onJustify()完成后才会回调
         */
        void onFinished();

        /**
         * 滚动结束时，调用justify回调来对齐视图
         */
        void onJustify();
    }

    /**
     * 滚动事件
     */
    private static final int SCROLLING_DURATION = 400;

    /**
     * 最小的滚动增量
     */
    public static final int MIN_DELTA_FOR_SCROLLING = 1;

    private ScrollingListener listener;

    private Context context;

    private GestureDetector gestureDetector;
    private Scroller scroller;
    private int lastScrollY;
    private float lastTouchedY;
    private boolean isScrollingPerformed;

    /**
     * @param context  context
     * @param listener ScrollingListener
     */
    public WheelScroller(Context context, ScrollingListener listener) {
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);

        scroller = new Scroller(context);

        this.listener = listener;
        this.context = context;
    }

    /**
     * 设置Interpolator
     * 插值器定义动画的变化率。 这允许基本的动画效果（alpha，缩放，平移，旋转）被加速，减速，重复等。
     *
     * @param interpolator interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        scroller.forceFinished(true);
        scroller = new Scroller(context, interpolator);
    }

    /**
     * 滚动
     *
     * @param distance 滚动距离
     * @param time     滚动持续时间
     */
    public void scroll(int distance, int time) {
        scroller.forceFinished(true);

        lastScrollY = 0;

        scroller.startScroll(0, 0, 0, distance, time != 0 ? time : SCROLLING_DURATION);
        setNextMessage(MESSAGE_SCROLL);

        startScrolling();
    }

    /**
     * 停止滚动
     */
    public void stopScrolling() {
        scroller.forceFinished(true);
    }

    /**
     * 触摸时间
     *
     * @param event event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchedY = event.getY();
                scroller.forceFinished(true);
                clearMessages();
                break;

            case MotionEvent.ACTION_MOVE:
                int distanceY = (int) (event.getY() - lastTouchedY);
                if (distanceY != 0) {
                    startScrolling();
                    listener.onScroll(distanceY);
                    lastTouchedY = event.getY();
                }
                break;
        }

        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {
            justify();
        }

        return true;
    }

    // 手势监听器
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //请在onTouchEvent（）中滚动，因为当用户触摸并移动滚轮时不会立即调用onScroll（）
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            lastScrollY = 0;
            final int maxY = 0x7FFFFFFF;
            final int minY = -maxY;
            scroller.fling(0, lastScrollY, 0, (int) -velocityY, 0, 0, minY, maxY);
            setNextMessage(MESSAGE_SCROLL);
            return true;
        }
    };

    private final int MESSAGE_SCROLL = 0;
    private final int MESSAGE_JUSTIFY = 1;

    /**
     * 将下一条消息设置为排队。 之前清除队列。
     *
     * @param message message
     */
    private void setNextMessage(int message) {
        clearMessages();
        animationHandler.sendEmptyMessage(message);
    }

    /**
     * 清除消息
     */
    private void clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }

    // 处理动画的 handler
    @SuppressLint("HandlerLeak")
    private Handler animationHandler = new Handler() {
        public void handleMessage(Message msg) {
            scroller.computeScrollOffset();
            int currY = scroller.getCurrY();
            int delta = lastScrollY - currY;
            lastScrollY = currY;
            if (delta != 0) {
                listener.onScroll(delta);
            }

            // 最终的Y滚动尚未完成，请手动完成
            if (Math.abs(currY - scroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING) {
                currY = scroller.getFinalY();
                scroller.forceFinished(true);
            }
            if (!scroller.isFinished()) {
                animationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {
                justify();
            } else {
                finishScrolling();
            }
        }
    };

    /**
     * 对齐 wheel
     */
    private void justify() {
        listener.onJustify();
        setNextMessage(MESSAGE_JUSTIFY);
    }

    /**
     * 开始滚动
     */
    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            listener.onStarted();
        }
    }

    /**
     * 结束滚动
     */
    private void finishScrolling() {
        if (isScrollingPerformed) {
            listener.onFinished();
            isScrollingPerformed = false;
        }
    }
}
