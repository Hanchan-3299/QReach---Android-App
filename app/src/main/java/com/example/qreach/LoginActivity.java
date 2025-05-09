package com.example.qreach;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.qreach.apihandler.LoginHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private boolean isLoading = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen sp = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sp.setKeepOnScreenCondition(() -> isLoading);
        new android.os.Handler().postDelayed(() -> {
            isLoading = false;

            LoginHandler.checkLogin(LoginActivity.this, new LoginHandler.CallBack() {
                @Override
                public void onSuccess(JSONObject data) {
                    runOnUiThread(() -> {
                        String id = null;

                        try {
                            id = data.getString("id");
                            String email = data.getString("email");

                            Toast.makeText(LoginActivity.this, "You has logged in", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("userID", id);
                            intent.putExtra("userEmail", email);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    Log.e("checkLogin", "Error : " + message);
                }
            });

        }, 500);


        EditText txtEmail = findViewById(R.id.txtEmail);
        EditText txtPassword = findViewById(R.id.txtPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();

            LoginHandler.login(LoginActivity.this, txtEmail.getText().toString(), txtPassword.getText().toString(), new LoginHandler.CallBack() {
                @Override
                public void onSuccess(JSONObject data) {
                    runOnUiThread(() -> {
                        try {
                            String id = data.getString("id");
                            String email = data.getString("email");

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("userID", id);
                            intent.putExtra("userEmail", email);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, "Error parsing JSON", Toast.LENGTH_LONG).show();
                            throw new RuntimeException(e);
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Log.e("error", message);
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });


    }
}