package com.example.qreach.apihandler;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class LoginHandler {
    private static final String BASE_URL = "http://192.168.100.92/QReachAPI/";
    private static final String APP_PREFS = "account_logged_in";
    private static final String SESSION_KEY = "SessionId";

    public interface CallBack{
        void onSuccess(JSONObject data);
        void onError(String message);
    }

    private static void sendRequest(Context context, String endPoint, String method, JSONObject jsonBody, CallBack callBack){
        new Thread(() -> {
            URL url = null;
            try {
                url = new URL(BASE_URL + endPoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setUseCaches(false);
                SharedPreferences prefs = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
                String sessionId = prefs.getString(SESSION_KEY, null);
                if(sessionId != null){
                    conn.setRequestProperty("Cookie", sessionId);
                }

                if (jsonBody != null) {
                    conn.setDoOutput(true);
                    byte[] postData = jsonBody.toString().getBytes(StandardCharsets.UTF_8);
                    try(OutputStream os = conn.getOutputStream()){
                        os.write(postData);
                    }
                }

                Map<String, List<String>> headerFields = conn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get("Set-Cookie");
                if(cookiesHeader != null) {
                    for (String cookie : cookiesHeader) {
                        if(cookie.startsWith("PHPSESSID")){
                            String newSession = cookie.split(";")[0];
                            prefs.edit().putString(SESSION_KEY, newSession).apply();
                        }
                    }
                }

                StringBuilder response = new StringBuilder();
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
                    String line;
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                }

                JSONObject resJson = new JSONObject(response.toString());
                if(resJson.optString("status").equals("success")){
                    callBack.onSuccess(resJson.optJSONObject("data"));
                }else {
                    callBack.onError(resJson.optString("message", "Unknown Error"));
                }

            } catch (IOException | JSONException e) {
                callBack.onError("Error : " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void  login(Context context, String email, String password, CallBack callBack){
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("password", password);
        } catch (JSONException e) {
            callBack.onError("Error : " + e.getMessage());
            throw new RuntimeException(e);
        }
        sendRequest(context, "login.php", "POST", json, callBack);
    }

    public static void checkLogin(Context context, CallBack callBack){
        sendRequest(context, "login.php", "GET", null, callBack);
    }

    public static void logout(Context context, CallBack callBack){
        sendRequest(context, "login.php", "GET", null, new CallBack() {
            @Override
            public void onSuccess(JSONObject data) {
                SharedPreferences prefs = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
                prefs.edit().remove(SESSION_KEY).apply();
                callBack.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callBack.onError(message);
            }
        });
    }
}
