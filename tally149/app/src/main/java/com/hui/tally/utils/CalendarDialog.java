package com.hui.tally.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hui.tally.R;
import com.hui.tally.adapter.CalendarAdapter;
import com.hui.tally.db.DBManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarDialog extends Dialog implements View.OnClickListener {
    ImageView errorIv;
    GridView gv;
    LinearLayout hsvLayout;

    List<TextView>hsvViewList;
    List<Integer>yearList;

    int selectPos = -1;   //表示正在被点击的年份的位置 Indicates the location of the year being clicked
    private CalendarAdapter adapter;
    int selectMonth = -1;

    public interface OnRefreshListener{
        public void onRefresh(int selPos,int year,int month);
    }
    OnRefreshListener onRefreshListener;

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public CalendarDialog(@NonNull Context context,int selectPos,int selectMonth) {
        super(context);
        this.selectPos = selectPos;
        this.selectMonth = selectMonth;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_calendar);
        gv = findViewById(R.id.dialog_calendar_gv);
        errorIv = findViewById(R.id.dialog_calendar_iv);
        hsvLayout = findViewById(R.id.dialog_calendar_layout);
        errorIv.setOnClickListener(this);
        // 向横向的ScrollView当中添加View的方法 Method of adding View to horizontal ScrollView
        addViewToLayout();
        initGridView();
        //设置GridView当中每一个item的点击事件 Set the click event of each item in the GridView
        setGVListener();
    }

    private void setGVListener() {
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.selPos = position;
                adapter.notifyDataSetInvalidated();
                int month = position + 1;
                int year = adapter.year;
                onRefreshListener.onRefresh(selectPos,year,month);
                cancel();
            }
        });
    }

    private void initGridView() {
        int selYear = yearList.get(selectPos);
        adapter = new CalendarAdapter(getContext(), selYear);
        if (selectMonth == -1) {
            int month = Calendar.getInstance().get(Calendar.MONTH);
            adapter.selPos = month;
        }else {
            adapter.selPos = selectMonth-1;
        }
        gv.setAdapter(adapter);
    }

    private void addViewToLayout() {
        hsvViewList = new ArrayList<>();   //将添加进入线性布局当中的TextView进行统一管理的集合 A collection of TextView that will be added to the linear layout for unified management
        yearList = DBManager.getYearListFromAccounttb(); //获取数据库当中存储了多少个年份 Get how many years are stored in the database
        //如果数据库当中没有记录，就添加今年的记录 If there is no record in the database, add this year's record
        if (yearList.size() == 0) {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            yearList.add(year);
        }

        //遍历年份，有几年，就向ScrollView当中添加几个view
        //Traverse the years and add several views to the ScrollView if there are several years
        for (int i = 0; i < yearList.size(); i++) {
            int year = yearList.get(i);
            View view = getLayoutInflater().inflate(R.layout.item_dialogcal_hsv, null);
            hsvLayout.addView(view);   //将view添加到布局当中 Add the view to the layout
            TextView hsvTv = view.findViewById(R.id.item_dialogcal_hsv_tv);
            hsvTv.setText(year+"");
            hsvViewList.add(hsvTv);
        }
        if (selectPos == -1) {
            selectPos = hsvViewList.size()-1;     //设置当前被选中的是最近的年份Set the current selected year to be the most recent year
        }
        changeTvbg(selectPos);    //将最后一个设置为选中状态 Set the last one as selected
        setHSVClickListener();    //设置每一个View的监听事件 Set the listening event of each View
    }
    /** 给横向的ScrollView当中每一个TextView设置点击事件
     * Set the click event for each TextView in the horizontal ScrollView*/
    private void setHSVClickListener() {
        for (int i = 0; i < hsvViewList.size(); i++) {
            TextView view = hsvViewList.get(i);
            final int pos = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeTvbg(pos);
                    selectPos = pos;
                    int year = yearList.get(selectPos);
                    adapter.setYear(year);
                }
            });
        }
    }

    /** 传入被选中的位置，改变此位置上的背景和文字颜色
     * Pass in the selected position, change the background and text color at this position*/
    private void changeTvbg(int selectPos) {
        for (int i = 0; i < hsvViewList.size(); i++) {
            TextView tv = hsvViewList.get(i);
            tv.setBackgroundResource(R.drawable.dialog_btn_bg);
            tv.setTextColor(Color.BLACK);
        }

        TextView selView = hsvViewList.get(selectPos);
        selView.setBackgroundResource(R.drawable.main_recordbtn_bg);
        selView.setTextColor(Color.WHITE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_calendar_iv:
                cancel();
                break;
        }
    }

    /* 设置Dialog的尺寸和屏幕尺寸一致
    * Set the size of Dialog to be the same as the screen size*/
    public void setDialogSize(){
//        获取当前窗口对象 Get the current window object
        Window window = getWindow();
//        获取窗口对象的参数Get the parameters of the window object
        WindowManager.LayoutParams wlp = window.getAttributes();
        Display d = window.getWindowManager().getDefaultDisplay();
        wlp.width = (int)(d.getWidth());
        wlp.gravity = Gravity.TOP;
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setAttributes(wlp);
    }
}
