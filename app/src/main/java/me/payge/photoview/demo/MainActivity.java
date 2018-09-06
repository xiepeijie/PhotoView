package me.payge.photoview.demo;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import me.payge.photoview.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView photoView = findViewById(R.id.photo_1);
        photoView.setOnClickListener(this);
        photoView = findViewById(R.id.photo_2);
        photoView.setOnClickListener(this);
        photoView = findViewById(R.id.photo_3);
        photoView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.setTransitionName("profile");
        }
        Intent intent =  new Intent(getApplication(), PreviewPhotoActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(MainActivity.this, v, "profile");
        startActivity(intent, options.toBundle());

    }
}
