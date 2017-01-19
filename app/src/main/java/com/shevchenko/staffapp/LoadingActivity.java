package com.shevchenko.staffapp;
/*
This screen is loading screen.
 */
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;


public class LoadingActivity extends Activity {
    public static Activity loadingActivity;

    private TextView mTxtLoading;
    private boolean mNeedSync;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        loadingActivity = LoadingActivity.this;

        mTxtLoading = (TextView)findViewById(R.id.txtLoading);
        mNeedSync = getIntent().getBooleanExtra("needSync", false);

        if(mNeedSync) {
            mTxtLoading.setText(R.string.loading_3hours);
        } else {
            mTxtLoading.setText(R.string.loading);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        System.exit(0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
/*        ImageView imgView = (ImageView) findViewById(R.id.imageView1);
        imgView.setVisibility(ImageView.VISIBLE);
        imgView.setBackgroundResource(R.anim.animation);
        AnimationDrawable frameAnimation = (AnimationDrawable) imgView.getBackground();
        frameAnimation.start();*/
        super.onWindowFocusChanged(hasFocus);
    }
}
