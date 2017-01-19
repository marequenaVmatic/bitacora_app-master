package com.shevchenko.staffapp;
/*
This screen is for Abastec list.
When the user press one item in the list, the abastec input screen appears.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapsInitializer;
import com.shevchenko.staffapp.Common.Common;
import com.shevchenko.staffapp.Model.CompleteTask;
import com.shevchenko.staffapp.Model.CompltedTinTask;
import com.shevchenko.staffapp.Model.GpsInfo;
import com.shevchenko.staffapp.Model.LocationLoader;
import com.shevchenko.staffapp.Model.PendingTasks;
import com.shevchenko.staffapp.Model.Producto;
import com.shevchenko.staffapp.Model.Producto_RutaAbastecimento;
import com.shevchenko.staffapp.Model.TaskInfo;
import com.shevchenko.staffapp.Model.TinTask;
import com.shevchenko.staffapp.db.DBManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Pattern;

public class AbastecTinTaskActivity extends Activity implements View.OnClickListener {

    private LinearLayout lnContainer;
    private ProgressDialog mProgDlg;
    private ComponentName mService;
    private int nTaskID;
    private ArrayList<Producto> currentProductos = new ArrayList<Producto>();
    LocationLoader mLocationLoader;
    private Location mNewLocation;
    private RelativeLayout RnButtons;
    private TaskInfo mTaskInfo;
    private String mSelectedNus = "";
    private String mSelectedQuantity = "";
    private boolean switchBackDlg = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abastectintask);
        Common.getInstance().signaturePath = "";
        Common.getInstance().selectedNus = "";
        Common.getInstance().selectedQuantity = "";
        nTaskID = getIntent().getIntExtra("taskid", 0);
        mTaskInfo = DBManager.getManager().getTaskInfo(nTaskID);
//        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
//            mTaskInfo = Common.getInstance().arrIncompleteTasks.get(i);
//            if (mTaskInfo.getTaskID() == nTaskID) {
//                String actiondate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
//                break;
//            }
//        }
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

///////////////////////////////
        currentProductos.clear();
        //ArrayList<String> lstCus = new ArrayList<String>();
        ArrayList<Producto> lstCus = new ArrayList<Producto>();
        lstCus = DBManager.getManager().getProductos_CUS(mTaskInfo.RutaAbastecimiento, mTaskInfo.MachineType, mTaskInfo.taskType);
        currentProductos = DBManager.getManager().getProductos_CUS(mTaskInfo.RutaAbastecimiento, mTaskInfo.MachineType, mTaskInfo.taskType);
//        for (int i = 0; i < Common.getInstance().arrProducto.size(); i++) {
//            for (int j = 0; j < lstCus.size(); j++) {
//                if (Common.getInstance().arrProducto.get(i).cus.equals(lstCus.get(j))) {
//                    currentProductos.add(Common.getInstance().arrProducto.get(i));
//                    break;
//                }
//            }
//        }
        Collections.sort(currentProductos, new Comparator<Producto>() {
            @Override
            public int compare(Producto lhs, Producto rhs) {
                return lhs.nus.compareToIgnoreCase(rhs.nus);
            }
        });
        loadProductos();
    }

    private void getLocation() {
        if (mNewLocation == null)
            return;
        Common.getInstance().latitude = String.valueOf(mNewLocation.getLatitude());
        Common.getInstance().longitude = String.valueOf(mNewLocation.getLongitude());
    }
    //load the abastec list and show in the screen.
    private void loadProductos() {
        String strData = getSharedPreferences(Common.PREF_KEY_TEMPSAVE, MODE_PRIVATE).getString(Common.PREF_KEY_TEMPSAVE_ABASTEC + nTaskID, "");
        String[] arrData = strData.split(";");
        for (int i = 0; i < currentProductos.size(); i++) {
            LinearLayout lnChild = new LinearLayout(AbastecTinTaskActivity.this);
            final int a = i;
            final Producto productCurrent  = currentProductos.get(i);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = (int) getResources().getDimension(R.dimen.space_10);
            params.rightMargin = (int) getResources().getDimension(R.dimen.space_10);
            params.topMargin = (int) getResources().getDimension(R.dimen.space_20);
            params.gravity = Gravity.CENTER;
            lnChild.setLayoutParams(params);
            lnChild.setOrientation(LinearLayout.HORIZONTAL);
            lnContainer.addView(lnChild, i);

            TextView txtContent = new TextView(AbastecTinTaskActivity.this);
            LinearLayout.LayoutParams param_text = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT/*(int) getResources().getDimension(R.dimen.space_40)*/);
            param_text.weight = 70;
            param_text.gravity = Gravity.CENTER_VERTICAL;
            txtContent.setText(currentProductos.get(i).nus + ":");
            txtContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.space_15));
            txtContent.setLayoutParams(param_text);
            txtContent.setTextColor(getResources().getColor(R.color.clr_graqy));
            lnChild.addView(txtContent);

            final TextView txtQuantity = new TextView(AbastecTinTaskActivity.this);
            LinearLayout.LayoutParams param_content = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            param_content.weight = 30;
            param_content.gravity = Gravity.CENTER;
            param_content.leftMargin = (int) getResources().getDimension(R.dimen.space_3);
            txtQuantity.setPadding((int) getResources().getDimension(R.dimen.space_5), (int) getResources().getDimension(R.dimen.space_5), (int) getResources().getDimension(R.dimen.space_5), (int) getResources().getDimension(R.dimen.space_5));
            txtQuantity.setGravity(Gravity.CENTER);
            txtQuantity.setLayoutParams(param_content);
            txtQuantity.setId(i + 1);
            if(i < arrData.length) {
                if(!arrData[i].equals(""))
                    txtQuantity.setText(arrData[i]);
                else
                    txtQuantity.setText("0");
            }else
                txtQuantity.setText("0");
            txtQuantity.setBackgroundResource(R.drawable.tineditborder);
            txtQuantity.setTextColor(getResources().getColor(R.color.clr_graqy));
            txtQuantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.space_15));
            lnChild.addView(txtQuantity);
            lnChild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AbastecTinTaskActivity.this, AbastecTinTaskEditActivity.class);
                    mSelectedQuantity = "";
                    mSelectedNus = "";
                    //intent.putExtra("nus", currentProductos.get(a).nus);
                    intent.putExtra("nus", productCurrent.nus);
                    intent.putExtra("product", productCurrent);
                    intent.putExtra("quantity", txtQuantity.getText().toString());
                    intent.putExtra("taskId", nTaskID);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if(switchBackDlg)
            super.onBackPressed();
        else{
            AbastecDlg dlg = new AbastecDlg(this);
            Common.getInstance().isAbastec = true;
            dlg.setTitle("Confirmar abastecimiento");
            dlg.setTinTasks(Common.getInstance().arrAbastTinTasks);
            dlg.setCancelable(false);
            dlg.setListener(mCancelListener);
            dlg.show();
            switchBackDlg=true;
        }

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
        if (!Common.getInstance().signaturePath.equals(""))
            findViewById(R.id.btn_signature).setBackgroundColor(Color.GREEN);
        mSelectedNus = Common.getInstance().selectedNus;
        mSelectedQuantity = Common.getInstance().selectedQuantity;
        if (!mSelectedNus.equals("") && !mSelectedQuantity.equals("")) {
            for (int i = 0; i < currentProductos.size(); i++) {
                if (currentProductos.get(i).nus.equals(mSelectedNus)) {
                    TextView txtQuantity = (TextView) findViewById(i + 1);
                    txtQuantity.setText(mSelectedQuantity);
                }
            }
        }
    }
    //when the user press the Guardar button, the abastec data will be saved at the Global variables of the app.
    private void addPendingTask() {

        TaskInfo taskInfo;
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
            taskInfo = Common.getInstance().arrIncompleteTasks.get(i);
            if (taskInfo.getTaskID() == nTaskID) {
                Common.getInstance().arrAbastTinTasks.clear();
                String strData = "";
                for (int j = 0; j < currentProductos.size(); j++) {
                    TextView txtQuantity = (TextView) findViewById(j + 1);
                    String quantity = txtQuantity.getText().toString();
                    strData += quantity + ";";
                    if(!quantity.equals("")) {
                        if (Integer.parseInt(quantity) != 0) {
                            TinTask tinInfo = new TinTask(Common.getInstance().getLoginUser().getUserId(), nTaskID, taskInfo.getTaskType(), taskInfo.getRutaAbastecimiento(), currentProductos.get(j).cus, currentProductos.get(j).nus, quantity);
                            for (int z = 0; z <  Common.getInstance().arrAbastTinTasks.size(); z++) {
                                if(Common.getInstance().arrAbastTinTasks.get(z).taskid==nTaskID && Common.getInstance().arrAbastTinTasks.get(z).cus == currentProductos.get(j).cus){
                                    Common.getInstance().arrAbastTinTasks.remove(z);
                                }
                            }
                            DBManager.getManager().insertPendingTinTask(tinInfo);
                            Common.getInstance().arrAbastTinTasks.add(tinInfo);
                        }
                    }
                }
                SharedPreferences.Editor editor = getSharedPreferences(Common.PREF_KEY_TEMPSAVE, MODE_PRIVATE).edit();
                editor.putString(Common.PREF_KEY_TEMPSAVE_ABASTEC + nTaskID, strData);
                editor.commit();
                break;
            }
        }
    }
    private AbastecDlg.OnCancelOrderListener mCancelListener = new AbastecDlg.OnCancelOrderListener() {
        @Override
        public void OnCancel(String strReason, int iType) {
            if(iType == 1) {
                Common.getInstance().arrAbastTinTasks.clear();
                onBackPressed();
            } else {
                onBackPressed();
            }
        }
    };
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        AbastecDlg dlg;
        switch (v.getId()) {
            case R.id.btnSendForm:
                setService("The user clicks the Send Form Button");
                addPendingTask();
                dlg = new AbastecDlg(this);
                Common.getInstance().isAbastec = true;
                dlg.setTitle("Confirmar abastecimiento");
                dlg.setTinTasks(Common.getInstance().arrAbastTinTasks);
                dlg.setCancelable(false);
                dlg.setListener(mCancelListener);
                dlg.show();
                switchBackDlg=true;
                break;
            case R.id.btnBack:
                setService("The user clicks the Volver Button");
                addPendingTask();
                dlg = new AbastecDlg(this);
                dlg.setTitle("ABASTECIMIENTO 42810755 KIKKO MAX");
                dlg.setType(1);
                dlg.setTinTasks(Common.getInstance().arrAbastTinTasks);
                dlg.setCancelable(false);
                dlg.setListener(mCancelListener);
                dlg.show();
                switchBackDlg=true;
                //onBackPressed();
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

        Intent service = new Intent(AbastecTinTaskActivity.this, LogService.class);
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
