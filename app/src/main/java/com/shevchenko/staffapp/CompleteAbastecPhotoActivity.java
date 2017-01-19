package com.shevchenko.staffapp;

/**
 * Created by shevchenko on 2015-11-26.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.shevchenko.staffapp.Common.Common;
import com.shevchenko.staffapp.Model.PhotoListAdapter;

import java.util.ArrayList;

public class CompleteAbastecPhotoActivity extends Activity implements View.OnClickListener {

    private ProgressDialog mProgDlg;
    ListView photoList;
    ArrayList<String> mPaths;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completeabastecphoto);
        int taskId = getIntent().getIntExtra("taskid", 0);

        photoList = (ListView) findViewById(R.id.listPhoto);
        mPaths = new ArrayList<String>();
        for(int i = 0; i < Common.getInstance().arrCompleteTasks.size(); i++){
            if(Common.getInstance().arrCompleteTasks.get(i).taskid == taskId){
                if(!Common.getInstance().arrCompleteTasks.get(i).file1.equals(""))
                    mPaths.add(Common.getInstance().arrCompleteTasks.get(i).file1);
                if(!Common.getInstance().arrCompleteTasks.get(i).file2.equals(""))
                    mPaths.add(Common.getInstance().arrCompleteTasks.get(i).file2);
                if(!Common.getInstance().arrCompleteTasks.get(i).file3.equals(""))
                    mPaths.add(Common.getInstance().arrCompleteTasks.get(i).file3);
                if(!Common.getInstance().arrCompleteTasks.get(i).file4.equals(""))
                    mPaths.add(Common.getInstance().arrCompleteTasks.get(i).file4);
                if(!Common.getInstance().arrCompleteTasks.get(i).file5.equals(""))
                    mPaths.add(Common.getInstance().arrCompleteTasks.get(i).file5);
            }
        }

        PhotoListAdapter path_adapter = new PhotoListAdapter(CompleteAbastecPhotoActivity.this, mPaths);
        photoList.setAdapter(path_adapter);
        photoList.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
        btnBack = (Button)findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }
}
