

package com.freak.lottery.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

/**
 * 滚轮适配器 可继承自定义
 *
 * @author Freak
 * @date 2019/12/19.
 */
public abstract class AbstractWheelAdapter implements WheelViewAdapter {
    // Observers
    private List<DataSetObserver> dataSetObservers;

    @Override
    public View getEmptyItem(View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (dataSetObservers == null) {
            dataSetObservers = new LinkedList<DataSetObserver>();
        }
        dataSetObservers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (dataSetObservers != null) {
            dataSetObservers.remove(observer);
        }
    }

    /**
     * 通知观察者有关数据更改的信息
     */
    protected void notifyDataChangedEvent() {
        if (dataSetObservers != null) {
            for (DataSetObserver observer : dataSetObservers) {
                observer.onChanged();
            }
        }
    }

    /**
     * 通知观察者有关数据无效的信息
     */
    protected void notifyDataInvalidatedEvent() {
        if (dataSetObservers != null) {
            for (DataSetObserver observer : dataSetObservers) {
                observer.onInvalidated();
            }
        }
    }
}
