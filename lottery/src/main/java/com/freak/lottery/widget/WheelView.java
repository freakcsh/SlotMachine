/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.freak.lottery.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import com.freak.lottery.R;
import com.freak.lottery.adapters.WheelViewAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * 数字WheelView
 *
 * @author Freak
 * @date 2019/12/19.
 */
public class WheelView extends View {

    /**
     * 顶部和底部阴影颜色
     */
    private int[] SHADOWS_COLORS = new int[]{0xefE9E9E9,
            0xcfE9E9E9, 0x3fE9E9E9};

    /**
     * 顶部和底部的item偏移
     */
    private static final int ITEM_OFFSET_PERCENT = 0;

    /**
     * Left 和 right padding值
     */
    private static final int PADDING = 0;

    /**
     * 可见item的计数
     */
    private static final int DEF_VISIBLE_ITEMS = 5;

    // Wheel Values
    private int currentItem = 0;

    // 可见item数
    private int visibleItems = DEF_VISIBLE_ITEMS;

    // Item 高
    private int itemHeight = 0;

    // 中心线
    private Drawable centerDrawable;

    // Wheel drawables
    private int wheelBackground = R.drawable.wheel_bg;
    private int wheelForeground = R.drawable.wheel_val;

    // 阴影drawable
    private GradientDrawable topShadow;
    private GradientDrawable bottomShadow;

    // 绘制阴影
    private boolean drawShadows = true;

    // Scrolling
    private WheelScroller scroller;
    private boolean isScrollingPerformed;
    private int scrollingOffset;

    // 是否循环
    boolean isCyclic = false;

    // Items layout
    private LinearLayout itemsLayout;

    // 第一个item的数量
    private int firstItem;

    // 适配器
    private WheelViewAdapter viewAdapter;

    // Recycle
    private WheelRecycle recycle = new WheelRecycle(this);

