
package com.freak.lottery.widget;
/**
 * @author Freak
 * @date 2019/12/19.
 */
public class ItemsRange {
    private int first;

    private int count;

    public ItemsRange() {
        this(0, 0);
    }

    public ItemsRange(int first, int count) {
        this.first = first;
        this.count = count;
    }


    public int getFirst() {
        return first;
    }


    public int getLast() {
        return getFirst() + getCount() - 1;
    }


    public int getCount() {
        return count;
    }

    /**
     * 判断item是否包含在内
     *
     * @param index 索引值
     * @return 如果包含item，则为true
     */
    public boolean contains(int index) {
        return index >= getFirst() && index <= getLast();
    }
}