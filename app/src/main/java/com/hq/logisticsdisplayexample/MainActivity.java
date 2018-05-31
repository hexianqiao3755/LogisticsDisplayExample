package com.hq.logisticsdisplayexample;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv_logistics = findViewById(R.id.rv_logistics);
        rv_logistics.setLayoutManager(new LinearLayoutManager(this));
        //解决ScrollView嵌套RecyclerView出现的系列问题
        rv_logistics.setNestedScrollingEnabled(false);
        rv_logistics.setHasFixedSize(true);
        rv_logistics.setAdapter(new LogisticsInfoAdapter(this, R.layout.item_logistics, getData()));
    }

    private List<LogisticsInfoBean> getData() {
        List<LogisticsInfoBean> data = new ArrayList<>();
        data.add(new LogisticsInfoBean("2018-05-20 13:37:57", "客户 签收人: 他人代收 已签收 感谢使用圆通速递，期待再次为您服务"));
        data.add(new LogisticsInfoBean("2018-05-20 09:03:42", "【广东省深圳市宝安区新安公司】 派件人: 陆黄星 派件中 派件员电话13360979918"));
        data.add(new LogisticsInfoBean("2018-05-20 08:27:10", "【广东省深圳市宝安区新安公司】 已收入"));
        data.add(new LogisticsInfoBean("2018-05-20 04:38:32", "【深圳转运中心】 已收入"));
        data.add(new LogisticsInfoBean("2018-05-19 01:27:49", "【北京转运中心】 已发出 下一站 【深圳转运中心】"));
        data.add(new LogisticsInfoBean("2018-05-19 01:17:19", "【北京转运中心】 已收入"));
        data.add(new LogisticsInfoBean("2018-05-18 18:34:28", "【河北省保定市容城县公司】 已发出 下一站 【北京转运中心】"));
        data.add(new LogisticsInfoBean("2018-05-18 18:33:23", "【河北省保定市容城县公司】 已打包"));
        data.add(new LogisticsInfoBean("2018-05-18 18:27:21", "【河北省保定市容城县公司】 已收件"));
        return data;
    }
}
