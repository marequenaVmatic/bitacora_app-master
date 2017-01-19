package com.shevchenko.staffapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.shevchenko.staffapp.Common.Common;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shevchenko on 2016-02-24.
 */
public class TinSignature extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        findViewById(R.id.btnSave).setOnClickListener(this);
        Common.getInstance().signaturePath = "";
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent;
        switch (v.getId()) {
            case R.id.btnSave:
                Bitmap bmp = ((SignatureView) findViewById(R.id.signature)).getImage();
                try {
                    String root = Environment.getExternalStorageDirectory() + "/staffapp/";
                    String strFileName = "signature" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";

                    String filepath = root + strFileName;
                    Common.getInstance().signaturePath = filepath;
                    FileOutputStream fos = new FileOutputStream(filepath);
                    bmp.compress(CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                }
                catch(Exception e) {
                    Log.e("Could not save", e.getMessage());
                    e.printStackTrace();
                }
                onBackPressed();
                break;
        }
    }
}
