
package com.freak.lottery.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Freak
 * @date 2019/12/19.
 */
public interface WheelViewAdapter {
    /**
     * 获取item数量
     *
     * @return
     */
    int getItemsCount();

    /**
     * 获取指定的item
     *
     * @param index       索引值
     * @param convertView 尽可能重用的旧视图
     * @param parent      parent
     * @return
     */
    View getItem(int index, View convertView, ViewGroup parent);

    /**
     * 获取一个视图，该视图显示一个空的车轮项目，该项目位于第一个或最后一个车轮项目之前或之后。
     *
     * @param convertView 尽可能重用的旧视图
     * @param parent      该视图最终将附加到的parent
     * @return the empty item View
     */
    View getEmptyItem(View convertView, ViewGroup parent);

    /**
     * 注册观察者
     *
     * @param observer DataSetObserver
     */
    void registerDataSetObserver(DataSetObserver observer);

    /**
     * 解绑观察者
     *
     * @param observer DataSetObserver
     */
    void unregisterDataSetObserver(DataSetObserver observer);
}
