package com.freak.lottery;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.freak.lottery.adapters.AbstractWheelAdapter;
import com.freak.lottery.widget.OnWheelScrollListener;
import com.freak.lottery.widget.WheelView;

import java.util.List;

/**
 * @author Freak
 * @date 2019/12/19.
 */
public class WheelManager {
    private static final String TAG = "WheelManager";
    private static WheelManager wheelManager;

    public static WheelManager getInstance() {
        if (wheelManager == null) {
            synchronized (WheelManager.class) {
                if (wheelManager == null) {
                    wheelManager = new WheelManager();
                }
            }
        }
        return wheelManager;
    }


    /**
     * 初始化滚轮
     *
     * @param abstractWheelAdapter  适配器
     * @param wheelViewList         设置的滚轮列表
     * @param visibleItem           可见的item数量
     * @param finishWheelView       结束的滚轮
     * @param cyclic                是否循环滚动
     * @param enabled               是否可点击
     * @param drawShadows           是否绘制阴影
     * @param onWheelScrollListener 滚动监听
     */
    public void initWheel(AbstractWheelAdapter abstractWheelAdapter, List<WheelView> wheelViewList, int visibleItem, WheelView finishWheelView, boolean cyclic, boolean enabled, boolean drawShadows, OnWheelScrollListener onWheelScrollListener) {
        for (WheelView wheelView : wheelViewList) {
            wheelView.setViewAdapter(abstractWheelAdapter);
            wheelView.setVisibleItems(visibleItem);
            if (wheelView == finishWheelView) {
                wheelView.addScrollingListener(onWheelScrollListener);
            }
            wheelView.setCyclic(cyclic);
            wheelView.setEnabled(enabled);
            wheelView.setDrawShadows(drawShadows);
        }
    }


    /**
     * 开始滚动
     *
     * @param result        滚动结果
     * @param wheelViewList 需要滚动的WheelView
     * @param roundList     滚动距离列表
     * @param timeList      持续时间列表
     */
    public void start(@NonNull String result, @NonNull List<WheelView> wheelViewList, @NonNull List<Integer> roundList, List<Integer> timeList) {
        if (TextUtils.isEmpty(result)) {
            throw new NullPointerException("result不能为空");
        }
        if (wheelViewList.size() <= 0) {
            throw new NullPointerException("wheelViewList长度要大于0");
        }
        if (roundList.size() <= 0) {
            throw new NullPointerException("roundList长度要大于0");
        }
        if (timeList.size() <= 0) {
            throw new NullPointerException("timeList长度要大于0");
        }

        int resultCount = result.length() > wheelViewList.size() ? wheelViewList.size() : result.length();
        if (wheelViewList.size() != roundList.size() || wheelViewList.size() != timeList.size()) {
            throw new RuntimeException("roundList和timeList长度要与wheelViewList长度一致");
        }
        for (int i = 0; i < resultCount; i++) {
            char num = result.charAt(i);
            scroll(wheelViewList.get(i), roundList.get(i) + Integer.parseInt(String.valueOf(num)), timeList.get(i));
        }
    }

    public void scroll(WheelView wheelView, int round, int time) {
        wheelView.setCurrentItem(0);
        wheelView.scroll(round, time);
    }
}
