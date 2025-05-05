package com.example.qreach;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

public class QRScanner extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new IntentIntegrator(this)
                .setPrompt("Scan QR")
                .setOrientationLocked(true)
                .setBeepEnabled(true)
                .setCaptureActivity(CaptureActivity.class)
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "cancelled", Toast.LENGTH_LONG).show();
            }else {
                String scannedData = result.getContents();
                Toast.makeText(this, "scanned result : " + scannedData, Toast.LENGTH_LONG).show();

                Intent intent = new Intent();
                intent.putExtra("Scanned Result", scannedData);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
