package com.digi.xbee.sample.android.bleconfiguration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OpenScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_screen);

        Button buttonWith = findViewById(R.id.buttonWith);
        buttonWith.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OpenScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button buttonWithout = findViewById(R.id.buttonWithout);
        buttonWithout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OpenScreen.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
}
