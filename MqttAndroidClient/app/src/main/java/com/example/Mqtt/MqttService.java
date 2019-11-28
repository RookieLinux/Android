package com.example.Mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MqttService extends Service{

        public static final String TAG = MqttService.class.getSimpleName();

        private static String host = "tcp://42.105.13.186:1883";
        private static String userName = "IOTuserName";
        private static String passWord = "IOTpassWord";
        private static String sendTopic = "null/null/Actuator/00:09:C0:FF:EC:48/00:12:4B:00:06:1B:65:5D/1/02";      //要发布的主题
        private static String recTopic[] = {"null/null/Sensor/00:09:C0:FF:EC:48/00:12:4B:00:06:1B:65:2E/0/01","null/null/Sensor/00:09:C0:FF:EC:48/00:12:4B:00:06:1B:65:46/0/01","null/null/Sensor/00:09:C0:FF:EC:48/00:12:4B:00:06:1B:65:46/1/01"};      //要订阅的主题
        //private static String recTopic ="null/null/Gateway/00:09:C0:FF:EC:48/05";
        private static String clientId = "androidId";//客户端标识
        private IGetMessageCallBack IGetMessageCallBack;


        private static MqttAndroidClient client;
        private MqttConnectOptions conOpt;



        @Override
        public void onCreate() {
            super.onCreate();
            Log.e(getClass().getName(), "onCreate");
            init();
        }
         //  主题是myTopic  消息质量等级 0  消息在服务器端不保存
        public static void publish(String msg){
            String topic = MqttService.sendTopic;
            try {
                if (client != null){
                    client.publish(topic, msg.getBytes(),0, false,null,null);
                    Log.i("Publish","pubish mes is"+" "+ msg);
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        private void init() {
            // 服务器地址（协议+地址+端口号）
            String uri = host;
            client = new MqttAndroidClient(this, uri, clientId);
            // 设置MQTT监听并且接受消息   Sets a callback listener to use for events that happen asynchronously.
            client.setCallback(mqttCallback);

            conOpt = new MqttConnectOptions();
            // 清除缓存
            conOpt.setCleanSession(true);
            // 设置超时时间，单位：秒
            conOpt.setConnectionTimeout(10);
            // 心跳包发送间隔，单位：秒
            conOpt.setKeepAliveInterval(20);
            // 用户名
            conOpt.setUserName(userName);
            // 密码
            conOpt.setPassword(passWord.toCharArray());     //将字符串转换为字符串数组

            // last will message
            boolean doConnect = true;
            String message = "{\"terminal_uid\":\"" + clientId + "\"}";
            Log.e(getClass().getName(), "message是:" + message);
            String topic = sendTopic;
            Integer qos = 0;
            Boolean retained = false;
            if ((!message.equals("")) || (!topic.equals(""))) {
                // 最后的遗嘱
                // MQTT本身就是为信号不稳定的网络设计的，所以难免一些客户端会无故的和Broker断开连接。
                //当客户端连接到Broker时，可以指定LWT，Broker会定期检测客户端是否有异常。
                //当客户端异常掉线时，Broker就往连接时指定的topic里推送当时指定的LWT消息。

                try {
                    conOpt.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
                } catch (Exception e) {
                    Log.i(TAG, "Exception Occured", e);
                    doConnect = false;
                    iMqttActionListener.onFailure(null, e);
                }
            }

            if (doConnect) {
                //连接Mqtt服务器
                doClientConnection();
            }

        }


        @Override
        public void onDestroy() {
            stopSelf();
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            super.onDestroy();
        }

        /** 连接MQTT服务器 */
        private void doClientConnection() {
            //是否连接服务器  网络连接是否正常      连接成功后订阅主题
            if (!client.isConnected() && isConnectIsNormal()) {
                try {
                    client.connect(conOpt, null, iMqttActionListener);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

        }

        // MQTT是否连接成功
        private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken arg0) {
                Log.i(TAG, "连接成功 ");
                try {
                    // 订阅myTopic话题
                    for(int i=0;i<3;i++)
                    client.subscribe(recTopic[i],1);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(IMqttToken arg0, Throwable arg1) {
                arg1.printStackTrace();
                // 连接失败，重连
            }
        };

        // MQTT监听并且接受消息
        private MqttCallback mqttCallback = new MqttCallback() {

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String str1 = new String(message.getPayload());
                if (IGetMessageCallBack != null){
                    //光敏
                    if(topic.equals("null/null/Sensor/00:09:C0:FF:EC:48/00:12:4B:00:06:1B:65:2E/0/01")){
                        IGetMessageCallBack.setMessage(str1+"lx");//调用activity中的setMessage();
                    }
                    //温度
                    else if(topic.equals("null/null/Sensor/00:09:C0:FF:EC:48/00:12:4B:00:06:1B:65:46/0/01")){
                        IGetMessageCallBack.setMessage1(str1+"℃");//调用activity中的setMessage();
                    }
                    //湿度
                    else if(topic.equals("null/null/Sensor/00:09:C0:FF:EC:48/00:12:4B:00:06:1B:65:46/1/01")){
                        IGetMessageCallBack.setMessage2(str1+"%");//调用activity中的setMessage();
                    }
                    //IGetMessageCallBack.setMessage(str1);//调用activity中的setMessage();
                }
                String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
                Log.i(TAG, "messageArrived:" + str1);
                Log.i(TAG, str2);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {

            }

            @Override
            public void connectionLost(Throwable arg0) {
                // 失去连接，重连
            }
        };

        /** 判断网络是否连接 */
        private boolean isConnectIsNormal() {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                String name = info.getTypeName();
                Log.i(TAG, "MQTT当前网络名称：" + name);
                return true;
            } else {
                Log.i(TAG, "MQTT 没有可用网络");
                return false;
            }
        }


        @Override
        public IBinder onBind(Intent intent) {
            Log.e(getClass().getName(), "onBind");
            return new CustomBinder();
        }

        public void setIGetMessageCallBack(IGetMessageCallBack IGetMessageCallBack){
            this.IGetMessageCallBack = IGetMessageCallBack;
        }

        public class CustomBinder extends Binder {
            //公开方法给activity调用
            public MqttService getService(){
                return MqttService.this;
            }
        }

}
