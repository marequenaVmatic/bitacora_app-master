package com.shevchenko.staffapp;
/*
This is the Completed task list of the main screen.
 when the user click the completed task, the event appears in this files.
*/
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shevchenko.staffapp.Common.Common;
import com.shevchenko.staffapp.Model.CompleteTask;
import com.shevchenko.staffapp.Model.LocationLoader;
import com.shevchenko.staffapp.db.DBManager;

import java.io.File;

@SuppressLint("ValidFragment")
public class CompletedTask extends Fragment {
    Context mContext;
    private LinearLayout lnTasks;
    private LocationLoader mLocationLoader;
    public CompletedTask(Context context) {
        mContext = context;
    }
    public CompletedTask(){
        this.mContext = null;
        this.mLocationLoader = null;
    }
    public static CompletedTask getInstance(Context context, LocationLoader loader){
        CompletedTask pendingtask = new CompletedTask();
        pendingtask.mContext = context;
        pendingtask.mLocationLoader = loader;
        return pendingtask;
    }
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_pending, null);
        lnTasks = (LinearLayout) view.findViewById(R.id.lnTasks);
        lnTasks.removeAllViews();
        Common.getInstance().arrPendingTasks = DBManager.getManager().getPendingTask(Common.getInstance().getLoginUser().getUserId());
        /*
        for(int i = 0; i < Common.getInstance().arrPendingTasks.size(); i++)
        {
            final PendingTasks task = Common.getInstance().arrPendingTasks.get(i);

            LinearLayout aRow = (LinearLayout)View.inflate(mContext, R.layout.row_task, null);
            LinearLayout.LayoutParams paramSet = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramSet.topMargin = (int) getResources().getDimension(R.dimen.space_5);
            paramSet.leftMargin = (int) getResources().getDimension(R.dimen.space_5);
            paramSet.rightMargin = (int) getResources().getDimension(R.dimen.space_5);

            TextView txtTitle = (TextView) aRow.findViewById(R.id.txtTitle);
            TextView txtSummary = (TextView) aRow.findViewById(R.id.txtField2);
            TextView txtTaskBusinessKey = (TextView) aRow.findViewById(R.id.txtField3);
            ImageView img = (ImageView) aRow.findViewById(R.id.imgTask);
            txtTitle.setText(task.Customer);
            txtSummary.setText(task.Adress + ", " + task.LocationDesc);
            txtTaskBusinessKey.setText(task.TaskBusinessKey);
            if(task.file1 != null){
                File imgFile = new File(task.file1);
                Bitmap bitmap = loadLargeBitmapFromFile(imgFile.getAbsolutePath(), mContext);

                img.setImageBitmap(bitmap);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            aRow.setLayoutParams(paramSet);

            aRow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    //setService("The user clicks the Pending Task list. taskid=" + String.valueOf(task.taskid));
                    if(task.tasktype.equals("1")){
                        Intent intent = new Intent(mContext, CompletedTaskActivity.class);
                        intent.putExtra("taskid", task.taskid);
                        startActivity(intent);
                    }else if(task.tasktype.equals("2")){
                        Intent intent = new Intent(mContext, CompletedTinTaskActivity.class);
                        intent.putExtra("taskid", task.taskid);
                        startActivity(intent);
                    }else if(task.tasktype.equals("4")){
                        Intent intent = new Intent(mContext, CompletedTinTaskActivity.class);
                        intent.putExtra("taskid", task.taskid);
                        startActivity(intent);
                    }
                }
            });

            //aRow.findViewById(R.id.imgStatus).setVisibility(View.VISIBLE);
            ImageView imgStaus = (ImageView) aRow.findViewById(R.id.imgStatus);
            imgStaus.setImageDrawable(getResources().getDrawable(R.drawable.clock));
            lnTasks.addView(aRow);
        }*/
        for(int i = 0; i < Common.getInstance().arrCompleteTasks.size(); i++)
        {
            final CompleteTask task = Common.getInstance().arrCompleteTasks.get(i);

            LinearLayout aRow = (LinearLayout)View.inflate(mContext, R.layout.row_task, null);
            LinearLayout.LayoutParams paramSet = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramSet.topMargin = (int) getResources().getDimension(R.dimen.space_5);
            paramSet.leftMargin = (int) getResources().getDimension(R.dimen.space_5);
            paramSet.rightMargin = (int) getResources().getDimension(R.dimen.space_5);

            TextView txtTitle = (TextView) aRow.findViewById(R.id.txtTitle);
            TextView txtSummary = (TextView) aRow.findViewById(R.id.txtField2);
            TextView txtTaskBusinessKey = (TextView) aRow.findViewById(R.id.txtField3);
            ImageView imageTask = (ImageView) aRow.findViewById(R.id.imgTask);
            txtTitle.setText(task.Customer);
            txtSummary.setText(task.Adress + ", " + task.LocationDesc);
            txtTaskBusinessKey.setText(task.TaskBusinessKey);
            if(!task.file1.equals("")){
                File imgFile = new File(task.file1);
                Bitmap bitmap = loadLargeBitmapFromFile(imgFile.getAbsolutePath(), mContext);

                imageTask.setImageBitmap(bitmap);
                imageTask.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            aRow.setLayoutParams(paramSet);

            aRow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    //setService("The user clicks the Completed Task list. taskid=" + String.valueOf(task.taskid));
                    mLocationLoader.Stop();
                    if(task.tasktype.equals("1")){
                        Intent intent = new Intent(mContext, CompletedTaskActivity.class);
                        intent.putExtra("taskid", task.taskid);
                        startActivity(intent);
                    }else if(task.tasktype.equals("2")){
                        Intent intent = new Intent(mContext, CompletedTinTaskActivity.class);
                        intent.putExtra("taskid", task.taskid);
                        startActivity(intent);
                    }else if(task.tasktype.equals("4")){
                        Intent intent = new Intent(mContext, CompleteAbaTaskActivity.class);
                        intent.putExtra("taskid", task.taskid);
                        startActivity(intent);
                    }
                }
            });

            //aRow.findViewById(R.id.imgStatus).setVisibility(View.VISIBLE);
            ImageView imgStaus = (ImageView) aRow.findViewById(R.id.imgStatus);
            imgStaus.setImageDrawable(getResources().getDrawable(R.drawable.check));

            lnTasks.addView(aRow);
        }

        return view;
    }
    private Bitmap loadLargeBitmapFromFile(String strPath, Context context)
    {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(strPath, opt);

        int REQUIRED_SIZE = 480;
        int width_tmp = opt.outWidth, height_tmp = opt.outHeight;
        int scale = 1;
        while(true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE) break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options opt1 = new BitmapFactory.Options();
        opt1.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeFile(strPath, opt1);
        return bitmap;

    }

}
