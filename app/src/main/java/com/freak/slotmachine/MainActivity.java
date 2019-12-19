package com.freak.slotmachine;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.freak.lottery.WheelManager;
import com.freak.lottery.adapters.AbstractWheelAdapter;
import com.freak.lottery.widget.OnWheelScrollListener;
import com.freak.lottery.widget.WheelView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private Button button;
    private Button button1;
    private WheelView wheelView;
    private WheelView wheelView1;
    private WheelView wheelView2;
    private WheelView wheelView3;
    private List<WheelView> list;
    private List<Integer> roundList;
    private List<Integer> timeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button1 = findViewById(R.id.button1);
        wheelView = findViewById(R.id.dialog_slot_1);
        wheelView1 = findViewById(R.id.dialog_slot_2);
        wheelView2 = findViewById(R.id.dialog_slot_3);
        wheelView3 = findViewById(R.id.dialog_slot_4);
        list = new ArrayList<>();
        roundList = new ArrayList<>();
        timeList = new ArrayList<>();
        roundList.add(50);
        roundList.add(70);
        roundList.add(90);
        roundList.add(110);
        timeList.add(2000);
        timeList.add(3000);
        timeList.add(5000);
        timeList.add(7000);
        list.add(wheelView);
        list.add(wheelView1);
        list.add(wheelView2);
        list.add(wheelView3);
        WheelManager.getInstance().initWheel(new SlotMachineAdapter(), list, 1, wheelView3, true, false, false, new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
                Log.e("TAG", "开始滚动");
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                Log.e("TAG", "滚动结束");
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WheelManager.getInstance().start("96573", list, roundList, timeList);
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WheelManager.getInstance().start("4100", list, roundList, timeList);

            }
        });
    }


    /**
     * 老虎机适配器
     */
    private class SlotMachineAdapter extends AbstractWheelAdapter {
        @Override
        public int getItemsCount() {
            return 10;
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            View view;
            if (cachedView != null) {
                view = cachedView;
            } else {
                view = View.inflate(MainActivity.this, R.layout.item_dialog_tiger_img, null);
            }
            ImageView img = (ImageView) view.findViewById(R.id.iv_dialog_home_tiger);
            switch (index) {
                case 0:
                    img.setImageResource(R.mipmap.ic_icon_0);
                    break;
                case 1:
                    img.setImageResource(R.mipmap.ic_icon_1);
                    break;
                case 2:
                    img.setImageResource(R.mipmap.ic_icon_2);
                    break;
                case 3:
                    img.setImageResource(R.mipmap.ic_icon_3);
                    break;
                case 4:
                    img.setImageResource(R.mipmap.ic_icon_4);
                    break;
                case 5:
                    img.setImageResource(R.mipmap.ic_icon_5);
                    break;
                case 6:
                    img.setImageResource(R.mipmap.ic_icon_6);
                    break;
                case 7:
                    img.setImageResource(R.mipmap.ic_icon_7);
                    break;
                case 8:
                    img.setImageResource(R.mipmap.ic_icon_8);
                    break;
                case 9:
                    img.setImageResource(R.mipmap.ic_icon_9);
                    break;
            }

            return view;
        }
    }
}
