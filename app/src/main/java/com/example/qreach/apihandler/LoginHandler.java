package com.example.qreach.apihandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginHandler {
    private static final String BASE_URL = "http://192.168.100.92/QReachAPI";

    public interface CallBack{
        void onSuccess(JSONObject data);
        void onError(String message);
    }

    private static void sendRequest(String endPoint, String method, JSONObject jsonBody, CallBack callBack){
        new Thread(() -> {
            URL url = null;
            try {
                url = new URL(BASE_URL + endPoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                if (jsonBody != null) {
                    conn.setDoOutput(true);
                    byte[] postData = jsonBody.toString().getBytes(StandardCharsets.UTF_8);
                    try(OutputStream os = conn.getOutputStream()){
                        os.write(postData);
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
        });
    }

    public static void  login(String email, String password, CallBack callBack){
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("password", password);
        } catch (JSONException e) {
            callBack.onError("Error : " + e.getMessage());
            throw new RuntimeException(e);
        }
        sendRequest("login.php", "POST", json, callBack);
    }

    public static void checkLogin(CallBack callBack){
        sendRequest("login.php", "GET", null, callBack);
    }

    public static void logout(CallBack callBack){
        sendRequest("logout.php", "GET", null, callBack);
    }
}
