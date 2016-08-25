package com.lfk.justwe_webserver.WebServer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

import com.dega1999.dns.liar.DnsLiar;
import com.lfk.justwe_webserver.R;

/**
 * Service for Android
 *
 * @author liufengkai
 *         Created by liufengkai on 16/1/6.
 */

//TODO rivedere come chiudere correttamente il service
//TODO non sono sicuro sia giusto cosi'.

public class LupinServerService extends Service {
    private LupinServer.MessageHandler logResult;
    private NotificationManager notificationManager;
    private Notification notification;
    private final IBinder mBinder = new LocalBinder();
    private WebServer webServer;
    private DnsLiar dnsServer;
    private static Activity engine;
    private PendingIntent contentIntent;
    private boolean IsRunning;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        updateNotification("Open Service");
    }

    public static void init(Activity engine) {
        LupinServerService.engine = engine;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void updateNotification(String text) {
        contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, engine.getClass()), 0);
        Notification.Builder builder = new Notification.Builder(engine)
                .setContentTitle("LupinServer running")
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis());
        notification = builder.getNotification();
        notificationManager.notify(0, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public class LocalBinder extends Binder {
        LupinServerService getService() {
            return LupinServerService.this;
        }
    }

    public void startServer(LupinServer.MessageHandler logResult, int port) {
        this.logResult = logResult;
        // running
        setIsRunning(true);

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        //WebServerDefault.setWebServerIp(WebServerDefault.intToIp(wifiInfo.getIpAddress()));
        WebServerDefault.setWebServerIp("192.168.43.1");
        // if not in wifi
        if (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            this.logResult.OnError("Please connect to a WIFI-network.");
            try {
                throw new Exception("Please connect to a WIFI-network.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        webServer = new WebServer(engine.getApplicationContext(), logResult, port);
        webServer.start();

        dnsServer = new DnsLiar(engine.getApplicationContext(),logResult);
        dnsServer.start();
        updateNotification("Webserver UP: " + WebServerDefault.WebServerIp + ":" + port);
    }

    public void stopServer() {
        setIsRunning(false);
        if(webServer != null) {
            webServer.stopServer();
            webServer.interrupt();
        }
        if(dnsServer!= null) {
            dnsServer.stopServer();
            dnsServer.interrupt();
        }
    }

    public void setIsRunning(boolean isRunning) {
        IsRunning = isRunning;
    }
}
