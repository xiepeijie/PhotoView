package me.payge.demo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import me.payge.evenbus.EventBus;
import me.payge.evenbus.EventObserver;
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
        EventBus.getInstance().register(this);
    }

    @Override
    public void onClick(View v) {
        int i = 0;
        switch (v.getId()) {
            case R.id.photo_1:
                break;
            case R.id.photo_2:
                i = 1;
                break;
            case R.id.photo_3:
                i = 2;
        }
        openPreview(v, i);
    }

    private void openPreview(View v, int i) {
        String name = "profile";
        name += i;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.setTransitionName(name);
        }
        Intent intent =  new Intent(getApplication(), PreviewPhotoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("i", i);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(MainActivity.this, v, name);
        startActivity(intent, options.toBundle());
    }

    @EventObserver
    public void onPageChanged(Integer i) {
        Log.i("xxx", "onPageChanged: " + i);
    }
}
