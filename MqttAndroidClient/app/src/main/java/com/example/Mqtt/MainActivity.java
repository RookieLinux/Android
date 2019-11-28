package com.example.Mqtt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements IGetMessageCallBack {

    //    private TextView txt3,txt4,txt5;
    private String str[]={"","",""};
    private SimpleAdapter adapter;
    private TextView txt;
    private Button button;
    private ArrayList list,newlist;
    private ListView listView;
    private MyServiceConnection serviceConnection;
    int j = 0;
//    private MqttService mqttService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        txt3 =  findViewById(R.id.txt3);
//        txt4 =  findViewById(R.id.txt4);
//        txt5 =  findViewById(R.id.txt5);

        txt  = findViewById(R.id.item_content);
        button =  findViewById(R.id.button);

        listView = findViewById(R.id.lv);
        initDataList();

        // key值数组，适配器通过key值取value，与列表项组件一一对应
        String[] from = { "img", "title", "content" };
        // 列表项组件Id 数组
        int[] to = { R.id.item_img, R.id.item_title, R.id.item_content};

        /**
         * SimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to)
         * context：activity界面类
         * data 数组内容是map的集合数据
         * resource 列表项文件
         * from map key值数组
         * to 列表项组件id数组      from与to一一对应，适配器绑定数据
         */
        adapter = new SimpleAdapter(this, list,
                R.layout.listview_item, from, to);

        listView.setAdapter(adapter);

        serviceConnection = new MyServiceConnection();
        //MyServiceConnection类中定义的方法setIGetMessageCallBack
        serviceConnection.setIGetMessageCallBack(MainActivity.this);

        Intent intent = new Intent(this, MqttService.class);

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                j++;
                if (j == 1) {
                    MqttService.publish("0");
                    button.setBackgroundResource(R.mipmap.light3);
                } else if (j == 2) {
                    MqttService.publish("1");
                    button.setBackgroundResource(R.mipmap.light);
                    j = 0;
                }
            }
        });
    }

    @Override
    public void setMessage(String message) {
        str[0]=message;
        updateDataList();

    }
    @Override
    public void setMessage1(String message) {
        str[1]=message;
        updateDataList();
    }

    @Override
    public void setMessage2(String message) {
        str[2]=message;
        updateDataList();
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    /**
     * 初始化适配器需要的数据格式
     */
    private void initDataList() {
        //图片资源
        int img[] = {R.mipmap.weathy, R.mipmap.tem, R.mipmap.hum};
        String text[] = {"光敏","温度","湿度"};
        list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("img", img[i]);
            map.put("title",text[i]);
            map.put("content", "0");
            list.add(map);
        }
    }

    private void updateDataList(){
        list.clear();
        int img[] = {R.mipmap.weathy, R.mipmap.tem, R.mipmap.hum};
        String text[] = {"光敏","温度","湿度"};
        newlist = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("img", img[i]);
            map.put("title",text[i]);
            map.put("content",str[i]);
            list.add(map);
        }
        //txt.setText(message);
        list.addAll(newlist);
        adapter.notifyDataSetChanged();
    }
}