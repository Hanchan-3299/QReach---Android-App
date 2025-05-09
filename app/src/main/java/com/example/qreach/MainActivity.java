package com.example.qreach;

import android.content.Intent;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.qreach.apihandler.AbsenHandler;
import com.example.qreach.apihandler.LoginHandler;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private String userId, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        userId = getIntent().getStringExtra("userID");
        userEmail = getIntent().getStringExtra("userEmail");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnLogout = findViewById(R.id.bntLogout);
        btnLogout.setOnClickListener(v -> {
            LoginHandler.logout(MainActivity.this, new LoginHandler.CallBack() {
                @Override
                public void onSuccess(JSONObject data) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "You has logout successfully", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error : " + message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });


        Button btnScanner = findViewById(R.id.btnScanner);
        btnScanner.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QRScanner.class);
            startActivityForResult(intent, 123);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode == RESULT_OK && data != null) {
            String scannedResult = data.getStringExtra("Scanned Result");
//            Toast.makeText(this, "QR Result : " + scannedResult, Toast.LENGTH_LONG).show();

            AbsenHandler.sendAbsen(this, userId, userEmail, scannedResult, new AbsenHandler.CallBack() {
                @Override
                public void onSuccess(JSONObject data) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Absen success", Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Absen failed", Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }
}