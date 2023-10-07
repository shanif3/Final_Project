package com.digi.xbee.sample.android.bleconfiguration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private DatabaseGateway databaseGateway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.open_screen);

            Button buttonWith = findViewById(R.id.buttonWith);
            buttonWith.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            });

            Button buttonWithout = findViewById(R.id.buttonWithout);
            buttonWithout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            });
            PermissionUtils.requestLocationPermissions(this, MY_LOCATION_PERMISSION_REQUEST_CODE, false);
            databaseGateway = DatabaseGateway.getINSTANCE(getApplicationContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