    // Listeners
    private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
    private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();
    private List<OnWheelClickedListener> clickingListeners = new LinkedList<OnWheelClickedListener>();


    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData(context);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
    }

    public WheelView(Context context) {
        super(context);
        initData(context);
    }

    /**
     * 初始化
     *
     * @param context context
     */
    private void initData(Context context) {
        scroller = new WheelScroller(getContext(), scrollingListener);
    }

    // 滚动监听
    WheelScroller.ScrollingListener scrollingListener = new WheelScroller.ScrollingListener() {
        @Override
        public void onStarted() {
            isScrollingPerformed = true;
            notifyScrollingListenersAboutStart();
        }

        @Override
        public void onScroll(int distance) {
            doScroll(distance);

            int height = getHeight();
            if (scrollingOffset > height) {
                scrollingOffset = height;
                scroller.stopScrolling();
            } else if (scrollingOffset < -height) {
                scrollingOffset = -height;
                scroller.stopScrolling();
            }
        }

        @Override
        public void onFinished() {
            if (isScrollingPerformed) {
                notifyScrollingListenersAboutEnd();
                isScrollingPerformed = false;
            }

            scrollingOffset = 0;
            invalidate();
        }

        @Override
        public void onJustify() {
            if (Math.abs(scrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
                scroller.scroll(scrollingOffset, 0);
            }
        }
    };

    /**
     * 设置动画Interpolator
     *
     * @param interpolator the interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        scroller.setInterpolator(interpolator);
    }

    /**
     * 获取可见item的数量
     *
     * @return 可见item的数量
     */
    public int getVisibleItems() {
        return visibleItems;
    }

    /**
     * 设置所需的可见项计数。 可见项目的实际数量取决于滚轮布局参数。
     * 要应用更改并重建视图，请调用measure（）。
     *
     * @param count 可见item的数量
     */
    public void setVisibleItems(int count) {
        visibleItems = count;
    }

    /**
     * 获取适配器WheelViewAdapter
     *
     * @return WheelViewAdapter
     */
    public WheelViewAdapter getViewAdapter() {
        return viewAdapter;
    }

    // Adapter listener
    private DataSetObserver dataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            invalidateWheel(false);
        }

        @Override
        public void onInvalidated() {
            invalidateWheel(true);
        }
    };

    /**
     * 设置视图适配器。
     * 通常，新适配器包含不同的视图，因此它需要通过调用measure（）来重建视图。
     *
     * @param viewAdapter the view adapter
     */
    public void setViewAdapter(WheelViewAdapter viewAdapter) {
        if (this.viewAdapter != null) {
            this.viewAdapter.unregisterDataSetObserver(dataObserver);
        }
        this.viewAdapter = viewAdapter;
        if (this.viewAdapter != null) {
            this.viewAdapter.registerDataSetObserver(dataObserver);
        }

        invalidateWheel(true);
    }

    /**
     * 添加wheel改变监听器
     *
     * @param listener OnWheelChangedListener
     */
    public void addChangingListener(OnWheelChangedListener listener) {
        changingListeners.add(listener);
    }

    /**
     * 移除wheel改变监听器
     *
     * @param listener OnWheelChangedListener
     */
    public void removeChangingListener(OnWheelChangedListener listener) {
        changingListeners.remove(listener);
    }

    /**
     * 更新滚轮变化
     *
     * @param oldValue 旧的值
     * @param newValue 新的值
     */
    protected void notifyChangingListeners(int oldValue, int newValue) {
        for (OnWheelChangedListener listener : changingListeners) {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    /**
     * 添加wheel滚动监听器
     *
     * @param listener OnWheelScrollListener
     */
    public void addScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.add(listener);
    }

    /**
     * 移除wheel滚轮监听器
     *
     * @param listener OnWheelScrollListener
     */
    public void removeScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.remove(listener);
    }

    /**
     * 更新滚轮开始滚动的信息
     */
    protected void notifyScrollingListenersAboutStart() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingStarted(this);
        }
    }

    /**
     * 更新滚轮滚动结束的信息
     */
    protected void notifyScrollingListenersAboutEnd() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingFinished(this);
        }
    }

    /**
     * 添加滚轮点击监听
     *
     * @param listener OnWheelClickedListener
     */
    public void addClickingListener(OnWheelClickedListener listener) {
        clickingListeners.add(listener);
    }

    /**
     * 移除滚轮点击监听
     *
     * @param listener the listener
     */
    public void removeClickingListener(OnWheelClickedListener listener) {
        clickingListeners.remove(listener);
    }

    /**
     * 更新滚轮点击信息
     */
    protected void notifyClickListenersAboutClick(int item) {
        for (OnWheelClickedListener listener : clickingListeners) {
            listener.onItemClicked(this, item);
        }
    }

    /**
     * 获取当前的值
     *
     * @return
     */
    public int getCurrentItem() {
        return currentItem;
    }

    /**
     * 设置当前item。
     * 索引错误时不执行任何操作。
     *
     * @param index    item索引
     * @param animated 动画标志
     */
    public void setCurrentItem(int index, boolean animated) {
        if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
            return; // throw?
        }

        int itemCount = viewAdapter.getItemsCount();
        if (index < 0 || index >= itemCount) {
            if (isCyclic) {
                while (index < 0) {
                    index += itemCount;
                }
                index %= itemCount;
            } else {
                return;
            }
        }
        if (index != currentItem) {
            if (animated) {
                int itemsToScroll = index - currentItem;
                if (isCyclic) {
                    int scroll = itemCount + Math.min(index, currentItem) - Math.max(index, currentItem);
                    if (scroll < Math.abs(itemsToScroll)) {
                        itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
                    }
                }
                scroll(itemsToScroll, 0);
            } else {
                scrollingOffset = 0;

                int old = currentItem;
                currentItem = index;

                notifyChangingListeners(old, currentItem);

                invalidate();
            }
        }
    }

    /**
     * 设置不带动画的当前item。
     * 索引错误时不执行任何操作。
     * 当第一次滚动出结果后，再次滚动是一定要重置起始滚动位置，滚动起始位置都是从0开始
     * 所以多次滚动的时候设置起始滚动的值为0即可，否则滚动结果不准确
     *
     * @param index 索引值
     */
    public void setCurrentItem(int index) {
        setCurrentItem(index, false);
    }

    /**
     * 测试滚轮是否为循环滚动。
     * 这意味着在第一项之前显示了最后一项
     *
     * @return true ：滚轮是循环滚动，反之不是
     */
    public boolean isCyclic() {
        return isCyclic;
    }

    /**
     * 设置是否是循环滚动
     *
     * @param isCyclic 是否是循环滚动
     */
    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
        invalidateWheel(false);
    }

    /**
     * 是否绘制阴影
     *
     * @return true ：绘制阴影，反之不绘制
     */
    public boolean drawShadows() {
        return drawShadows;
    }

    /**
     * 设置是否绘制阴影
     *
     * @param drawShadows 是否绘制阴影
     */
    public void setDrawShadows(boolean drawShadows) {
        this.drawShadows = drawShadows;
    }

    /**
     * 设置阴影渐变色
     *
     * @param start  开始颜色
     * @param middle 过度颜色
     * @param end    结束颜色
     */
    public void setShadowColor(int start, int middle, int end) {
        SHADOWS_COLORS = new int[]{start, middle, end};
    }

    /**
     * 绘制滚轮背景颜色
     *
     * @param resource
     */
    public void setWheelBackground(int resource) {
        wheelBackground = resource;
        setBackgroundResource(wheelBackground);
    }

    /**
     * 绘制Foreground
     *
     * @param resource
     */
    public void setWheelForeground(int resource) {
        wheelForeground = resource;
        centerDrawable = getContext().getResources().getDrawable(wheelForeground);
    }

    /**
     * 使滚轮失效
     *
     * @param clearCaches 如果为true，则将清除缓存的视图
     */
    public void invalidateWheel(boolean clearCaches) {
        if (clearCaches) {
            recycle.clearAll();
            if (itemsLayout != null) {
                itemsLayout.removeAllViews();
            }
            scrollingOffset = 0;
        } else if (itemsLayout != null) {
            // 缓存所有的item
            recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
        }

        invalidate();
    }

    /**
     * 初始化资源
     */
    private void initResourcesIfNecessary() {
        if (centerDrawable == null) {
            centerDrawable = getContext().getResources().getDrawable(wheelForeground);
        }

        if (topShadow == null) {
            topShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        }

        if (bottomShadow == null) {
            bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
        }

        setBackgroundResource(wheelBackground);
    }

    /**
     * 计算所需的布局高度
     *
     * @param layout LinearLayout
     * @return 所需布局高度
     */
    private int getDesiredHeight(LinearLayout layout) {
        if (layout != null && layout.getChildAt(0) != null) {
            itemHeight = layout.getChildAt(0).getMeasuredHeight();
        }

        int desired = itemHeight * visibleItems - itemHeight * ITEM_OFFSET_PERCENT / 50;
        //System.out.println(desired+"...................");
        return Math.max(desired, getSuggestedMinimumHeight());
    }

    /**
     * 获取滚轮item高度
     *
     * @return item高度
     */
    private int getItemHeight() {
        if (itemHeight != 0) {
            return itemHeight;
        }

        if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
            itemHeight = itemsLayout.getChildAt(0).getHeight();
            return itemHeight;
        }

        return getHeight() / visibleItems;
    }

    /**
     * 计算控件宽度并创建文本布局
     *
     * @param widthSize 布局宽度
     * @param mode      布局模式
     * @return 计算出控件宽度
     */
    private int calculateLayoutWidth(int widthSize, int mode) {
        initResourcesIfNecessary();

        // TODO: make it static
        itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        itemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int width = itemsLayout.getMeasuredWidth();

        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width += 2 * PADDING;

            width = Math.max(width, getSuggestedMinimumWidth());

            if (mode == MeasureSpec.AT_MOST && widthSize < width) {
                width = widthSize;
            }
        }

        itemsLayout.measure(MeasureSpec.makeMeasureSpec(width - 2 * PADDING, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        return width;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        buildViewForMeasuring();

        int width = calculateLayoutWidth(widthSize, widthMode);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getDesiredHeight(itemsLayout);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layout(r - l, b - t);
    }

    /**
     * 设置布局宽度和高度
     *
     * @param width  宽度
     * @param height 高度
     */
    private void layout(int width, int height) {
        int itemsWidth = width - 2 * PADDING;

        itemsLayout.layout(0, 0, itemsWidth, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
            updateView();

            drawItems(canvas);
            drawCenterRect(canvas);
        }

        if (drawShadows) drawShadows(canvas);
    }

    /**
     * 在控件的顶部和底部绘制阴影
     *
     * @param canvas the canvas for drawing
     */
    private void drawShadows(Canvas canvas) {
        int itemCount = getVisibleItems();
        double ratio = 0.0;
        if (itemCount <= 1) {
            ratio = 0;
        } else if (itemCount <= 3) {
            ratio = 0;
        } else {
            ratio = 0;
        }
        // int height = (int)(1.5 *getItemHeight());
        int height = (int) (ratio * getItemHeight());

        topShadow.setBounds(0, 0, getWidth(), height);
        topShadow.draw(canvas);

        bottomShadow
                .setBounds(0, getHeight() - height, getWidth(), getHeight());
        bottomShadow.draw(canvas);
    }

    /**
     * 绘制item
     *
     * @param canvas Canvas
     */
    private void drawItems(Canvas canvas) {
        canvas.save();

        int top = (currentItem - firstItem) * getItemHeight() + (getItemHeight() - getHeight()) / 2;
        canvas.translate(PADDING, -top + scrollingOffset);

        itemsLayout.draw(canvas);

        canvas.restore();
    }

    /**
     * 绘制矩形
     *
     * @param canvas Canvas
     */
    private void drawCenterRect(Canvas canvas) {
        int center = getHeight() / 2;
        int offset = (int) (getItemHeight() / 2 * 1.2);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#C7C7C7"));
        // 设置线宽
        paint.setStrokeWidth((float) 3);
        // 绘制上边直线
        canvas.drawLine(0, center - offset, getWidth(), center - offset, paint);
        // 绘制下边直线
        canvas.drawLine(0, center + offset, getWidth(), center + offset, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || getViewAdapter() == null) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!isScrollingPerformed) {
                    int distance = (int) event.getY() - getHeight() / 2;
                    if (distance > 0) {
                        distance += getItemHeight() / 2;
                    } else {
                        distance -= getItemHeight() / 2;
                    }
                    int items = distance / getItemHeight();
                    if (items != 0 && isValidItemIndex(currentItem + items)) {
                        notifyClickListenersAboutClick(currentItem + items);
                    }
                }
                break;
        }

        return scroller.onTouchEvent(event);
    }

    /**
     * 滚动滚轮
     *
     * @param delta 滚动值
     */
    private void doScroll(int delta) {
        scrollingOffset += delta;

        int itemHeight = getItemHeight();
        int count = scrollingOffset / itemHeight;

        int pos = currentItem - count;
        int itemCount = viewAdapter.getItemsCount();

        int fixPos = scrollingOffset % itemHeight;
        if (Math.abs(fixPos) <= itemHeight / 2) {
            fixPos = 0;
        }
        if (isCyclic && itemCount > 0) {
            if (fixPos > 0) {
                pos--;
                count++;
            } else if (fixPos < 0) {
                pos++;
                count--;
            }
            // 旋转固定位置
            while (pos < 0) {
                pos += itemCount;
            }
            pos %= itemCount;
        } else {
            //
            if (pos < 0) {
                count = currentItem;
                pos = 0;
            } else if (pos >= itemCount) {
                count = currentItem - itemCount + 1;
                pos = itemCount - 1;
            } else if (pos > 0 && fixPos > 0) {
                pos--;
                count++;
            } else if (pos < itemCount - 1 && fixPos < 0) {
                pos++;
                count--;
            }
        }

        int offset = scrollingOffset;
        if (pos != currentItem) {
            setCurrentItem(pos, false);
        } else {
            invalidate();
        }

        // 更新偏移量
        scrollingOffset = offset - count * itemHeight;
        if (scrollingOffset > getHeight()) {
            scrollingOffset = scrollingOffset % getHeight() + getHeight();
        }
    }

    /**
     * 滚动
     *
     * @param time 滚动持续时间
     */
    public void scroll(int itemsToScroll, int time) {
        int distance = itemsToScroll * getItemHeight() - scrollingOffset;
        scroller.scroll(distance, time);
    }

    /**
     * 计算滚轮的范围
     *
     * @return 项目范围
     */
    private ItemsRange getItemsRange() {
        if (getItemHeight() == 0) {
            return null;
        }

        int first = currentItem;
        int count = 1;

        while (count * getItemHeight() < getHeight()) {
            first--;
            count += 2; // top + bottom items
        }

        if (scrollingOffset != 0) {
            if (scrollingOffset > 0) {
                first--;
            }
            count++;

            // process empty items above the first or below the second
            int emptyItems = scrollingOffset / getItemHeight();
            first -= emptyItems;
            count += Math.asin(emptyItems);
        }
        return new ItemsRange(first, count);
    }

    /**
     * 必要时重建滚轮物品。
     * 缓存所有未使用的项目。
     *
     * @return 如果重建item，则返回true
     */
    private boolean rebuildItems() {
        boolean updated = false;
        ItemsRange range = getItemsRange();
        if (itemsLayout != null) {
            int first = recycle.recycleItems(itemsLayout, firstItem, range);
            updated = firstItem != first;
            firstItem = first;
        } else {
            createItemsLayout();
            updated = true;
        }

        if (!updated) {
            updated = firstItem != range.getFirst() || itemsLayout.getChildCount() != range.getCount();
        }

        if (firstItem > range.getFirst() && firstItem <= range.getLast()) {
            for (int i = firstItem - 1; i >= range.getFirst(); i--) {
                if (!addViewItem(i, true)) {
                    break;
                }
                firstItem = i;
            }
        } else {
            firstItem = range.getFirst();
        }

        int first = firstItem;
        for (int i = itemsLayout.getChildCount(); i < range.getCount(); i++) {
            if (!addViewItem(firstItem + i, false) && itemsLayout.getChildCount() == 0) {
                first++;
            }
        }
        firstItem = first;

        return updated;
    }

    /**
     * 更新视图。
     * 重建项目并在必要时添加标签，重新计算item大小。
     */
    private void updateView() {
        if (rebuildItems()) {
            calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
            layout(getWidth(), getHeight());
        }
    }

    /**
     * 必要时创建item布局
     */
    private void createItemsLayout() {
        if (itemsLayout == null) {
            itemsLayout = new LinearLayout(getContext());
            itemsLayout.setOrientation(LinearLayout.VERTICAL);
        }
    }

    /**
     * 构建view并测量
     */
    private void buildViewForMeasuring() {
        // clear all items
        if (itemsLayout != null) {
            recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
        } else {
            createItemsLayout();
        }

        // add views
        int addItems = visibleItems / 2;
        for (int i = currentItem + addItems; i >= currentItem - addItems; i--) {
            if (addViewItem(i, true)) {
                firstItem = i;
            }
        }
    }

    /**
     * 将item视图添加到item布局
     *
     * @param index 索引值
     * @param first 该标志指示是否应首先查看
     * @return 如果存在相应项并添加，则为true
     */
    private boolean addViewItem(int index, boolean first) {
        View view = getItemView(index);
        if (view != null) {
            if (first) {
                itemsLayout.addView(view, 0);
            } else {
                itemsLayout.addView(view);
            }

            return true;
        }

        return false;
    }

    /**
     * 检查索引是否有效
     *
     * @param index 索引值
     * @return 如果项目索引未超出范围或滚轮是循环的，则为true
     */
    private boolean isValidItemIndex(int index) {
        return viewAdapter != null && viewAdapter.getItemsCount() > 0 &&
                (isCyclic || index >= 0 && index < viewAdapter.getItemsCount());
    }

    /**
     * 获取指定的item
     *
     * @param index 索引值
     * @return item视图或空视图（如果索引超出范围）
     */
    private View getItemView(int index) {
        if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
            return null;
        }
        int count = viewAdapter.getItemsCount();
        if (!isValidItemIndex(index)) {
            return viewAdapter.getEmptyItem(recycle.getEmptyItem(), itemsLayout);
        } else {
            while (index < 0) {
                index = count + index;
            }
        }

        index %= count;
        return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
    }

    /**
     * 停止滚动
     */
    public void stopScrolling() {
        scroller.stopScrolling();
    }
}
