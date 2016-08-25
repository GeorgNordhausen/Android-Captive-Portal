package com.lfk.justwe_webserver.WebServer;

import android.content.Context;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread Controller
 *
 * @author liufengkai
 *         Created by liufengkai on 16/1/6.
 */
public class WebServer extends Thread {
    // listen to connect
    private ServerSocket serverSocket;
    // log / error listener
    private static LupinServer.MessageHandler logResult;
    private Context context;
    private boolean IsRunning;
    // solve threads
    private static ExecutorService exe;

    public WebServer(Context context, LupinServer.MessageHandler logResult, int port) {
        super();
        this.context = context;
        WebServer.logResult = logResult;
        this.IsRunning = true;
        exe = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(port, 0,InetAddress.getByName(WebServerDefault.WebServerIp));
            logResult.OnResult("listen to :" + WebServerDefault.WebServerIp + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
            logResult.OnError("Server IO error");
        }
    }

    @Override
    public void run() {
        super.run();
        while (IsRunning) {
            try {
                logResult.OnResult("<<<<<<< waiting >>>>>>>");
                Socket s = serverSocket.accept();
                logResult.OnResult("get from" + s.getInetAddress().toString());
                exe.execute(new RequestSolve(context.getAssets(),s));
            } catch (IOException e) {
                logResult.OnError(e.getMessage());
            }
        }
    }

    public void stopServer() {
        IsRunning = false;
        try {
            serverSocket.close();
            logResult.OnResult("Server close");
        } catch (IOException e) {
            logResult.OnError(e.getMessage());
        }
    }

    public static LupinServer.MessageHandler getLogResult() {
        return logResult;
    }
}
