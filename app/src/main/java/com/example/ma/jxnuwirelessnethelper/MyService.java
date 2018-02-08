package com.example.ma.jxnuwirelessnethelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyService extends Service
{
    private String username;
    private String password;
    public MyService()
    {
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid=wifiInfo.getSSID();
        if(ssid.equals("\"jxnu_stu\""))
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    connectHost();
                    stopSelf();
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public void connectHost()
    {
        OkHttpClient client=new OkHttpClient.Builder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .build();
        SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
        RequestBody loginform=new FormBody.Builder()
                .add("action", "login")
                .add("username", pref.getString("username",""))
                .add("password", pref.getString("password",""))
                .add("ac_id", "1")
                .add("user_ip", "")
                .add("nas_ip", "")
                .add("user_mac", "")
                .add("save_me", "0")
                .add("ajax", "1")
                .build();

        Request loginRequest = new Request.Builder()
                .url("http://219.229.251.2/include/auth_action.php")
                .post(loginform)
                .build();
        try {
            Response loginResponse=client.newCall(loginRequest).execute();
            String body=loginResponse.body().string().toString();
            int code=isConnect(body);
            if(code==1)
            {
                showNotification("登录成功！");
            }
            else
            {
                showNotification(body);
            }
        }
        catch (IOException e){e.printStackTrace();showNotification("登录失败，地址无法访问！");}
    }

    private int isConnect(String body)
    {
        if(body.substring(0,8).equals("login_ok"))
            return 1;
        else return 0;
    }

    private void showNotification(String str)
    {
        NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notification=new NotificationCompat.Builder(this)
                .setContentTitle("Hi")
                .setContentText(str)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .build();
        manager.notify(1,notification);
    }
}