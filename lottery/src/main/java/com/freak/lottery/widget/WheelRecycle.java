
package com.freak.lottery.widget;

import android.view.View;
import android.widget.LinearLayout;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Freak
 * @date 2019/12/19.
 */
public class WheelRecycle {
    // 缓存 items
    private List<View> items;

    // 缓存空 items
    private List<View> emptyItems;

    // Wheel view
    private WheelView wheel;

    public WheelRecycle(WheelView wheel) {
        this.wheel = wheel;
    }

    /**
     * 从特定布局回收的物品。
     * 仅保存未包括在指定范围内的item。
     * 所有缓存的item将从原始布局中删除。
     *
     * @param layout    包含要缓存item的layout
     * @param firstItem 布局中第一项的数量
     * @param range     当前item的范围
     * @return 返回第一个item编号的新值
     */
    public int recycleItems(LinearLayout layout, int firstItem, ItemsRange range) {
        int index = firstItem;
        for (int i = 0; i < layout.getChildCount(); ) {
            if (!range.contains(index)) {
                recycleView(layout.getChildAt(i), index);
                layout.removeViewAt(i);
                if (i == 0) { // first item
                    firstItem++;
                }
            } else {
                i++; // go to next item
            }
            index++;
        }
        return firstItem;
    }


    public View getItem() {
        return getCachedView(items);
    }


    public View getEmptyItem() {
        return getCachedView(emptyItems);
    }


    public void clearAll() {
        if (items != null) {
            items.clear();
        }
        if (emptyItems != null) {
            emptyItems.clear();
        }
    }


    private List<View> addView(View view, List<View> cache) {
        if (cache == null) {
            cache = new LinkedList<View>();
        }

        cache.add(view);
        return cache;
    }


    private void recycleView(View view, int index) {
        int count = wheel.getViewAdapter().getItemsCount();

        if ((index < 0 || index >= count) && !wheel.isCyclic()) {
            // empty view
            emptyItems = addView(view, emptyItems);
        } else {
            while (index < 0) {
                index = count + index;
            }
            index %= count;
            items = addView(view, items);
        }
    }


    private View getCachedView(List<View> cache) {
        if (cache != null && cache.size() > 0) {
            View view = cache.get(0);
            cache.remove(0);
            return view;
        }
        return null;
    }

}
