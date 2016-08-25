package com.lfk.justwe_webserver.WebServer.Interface;

import java.io.InputStream;

/**
 * Created by dega1999 on 07/08/16.
 */
public interface OnWebAssetResult extends OnWebResult{
    InputStream returnAsset() throws Exception;
}
