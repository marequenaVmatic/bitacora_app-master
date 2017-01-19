package com.shevchenko.staffapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Created by Jimmy-PC on 6/2/2016.
 */
public class CreateTaskActivity extends Activity implements View.OnClickListener {
    private EditText mEdtTaskName;
    private LinearLayout mLnTaskDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createtask);

        mEdtTaskName = (EditText)findViewById(R.id.edtTaskName);
        mLnTaskDetail = (LinearLayout)findViewById(R.id.lnDetail);
        mLnTaskDetail.setVisibility(View.INVISIBLE);

        findViewById(R.id.btnBuscar).setOnClickListener(this);
        findViewById(R.id.btnConfirm).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnBuscar:
                if(!mEdtTaskName.getText().toString().equals("")) {

                }
                break;
            case R.id.btnConfirm:
                break;
        }
    }
}
