package com.lfk.justwe_webserver;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lfk.justwe_webserver.WebServer.Interface.OnLogResult;
import com.lfk.justwe_webserver.WebServer.Interface.OnPostData;
import com.lfk.justwe_webserver.WebServer.Interface.OnWebAssetResult;
import com.lfk.justwe_webserver.WebServer.Interface.OnWebFileResult;
import com.lfk.justwe_webserver.WebServer.Interface.OnWebStringResult;
import com.lfk.justwe_webserver.WebServer.LupinServer;
import com.lfk.justweengine.Utils.logger.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnLogResult {
    private LupinServer server;
    private TextView textView;
    private ScrollView scrollView;
    private boolean open = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.init();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = (TextView) findViewById(R.id.main_log);
        scrollView = (ScrollView) findViewById(R.id.main_scroll);

        server = new LupinServer(MainActivity.this, this);
        server.initWebService();

        server.apply("/logs", new OnWebFileResult() {
            @Override
            public File returnFile() {
                return new File("/data/data/com.lfk.justwe_webserver/files/log/webserver.log");
            }
        });

        //trovare un modo di pulire i log.
        server.apply("/clearlogs", new OnWebStringResult() {
            @Override
            public String OnResult() {
                return "Cleaning.. TODO.. missing";
            }
        });

        server.apply("/idx", new OnWebAssetResult() {
            @Override
            public InputStream returnAsset() throws Exception{
                return getAssets().open("index.htm");
            }
        });

        server.apply("/login.cgi", new OnWebStringResult() { ///unused???
            @Override
            public String OnResult() {
                return "Connected...";
            }
        });

        server.apply("/lfkdsk", new OnPostData() {
            @Override
            public String OnPostData(HashMap<String, String> hashMap) {
                if(hashMap==null)
                    return "null";
                String S = hashMap.get("LFKDSK");
                Logger.e(S);
                return "=_=";
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!open) {
                    server.startWebService();
                    open = true;
                } else {
                    server.stopWebService();
                    open = false;
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnResult(String log) {
        Log.e("log", log);
        textView.append(log + "\n");
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    @Override
    public void OnError(String error) {
        Log.e("error", error);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        server.callOffWebService();
    }
}
