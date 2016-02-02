package com.example.heli.msync;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void filePicker(View view){
        Toast.makeText(MainActivity.this, "file picker pressed", Toast.LENGTH_SHORT).show();
    }

    public void connect(View view){
        Toast.makeText(MainActivity.this, "connect pressed", Toast.LENGTH_SHORT).show();

    }

    public void listen(View view){
        Toast.makeText(MainActivity.this, "listen pressed", Toast.LENGTH_SHORT).show();

    }

    public void synchronize(View view){
        Toast.makeText(MainActivity.this, "synchronize pressed", Toast.LENGTH_SHORT).show();

    }
}
