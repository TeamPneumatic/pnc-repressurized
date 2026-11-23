/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.lib.Log;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PastebinHandler extends Thread {
    private static String userKey;
    private final HttpClient httpclient;
    public String username, password, contents, getLink;
    private static final String DEV_KEY = "e8b9b06d96deb19afa787604a89dd240";
    private static volatile boolean isDone;
    private static PastebinHandler runningHandler;
    private static volatile Exception exception;

    private PastebinHandler() {
        // Apache HttpClient is no longer shipped with Minecraft 26.1; use the JDK client instead
        httpclient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(5000)).build();
        isDone = false;
    }

    public static boolean isLoggedIn() {
        return userKey != null;
    }

    public static boolean isDone() {
        return isDone;
    }

    public static Exception getException() {
        return exception;
    }

    public static PastebinHandler getHandler() {
        if (!isDone) throw new IllegalStateException("Can't access pastebin handler while it's still running");
        return runningHandler;
    }

    @Override
    public void run() {
        try {
            exception = null;
            if (username != null) {
                loginInternal(username, password);
            } else if (contents != null) {
                getLink = putInternal(contents);
            } else if (getLink != null) {
                contents = getInternal(getLink);
            }
        } catch (Exception e) {
            exception = e;
        }
        isDone = true;
    }

    public static PastebinHandler getCleanHandler() {
        if (runningHandler != null) runningHandler.interrupt();
        runningHandler = new PastebinHandler();
        return runningHandler;
    }

    public static void login(String username, String password) {
        PastebinHandler handler = getCleanHandler();
        handler.username = username;
        handler.password = password;
        handler.start();
    }

    public static void logout() {
        userKey = null;
    }

    public static void put(String contents) {
        PastebinHandler handler = getCleanHandler();
        handler.contents = contents;
        handler.start();
    }

    public static void get(String pastebinLink) {
        PastebinHandler handler = getCleanHandler();
        handler.getLink = pastebinLink;
        handler.start();
    }

    public boolean loginInternal(String userName, String password) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("api_dev_key", DEV_KEY);
        params.put("api_user_name", userName);
        params.put("api_user_password", password);
        try {
            String body = postForm("https://pastebin.com/api/api_login.php", params);
            if (body != null) {
                userKey = body;
                if (userKey.startsWith("Bad API request")) {
                    Log.warning("User tried to log in into pastebin, it responded with the following: " + userKey);
                    userKey = null;
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String putInternal(String contents) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("api_dev_key", DEV_KEY);
        params.put("api_paste_code", contents);
        params.put("api_paste_format", "json");
        params.put("api_option", "paste");
        if (isLoggedIn()) params.put("api_user_key", userKey);
        try {
            return postForm("https://pastebin.com/api/api_post.php", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String postForm(String url, Map<String, String> params) throws IOException, InterruptedException {
        String form = params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();
        return httpclient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
    }

    public String getInternal(String key) throws IOException {
        if (key.contains("pastebin")) key = key.substring(key.lastIndexOf('/') + 1);
        return PneumaticCraftUtils.getPage("https://pastebin.com/raw/" + key);
    }
}
