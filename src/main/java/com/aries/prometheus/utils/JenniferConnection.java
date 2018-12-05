package com.aries.prometheus.utils;

import com.aries.extension.util.LogUtil;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class JenniferConnection {

    public JSONObject getResponse(String urlStr){
        HttpURLConnection conn;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream in = new BufferedInputStream(conn.getInputStream());
            return new JSONObject(IOUtils.toString(in, "UTF-8"));
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            conn = null;
        }
        return null;
    }
}
