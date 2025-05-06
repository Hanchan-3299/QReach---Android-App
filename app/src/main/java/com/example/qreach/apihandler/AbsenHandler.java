package com.example.qreach.apihandler;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class AbsenHandler {
    private static final String BASE_URL = "http://192.168.100.92/QReachAPI/";

    public interface CallBack {
        void onSuccess(JSONObject data);
        void onError(String message);
    }

    public static void sendAbsen(Context context, String userId, String email, String qrData, CallBack callBack) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "absen.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject requestBody = new JSONObject();
                requestBody.put("id", userId);
                requestBody.put("email", email);
                requestBody.put("qrdata", qrData);
                requestBody.put("status", true);

                OutputStream os =conn.getOutputStream();
                os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Scanner in = new Scanner(conn.getInputStream());
                StringBuilder response =new StringBuilder();
                while(in.hasNext()) {
                    response.append(in.nextLine());
                }
                in.close();

                JSONObject responseJSON = new JSONObject(response.toString());
                callBack.onSuccess(responseJSON);


            } catch (IOException | JSONException e) {
                callBack.onError("error : " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();
    }
}
