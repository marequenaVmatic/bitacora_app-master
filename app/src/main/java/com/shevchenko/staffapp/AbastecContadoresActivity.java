package com.shevchenko.staffapp;
/*
This is the screen when the user presses the Contadores button.
This screen shows the Contadores list informations.
 */
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapsInitializer;
import com.shevchenko.staffapp.Common.Common;
import com.shevchenko.staffapp.Model.DetailCounter;
import com.shevchenko.staffapp.Model.LocationLoader;
import com.shevchenko.staffapp.Model.MachineCounter;
import com.shevchenko.staffapp.Model.TaskInfo;
import com.shevchenko.staffapp.db.DBManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class AbastecContadoresActivity extends Activity implements View.OnClickListener {

    private LinearLayout lnContainer;
    private ProgressDialog mProgDlg;

    private ComponentName mService;
    private int nTaskID;
    private String latitude, longitude;
    private ArrayList<MachineCounter> currentMachine = new ArrayList<MachineCounter>();
    LocationLoader mLocationLoader;
    private Location mNewLocation;
    private RelativeLayout RnButtons;
    private Boolean isEnter = false;
    private TaskInfo mTaskInfo;
    //private final int DYNAMIC_EDIT_ID = 0x8000;
////////////2016--04-26 changes///////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contadores);
        Common.getInstance().signaturePath="";
        //Common.getInstance().arrAbastTinTasks.clear();
        nTaskID = getIntent().getIntExtra("taskid", 0);
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
            mTaskInfo = Common.getInstance().arrIncompleteTasks.get(i);
            if (mTaskInfo.getTaskID() == nTaskID) {
                String actiondate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                latitude = mTaskInfo.getLatitude();
                longitude = mTaskInfo.getLongitude();
                break;
            }
        }
        findViewById(R.id.btnSendForm).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
        lnContainer = (LinearLayout) findViewById(R.id.lnContainer);
        lnContainer.requestFocus();

        RnButtons = (RelativeLayout) findViewById(R.id.RnButtons);
        RnButtons.setVisibility(View.VISIBLE);

        mProgDlg = new ProgressDialog(this);
        mProgDlg.setCancelable(false);
        mProgDlg.setTitle("Posting Task!");
        mProgDlg.setMessage("Please Wait!");

        MapsInitializer.initialize(getApplicationContext());
        mLocationLoader = new LocationLoader(this, false);
        mLocationLoader
                .SetOnLoadEventListener(new LocationLoader.OnLoadEventListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        mNewLocation = location;
                        getLocation();
                        // mUpdateLocationHandler.sendEmptyMessage(0);
                    }

                    @Override
                    public void onAddressChanged(String strAddress) {

                    }

                    @Override
                    public void onError(int iErrorCode) {
                        getLocation();
                    }
                });
        mLocationLoader.Start();

        currentMachine.clear();
        currentMachine = DBManager.getManager().getMachineCounters(mTaskInfo.TaskBusinessKey);
        loadingMachine();

        //new Thread(mRunnable_producto).start();

    }
    private void getLocation() {
        if (mNewLocation == null)
            return;
        Common.getInstance().latitude = String.valueOf(mNewLocation.getLatitude());
        Common.getInstance().longitude = String.valueOf(mNewLocation.getLongitude());
    }
    private void loadingMachine(){
        String strData = getSharedPreferences(Common.PREF_KEY_TEMPSAVE, MODE_PRIVATE).getString(Common.PREF_KEY_TEMPSAVE_CONTADORES + nTaskID, "");
        String[] arrData = strData.split(";");
        for (int i = 0; i < currentMachine.size(); i++) {
            LinearLayout lnChild = new LinearLayout(AbastecContadoresActivity.this);
            final int a = i;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = (int) getResources().getDimension(R.dimen.space_10);
            params.rightMargin = (int) getResources().getDimension(R.dimen.space_10);
            params.topMargin = (int) getResources().getDimension(R.dimen.space_5);
            lnChild.setLayoutParams(params);
            lnChild.setOrientation(LinearLayout.HORIZONTAL);
            lnContainer.addView(lnChild, i);

            TextView txtContent = new TextView(AbastecContadoresActivity.this);
            LinearLayout.LayoutParams param_text = new LinearLayout.LayoutParams(0, (int) getResources().getDimension(R.dimen.space_40));
            param_text.weight = 80;
            param_text.gravity = Gravity.CENTER;
            //txtContent.setText(currentProductos.get(i).cus + "-" + currentProductos.get(i).nus + ":");
            txtContent.setText(currentMachine.get(i).CodContador);
            txtContent.setLayoutParams(param_text);
            //txtContent.setTextSize((float) getResources().getDimension(R.dimen.space_15));
            txtContent.setTextColor(getResources().getColor(R.color.clr_graqy));
            lnChild.addView(txtContent);

            final EditText edtContent = new EditText(AbastecContadoresActivity.this);
            LinearLayout.LayoutParams param_edt = new LinearLayout.LayoutParams(0, (int) getResources().getDimension(R.dimen.space_35));
            param_edt.weight = 20;
            param_edt.gravity = Gravity.CENTER;
            param_edt.leftMargin = (int) getResources().getDimension(R.dimen.space_5);
            edtContent.setLayoutParams(param_edt);
            ///edtContent.setTextSize((float) getResources().getDimension(R.dimen.space_15));
            edtContent.setTextColor(getResources().getColor(R.color.clr_edit));
            //edtContent.setId(DYNAMIC_EDIT_ID + i + 1);
            edtContent.setId(i + 1);
            if( i == 0) {
                edtContent.requestFocus();
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(edtContent.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            edtContent.setBackgroundResource(R.drawable.back_edit);
            //edtContent.setFilters(new InputFilter[]{filterNum});
            edtContent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            //edtContent.setText("0");
            edtContent.setHint("0");
            lnChild.addView(edtContent);
        }
        if(Common.getInstance().arrDetailCounters.size() != 0){
            for(int i = 0; i < Common.getInstance().arrDetailCounters.size(); i++){
                EditText edtContent = (EditText) findViewById(i + 1);
                edtContent.setText(Common.getInstance().arrDetailCounters.get(i).quantity);
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected InputFilter filterNum = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            Pattern ps = Pattern.compile("^[0-9]+$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        if(!Common.getInstance().signaturePath.equals(""))
            findViewById(R.id.btn_signature).setBackgroundColor(Color.GREEN);
    }
    //When the user press the Guardar button, the inputed data will be saved at the Global variables of the app.
    private void addPendingTask() {

        TaskInfo taskInfo;
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
            taskInfo = Common.getInstance().arrIncompleteTasks.get(i);
            if (taskInfo.getTaskID() == nTaskID) {
                Common.getInstance().arrDetailCounters.clear();
                String strData = "";
                for (int j = 0; j < currentMachine.size(); j++) {
                    EditText edtContent = (EditText) findViewById(j + 1);
                    String quantity = String.valueOf(edtContent.getText().toString());
                    if(quantity.equals(""))
                        quantity = "0";
                    strData += quantity + ";";
                    DetailCounter info = new DetailCounter(String.valueOf(nTaskID), currentMachine.get(j).CodContador, quantity);
                    Common.getInstance().arrDetailCounters.add(info);
                }
                SharedPreferences.Editor editor = getSharedPreferences(Common.PREF_KEY_TEMPSAVE, MODE_PRIVATE).edit();
                editor.putString(Common.PREF_KEY_TEMPSAVE_CONTADORES + nTaskID, strData);
                editor.commit();
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent;
        switch (v.getId()) {
            case R.id.btnSendForm:
                setService("The user clicks the Send Form Button");
                addPendingTask();
                onBackPressed();
                break;
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

        Intent service = new Intent(AbastecContadoresActivity.this, LogService.class);
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
