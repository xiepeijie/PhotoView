package me.payge.photoview.demo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import me.payge.photoview.PhotoView;
import me.payge.photoview.R;

public class PreviewPhotoActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        PhotoView photoView = findViewById(R.id.photo_preview);
        photoView.enable();
    }
}
