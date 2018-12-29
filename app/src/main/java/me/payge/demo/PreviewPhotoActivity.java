package me.payge.demo;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import me.payge.evenbus.EventBus;
import me.payge.photoview.PhotoView;
import me.payge.photoview.R;

public class PreviewPhotoActivity extends FragmentActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(0);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        final int i = getIntent().getIntExtra("i", 0);
        setContentView(R.layout.activity_preview);
        ViewPager previewPager = findViewById(R.id.preview);
        previewPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 10));
        previewPager.setAdapter(new PagerAdapter() {
            @Override
            public void finishUpdate(@NonNull ViewGroup container) {
                super.finishUpdate(container);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startPostponedEnterTransition();
                }
            }

            @Override
            public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                super.setPrimaryItem(container, position, object);
                int count = container.getChildCount();
                View child;
                for (int j = 0; j < count; j++) {
                    child = container.getChildAt(j);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (child == object) {
                            child.setTransitionName("profile" + i);
                        } else {
                            child.setTransitionName(null);
                        }
                    }
                }
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                PhotoView photoView = new PhotoView(container.getContext());
                photoView.enable();
                photoView.setOnClickListener(PreviewPhotoActivity.this);
                int img = 0;
                switch (position) {
                    case 0:
                        img = R.mipmap.img1;
                        break;
                    case 1:
                        img = R.mipmap.img2;
                        break;
                    case 2:
                        img = R.mipmap.img3;
                }
                photoView.setImageResource(img);
                container.addView(photoView);
                return photoView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }
        });
        previewPager.setCurrentItem(i, false);
        previewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                EventBus.getInstance().post(position);
            }
        });
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }
}
