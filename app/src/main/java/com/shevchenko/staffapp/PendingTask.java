package com.shevchenko.staffapp;
/*
This is the pending task list of the main screen.
when the user click the pending task, the event appears in this files.
 */
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shevchenko.staffapp.Common.Common;
import com.shevchenko.staffapp.Model.GpsInfo;
import com.shevchenko.staffapp.Model.TaskInfo;
import com.shevchenko.staffapp.db.DBManager;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("ValidFragment")
public class PendingTask extends Fragment {
    Context mContext;
    private LinearLayout lnTasks;
    private ComponentName mService;
////////////2016--04-26 changes///////////

    public PendingTask(Context context) {
        mContext = context;
    }

    public PendingTask() {
        this.mContext = null;
    }

    public static PendingTask getInstance(Context context) {
        PendingTask pendingtask = new PendingTask();
        pendingtask.mContext = context;
        return pendingtask;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_pending, null);
        lnTasks = (LinearLayout) view.findViewById(R.id.lnTasks);
        lnTasks.removeAllViews();
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++){
            TaskInfo task = Common.getInstance().arrIncompleteTasks.get(i);
            double distance = 0;

            Location locationA = new Location("point A");

            locationA.setLatitude(Double.parseDouble(task.latitude));
            locationA.setLongitude(Double.parseDouble(task.longitude));

            Location locationB = new Location("point B");
            if (!Common.getInstance().latitude.equals("")) {
                locationB.setLatitude(Double.parseDouble(Common.getInstance().latitude));
                locationB.setLongitude(Double.parseDouble(Common.getInstance().longitude));

                distance = locationA.distanceTo(locationB);
            }
            int dis = (int)((distance / 1000) * 10);
            float result = (float)dis / 10;
            task.distance = String.valueOf(result);
            //task.distance = String.valueOf(distance / 1000);
            DBManager.getManager().updateInCompleteTaskDistance(task);
        }
        Common.getInstance().arrIncompleteTasks.clear();
        Common.getInstance().arrIncompleteTasks = DBManager.getManager().getInCompleteTask(Common.getInstance().getLoginUser().getUserId());
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
            final TaskInfo task = Common.getInstance().arrIncompleteTasks.get(i);
            if (Common.getInstance().isPendingTaks(task.getTaskID()))
                continue;

            LinearLayout aRow = (LinearLayout) View.inflate(mContext, R.layout.row_pendingtask, null);
            LinearLayout.LayoutParams paramSet = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramSet.topMargin = (int) getResources().getDimension(R.dimen.space_5);
            paramSet.leftMargin = (int) getResources().getDimension(R.dimen.space_5);
            paramSet.rightMargin = (int) getResources().getDimension(R.dimen.space_5);

            TextView txtTitle = (TextView) aRow.findViewById(R.id.title);
            TextView txtValue2 = (TextView) aRow.findViewById(R.id.txtField2);
            TextView txtValue3 = (TextView) aRow.findViewById(R.id.txtField3);
            TextView txtTaskType = (TextView) aRow.findViewById(R.id.txtTaskType);
            TextView txtDistance = (TextView) aRow.findViewById(R.id.txtDistance);
            txtTitle.setText(task.getCustomer());
            txtValue2.setText(task.getAdress() + ", " + task.getLocationDesc());
            SpannableString content = new SpannableString(task.getTaskBusinessKey());
            content.setSpan(new UnderlineSpan(), 0, task.getTaskBusinessKey().length(), 0);
            txtValue3.setText(content);
            txtDistance.setText(task.distance);
            SpannableString content_epv = new SpannableString(task.getepv());
            content_epv.setSpan(new UnderlineSpan(), 0, task.getepv().length(), 0);
            txtTaskType.setText(content_epv);

            aRow.setLayoutParams(paramSet);

            aRow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    //setService("The user clicks the Incompleted Task list. taskid=" + String.valueOf(task.getTaskID()));
                    Intent intent;
                    setService(task.taskID, "The user presses the pending task.");
                    if (task.getTaskType().equals("1")) {
                        intent = new Intent(mContext, TaskActivity.class);
                        intent.putExtra("taskid", task.getTaskID());
                        intent.putExtra("date", task.getDate());
                        intent.putExtra("tasktype", task.getTaskType());
                        startActivity(intent);
                    } else if (task.getTaskType().equals("2")) {
                        intent = new Intent(mContext, TinTaskActivity.class);
                        intent.putExtra("taskid", task.getTaskID());
                        intent.putExtra("date", task.getDate());
                        intent.putExtra("tasktype", task.getTaskType());
                        intent.putExtra("RutaAbastecimiento", task.getRutaAbastecimiento());
                        intent.putExtra("Taskbusinesskey", task.getTaskBusinessKey());
                        startActivity(intent);
                    } else if (task.getTaskType().equals("4")) {
                        intent = new Intent(mContext, AbaTaskActivity.class);
                        intent.putExtra("taskid", task.getTaskID());
                        intent.putExtra("date", task.getDate());
                        intent.putExtra("tasktype", task.getTaskType());
                        intent.putExtra("RutaAbastecimiento", task.getRutaAbastecimiento());
                        intent.putExtra("Taskbusinesskey", task.getTaskBusinessKey());
                        intent.putExtra("MachineType", task.getMachineType());
                        startActivity(intent);
                    }
                }
            });
            lnTasks.addView(aRow);
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            Intent i = new Intent();
            i.setComponent(mService);
            mContext.stopService(i);
        }
    }

    public void setService(int nTaskID, String description) {

        GpsInfo info = new GpsInfo(mContext);
        Intent service = new Intent(mContext, LogService.class);
        service.putExtra("userid", Common.getInstance().getLoginUser().getUserId());
        service.putExtra("taskid", String.valueOf(nTaskID));
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        service.putExtra("datetime", time);
        service.putExtra("description", description);
        service.putExtra("latitude", Common.getInstance().latitude);
        service.putExtra("longitude", Common.getInstance().longitude);
        mService = mContext.startService(service);
    }
}
