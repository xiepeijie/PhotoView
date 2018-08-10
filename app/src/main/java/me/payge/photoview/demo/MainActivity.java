package me.payge.photoview.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import me.payge.photoview.PhotoView;
import me.payge.photoview.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PhotoView photoView = findViewById(R.id.photo);
        photoView.enable();
    }
}
