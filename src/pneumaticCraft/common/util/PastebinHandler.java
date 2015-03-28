package pneumaticCraft.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import pneumaticCraft.common.UpdateChecker;
import pneumaticCraft.lib.Log;

public class PastebinHandler extends Thread{
    private static String userKey;
    private final HttpClient httpclient;
    public String username, password, contents, getLink;
    private static final String DEV_KEY = "e8b9b06d96deb19afa787604a89dd240";
    private static volatile boolean isDone;
    private static PastebinHandler runningHandler;
    private static volatile Exception exception;

    private PastebinHandler(){
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).build();
        httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        isDone = false;
    }

    public static boolean isLoggedIn(){
        return userKey != null;
    }

    public static boolean isDone(){
        return isDone;
    }

    public static Exception getException(){
        return exception;
    }

    public static PastebinHandler getHandler(){
        if(!isDone) throw new IllegalStateException("Can't access pastebin handler while it's still running");
        return runningHandler;
    }

    @Override
    public void run(){
        try {
            exception = null;
            if(username != null) {
                loginInternal(username, password);
            } else if(contents != null) {
                getLink = putInternal(contents);
            } else if(getLink != null) {
                contents = getInternal(getLink);
            }
        } catch(Exception e) {
            exception = e;
        }
        isDone = true;
    }

    public static PastebinHandler getCleanHandler(){
        if(runningHandler != null) runningHandler.interrupt();
        runningHandler = new PastebinHandler();
        return runningHandler;
    }

    public static void login(String username, String password){
        PastebinHandler handler = getCleanHandler();
        handler.username = username;
        handler.password = password;
        handler.start();
    }

    public static void logout(){
        userKey = null;
    }

    public static void put(String contents){
        PastebinHandler handler = getCleanHandler();
        handler.contents = contents;
        handler.start();
    }

    public static void get(String pastebinLink){
        PastebinHandler handler = getCleanHandler();
        handler.getLink = pastebinLink;
        handler.start();
    }

    public boolean loginInternal(String userName, String password){
        HttpPost httppost = new HttpPost("http://pastebin.com/api/api_login.php");

        List<NameValuePair> params = new ArrayList<NameValuePair>(3);
        params.add(new BasicNameValuePair("api_dev_key", DEV_KEY));
        params.add(new BasicNameValuePair("api_user_name", userName));
        params.add(new BasicNameValuePair("api_user_password", password));
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            if(entity != null) {
                InputStream instream = entity.getContent();
                userKey = IOUtils.toString(instream, "UTF-8");
                if(userKey.startsWith("Bad API request")) {
                    Log.warning("User tried to log in into pastebin, it responded with the following: " + userKey);
                    userKey = null;
                    return false;
                }
                return true;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String putInternal(String contents){
        HttpPost httppost = new HttpPost("http://pastebin.com/api/api_post.php");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("api_dev_key", DEV_KEY));
        params.add(new BasicNameValuePair("api_paste_code", contents));
        params.add(new BasicNameValuePair("api_option", "paste"));
        if(isLoggedIn()) params.add(new BasicNameValuePair("api_user_key", userKey));
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            if(entity != null) {
                InputStream instream = entity.getContent();
                return IOUtils.toString(instream, "UTF-8");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getInternal(String key) throws IOException{
        if(key.contains("pastebin")) key = key.substring(key.lastIndexOf('/') + 1);
        return UpdateChecker.getPage("http://pastebin.com/raw.php?i=" + key);
    }
}
