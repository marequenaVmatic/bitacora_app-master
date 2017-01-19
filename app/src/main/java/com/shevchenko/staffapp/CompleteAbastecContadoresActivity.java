package com.shevchenko.staffapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shevchenko.staffapp.Common.Common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CompleteAbastecContadoresActivity extends Activity implements View.OnClickListener {

    private LinearLayout lnContainer;
    private ComponentName mService;
    private int nTaskID;
    private RelativeLayout RnButtons;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completeabasteccontadores);
        Common.getInstance().signaturePath="";
        //Common.getInstance().arrAbastTinTasks.clear();
        nTaskID = getIntent().getIntExtra("taskid", 0);
        findViewById(R.id.btnBack).setOnClickListener(this);
        lnContainer = (LinearLayout) findViewById(R.id.lnContainer);
        lnContainer.requestFocus();

        RnButtons = (RelativeLayout) findViewById(R.id.RnButtons);
        RnButtons.setVisibility(View.VISIBLE);

        loadProductos();

    }
    private void loadProductos(){
        for (int i = 0; i < Common.getInstance().arrCompleteDetailCounters.size(); i++) {
            if (Common.getInstance().arrCompleteDetailCounters.get(i).taskid.equals(String.valueOf(nTaskID))) {
                LinearLayout lnChild = new LinearLayout(CompleteAbastecContadoresActivity.this);
                final int a = i;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params.leftMargin = (int) getResources().getDimension(R.dimen.space_10);
                params.rightMargin = (int) getResources().getDimension(R.dimen.space_10);
                params.topMargin = (int) getResources().getDimension(R.dimen.space_5);
                lnChild.setLayoutParams(params);
                lnChild.setOrientation(LinearLayout.HORIZONTAL);
                //lnContainer.addView(lnChild, i);
                lnContainer.addView(lnChild, lnContainer.getChildCount());

                TextView txtContent = new TextView(CompleteAbastecContadoresActivity.this);
                LinearLayout.LayoutParams param_text = new LinearLayout.LayoutParams(0, (int) getResources().getDimension(R.dimen.space_40));
                param_text.weight = 80;
                param_text.gravity = Gravity.CENTER;
                //txtContent.setText(currentProductos.get(i).cus + "-" + currentProductos.get(i).nus + ":");
                txtContent.setText(Common.getInstance().arrCompleteDetailCounters.get(i).CodCounter);
                txtContent.setLayoutParams(param_text);
                //txtContent.setTextSize((float) getResources().getDimension(R.dimen.space_15));
                txtContent.setTextColor(getResources().getColor(R.color.clr_graqy));
                lnChild.addView(txtContent);

                TextView txtQuantity = new TextView(CompleteAbastecContadoresActivity.this);
                LinearLayout.LayoutParams param_edt = new LinearLayout.LayoutParams(0, (int) getResources().getDimension(R.dimen.space_35));
                param_edt.weight = 20;
                param_edt.gravity = Gravity.CENTER;
                param_edt.leftMargin = (int) getResources().getDimension(R.dimen.space_3);
                txtQuantity.setLayoutParams(param_edt);
                txtQuantity.setBackgroundResource(R.drawable.back_edit);

                txtQuantity.setTextColor(getResources().getColor(R.color.clr_edit));
                txtQuantity.setText(Common.getInstance().arrCompleteDetailCounters.get(i).quantity);
                lnChild.addView(txtQuantity);
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!Common.getInstance().signaturePath.equals(""))
            findViewById(R.id.btn_signature).setBackgroundColor(Color.GREEN);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent;
        switch (v.getId()) {
            case R.id.btnBack:
                setService("The user clicks the Volver Button");
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            Intent i = new Intent();
            i.setComponent(mService);
            stopService(i);
        }
    }
    public void setService(String description) {

        Intent service = new Intent(CompleteAbastecContadoresActivity.this, LogService.class);
        service.putExtra("userid", Common.getInstance().getLoginUser().getUserId());
        service.putExtra("taskid", String.valueOf(nTaskID));
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        service.putExtra("datetime", time);
        service.putExtra("description", description);
        service.putExtra("latitude", Common.getInstance().latitude);
        service.putExtra("longitude", Common.getInstance().longitude);
        mService = startService(service);
    }
}
