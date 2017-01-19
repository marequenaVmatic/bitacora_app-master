package com.shevchenko.staffapp;
/*
This activity is for tasktype 4.
This screen has the Abastec, Capture, CONTADORES, RECAUDAR, TOMAR FOTOGRAFIA DE MAQUINA functions.
 */
import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapsInitializer;
import com.shevchenko.staffapp.Common.Common;
import com.shevchenko.staffapp.Model.CompleteDetailCounter;
import com.shevchenko.staffapp.Model.CompleteTask;
import com.shevchenko.staffapp.Model.CompltedTinTask;
import com.shevchenko.staffapp.Model.DetailCounter;
import com.shevchenko.staffapp.Model.GpsInfo;
import com.shevchenko.staffapp.Model.LocationLoader;
import com.shevchenko.staffapp.Model.LogFile;
import com.shevchenko.staffapp.Model.MachineCounter;
import com.shevchenko.staffapp.Model.MenuItemButton;
import com.shevchenko.staffapp.Model.MenuListAdapter;
import com.shevchenko.staffapp.Model.PendingTasks;
import com.shevchenko.staffapp.Model.Producto;
import com.shevchenko.staffapp.Model.TaskInfo;
import com.shevchenko.staffapp.Model.TinTask;
import com.shevchenko.staffapp.connectivity.AuditManagerJofemarRD;
import com.shevchenko.staffapp.db.DBManager;
import com.shevchenko.staffapp.net.NetworkManager;
import com.shevchenko.staffapp.viewholder.CaptureViewHolder;

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

public class AbaTaskActivity extends Activity implements View.OnClickListener {

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_FILE = 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;

    private Uri mImageCaptureUri = null;
    private int nCurIndex = 0;
    private LinearLayout lnImages;
    private ProgressDialog mProgDlg;
    private TextView txtCustomer, txtSecond, txtMachine, txttipocaptura;
    private String[] mArrPhotos;
    private ComponentName mService;
    private int nTaskID;
    private String date;
    private String tasktype;
    private String latitude, longitude;
    private ImageView imgWaze;
    private int cnt = 0;
    private String mRutaAbastecimiento = "";
    private String mTaskbusinesskey = "";
    private String mMachineType = "";
    private ArrayList<Producto> currentProductos = new ArrayList<Producto>();
    private String strFileName = "";
    LocationLoader mLocationLoader;
    private Location mNewLocation;
    private Button btnPhoto, btnAbastec, btnCapturar, btnRecalculate, btnContadores, btnCapture_tar, btnSendForm, btnBack;
    private View captureLayout;
    private TaskInfo currentTask;
    private CaptureViewHolder captureViewHolder;
    private boolean recaudar;

    private ScrollView mScrContent;
    public ScrollView getScrollContent() { return mScrContent; }

    private boolean mIsPending = false;

    private ListView mLvDrawer;
    private DrawerLayout drawerLayout;
    private Menu mMenu;
    private TaskInfo mNewTask;
    private String mStrComment = "";
    private String mStrError = "";
    TextView txtError;
    ArrayList<String> mStatusList;
////////////2016--04-26 changes///////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abatask);

        Common.getInstance().arrAbastTinTasks.clear();
        Common.getInstance().arrDetailCounters.clear();
        Common.getInstance().isAbastec = false;
        Common.getInstance().capture = false;
        Common.getInstance().mainAbastec = false;

        nTaskID = getIntent().getIntExtra("taskid", 0);
        date = getIntent().getStringExtra("date");
        tasktype = getIntent().getStringExtra("tasktype");
        mRutaAbastecimiento = getIntent().getStringExtra("RutaAbastecimiento");
        mTaskbusinesskey = getIntent().getStringExtra("Taskbusinesskey");
        mMachineType = getIntent().getStringExtra("MachineType");
        btnPhoto = (Button) findViewById(R.id.btnTomar);
        btnPhoto.setOnClickListener(this);
        btnAbastec = (Button) findViewById(R.id.btnAbastec);
        btnAbastec.setOnClickListener(this);
        btnCapturar = (Button) findViewById(R.id.btnCapture);
        btnCapturar.setOnClickListener(this);
        btnRecalculate = (Button)findViewById(R.id.btnRecalculate);
        btnRecalculate.setVisibility(View.GONE);
        btnRecalculate.setOnClickListener(this);
        btnContadores = (Button)findViewById(R.id.btnContadores);
        btnContadores.setOnClickListener(this);
        btnContadores.setVisibility(View.GONE);
        btnCapture_tar = (Button)findViewById(R.id.btnCapture_tar);
        btnCapture_tar.setOnClickListener(this);
        btnCapture_tar.setVisibility(View.GONE);

        btnSendForm = (Button) findViewById(R.id.btnSendForm);
        btnSendForm.setOnClickListener(this);


        mScrContent = (ScrollView) findViewById(R.id.scrContent);

        txtCustomer = (TextView) findViewById(R.id.txtCustomer);
        txtSecond = (TextView) findViewById(R.id.txtSecond);
        txtMachine = (TextView) findViewById(R.id.txtMachine);
        txttipocaptura = (TextView) findViewById(R.id.txttcap);
        imgWaze = (ImageView) findViewById(R.id.waze);
        imgWaze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //String url = "waze://?q=Hawaii";
                    String url = "waze://?ll=" + latitude + "," + longitude + "&navigate=yes";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Intent intent =
                            new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                    startActivity(intent);
                }
            }
        });
        findViewById(R.id.btnSendForm).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);

        lnImages = (LinearLayout) findViewById(R.id.lnImages);

        mProgDlg = new ProgressDialog(this);
        mProgDlg.setCancelable(false);
        mProgDlg.setTitle("Posting Task!");
        mProgDlg.setMessage("Please Wait!");
        Common.getInstance().isAbastec = false;
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

        mArrPhotos = new String[]{"", "", "", "", ""};

        recaudar = false;
        setTitleAndSummary();
        captureLayout = findViewById(R.id.capture_layout);
        captureViewHolder = new CaptureViewHolder(this, captureLayout, currentTask, DBManager.getManager());
        invalidateCaptureButton();

        mIsPending = false;


        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        final ActionBar actionBar = getActionBar();
        ArrayList<MenuItemButton> arrMenus = new ArrayList<>();
        MenuItemButton menuItem = new MenuItemButton("Marcar tarea como no realizada", 0, 1);
        MenuItemButton menuItem1 = new MenuItemButton("Volver a Abastecer", 0, 1);
        arrMenus.add(menuItem);
        arrMenus.add(menuItem1);
        mLvDrawer = (ListView) findViewById(R.id.lvDrawer);
        mLvDrawer.setAdapter(new MenuListAdapter(this, arrMenus));
        mLvDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (drawerLayout.isDrawerOpen(GravityCompat.END))
                    drawerLayout.closeDrawer(GravityCompat.END);
                if(position == 0) {
                    final Dialog dlg = new Dialog(AbaTaskActivity.this);
                    dlg.setTitle("Confirmar");
                    View v = LayoutInflater.from(AbaTaskActivity.this).inflate(R.layout.dialog_confirm, null);
                    final EditText edtReason = (EditText)v.findViewById(R.id.edtReason);
                    v.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dlg.dismiss();
                        }
                    });
                    v.findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String strReason = edtReason.getText().toString();
                            if(strReason.isEmpty())
                                return;

                            setService("The user marked the task as completed.");
                            addPendingTask(strReason, "");

                            dlg.dismiss();
                        }
                    });
                    dlg.setContentView(v);
                    dlg.setCancelable(true);
                    dlg.show();
                }
                if(position == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AbaTaskActivity.this);
                    builder.setTitle("Confirmar");
                    builder.setMessage("¿Está seguro que dese reabastecer?");
                    builder.setPositiveButton("CONFIRMAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mProgDlg.setTitle("Creating task...");
                            mProgDlg.show();
                            new Thread(mCreateTaskRunnable).start();
                        }
                    });
                    builder.setNegativeButton("VOLVER", null);
                    builder.show();
                }
            }
        });

        SharedPreferences pref = getSharedPreferences(Common.PREF_KEY_TEMPSAVE, MODE_PRIVATE);
        recaudar = pref.getBoolean(Common.PREF_KEY_TEMPSAVE_RECAUDAR + nTaskID, false);
        if(recaudar)
            btnRecalculate.setBackgroundColor(getResources().getColor(R.color.clr_button_on));

        TaskInfo taskInfo;
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
            taskInfo = Common.getInstance().arrIncompleteTasks.get(i);
            if (taskInfo.getTaskID() == nTaskID) {

                currentProductos.clear();
                //ArrayList<String> lstCus = new ArrayList<String>();
                //lstCus = DBManager.getManager().getProductos_CUS(taskInfo.RutaAbastecimiento, taskInfo.MachineType, taskInfo.taskType);
                currentProductos = DBManager.getManager().getProductos_CUS(taskInfo.RutaAbastecimiento, taskInfo.MachineType, taskInfo.taskType);
//                for(int ii = 0;  ii < Common.getInstance().arrProducto.size(); ii++){
//                    for(int j = 0; j < lstCus.size(); j++){
//                        if(Common.getInstance().arrProducto.get(ii).cus.equals(lstCus.get(j))){
//                            currentProductos.add(Common.getInstance().arrProducto.get(ii));
//                            break;
//                        }
//                    }
//                }
                Collections.sort(currentProductos, new Comparator<Producto>() {
                    @Override
                    public int compare(Producto lhs, Producto rhs) {
                        return lhs.nus.compareToIgnoreCase(rhs.nus);
                    }
                });

                String strTinTask = pref.getString(Common.PREF_KEY_TEMPSAVE_ABASTEC + nTaskID, "");
                if(!strTinTask.equals("")) {

                    String[] arrTinTask = strTinTask.split(";");

                    Common.getInstance().arrAbastTinTasks.clear();
                    for (int j = 0; j < currentProductos.size(); j++) {
                        String quantity = (j < arrTinTask.length) ? arrTinTask[j] : "0";

                        TinTask tinInfo = new TinTask(Common.getInstance().getLoginUser().getUserId(), nTaskID, taskInfo.getTaskType(), taskInfo.getRutaAbastecimiento(), currentProductos.get(j).cus, currentProductos.get(j).nus, quantity);
                        Common.getInstance().arrAbastTinTasks.add(tinInfo);
                    }
                    Common.getInstance().isAbastec = true;
                    btnAbastec.setBackgroundColor(getResources().getColor(R.color.clr_button_on));
                }

                String strCounters = pref.getString(Common.PREF_KEY_TEMPSAVE_CONTADORES + nTaskID, "");
                if(!strCounters.equals("")) {
                    String[] arrCounters = strCounters.split(";");

                    Common.getInstance().arrDetailCounters.clear();

                    ArrayList<MachineCounter> currentMachine = DBManager.getManager().getMachineCounters(taskInfo.TaskBusinessKey);
                    String strData = "";
                    for (int j = 0; j < currentMachine.size(); j++) {
                        String quantity = (j < arrCounters.length) ? arrCounters[j] : "0";
                        DetailCounter info = new DetailCounter(String.valueOf(nTaskID), currentMachine.get(j).CodContador, quantity);
                        for (int z = 0; z <  Common.getInstance().arrDetailCounters.size(); z++) {
                            if(Common.getInstance().arrDetailCounters.get(z).taskid==String.valueOf(nTaskID) && Common.getInstance().arrDetailCounters.get(z).CodCounter == currentMachine.get(j).CodContador){
                                Common.getInstance().arrDetailCounters.remove(z);
                            }
                        }
                        Common.getInstance().arrDetailCounters.add(info);

                    }
                    btnContadores.setBackgroundColor(getResources().getColor(R.color.clr_button_on));
                }
                break;
            }
        }

        new Thread(mRunnable_producto).start();
    }
    //Thread in order to create the new pending task.
    private Runnable mCreateTaskRunnable = new Runnable() {
        @Override
        public void run() {
            TaskInfo task = new TaskInfo(
                    currentTask.userid,
                    0,
                    new SimpleDateFormat("dd/MM/yyyy").format(new Date()),
                    currentTask.taskType,
                    currentTask.RutaAbastecimiento,
                    currentTask.TaskBusinessKey,
                    currentTask.Customer,
                    currentTask.Adress,
                    currentTask.LocationDesc,
                    currentTask.Model,
                    currentTask.latitude,
                    currentTask.longitude,
                    currentTask.epv,
                    currentTask.MachineType,
                    "",
                    currentTask.Aux_valor1,
                    "",
                    "",
                    "",
                    "",
                    ""
            );
            if(NetworkManager.getManager().postNewTask(task)) {
                DBManager.getManager().insertInCompleteTask(task);
                Common.getInstance().arrIncompleteTasks.add(task);
                mNewTask = new TaskInfo();
                mNewTask = task;
                mCreateTaskHandler.sendEmptyMessage(0);
            } else {
                mCreateTaskHandler.sendEmptyMessage(-1);
            }

        }
    };
    private Handler mCreateTaskHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mProgDlg.dismiss();
            if(msg.what == 0) {
                Common.getInstance().isNeedRefresh = true;
                Intent intent = new Intent(AbaTaskActivity.this, AbaTaskActivity.class);
                intent.putExtra("taskid", mNewTask.getTaskID());
                intent.putExtra("date", mNewTask.getDate());
                intent.putExtra("tasktype", mNewTask.getTaskType());
                intent.putExtra("RutaAbastecimiento", mNewTask.getRutaAbastecimiento());
                intent.putExtra("Taskbusinesskey", mNewTask.getTaskBusinessKey());
                intent.putExtra("MachineType", mNewTask.getMachineType());
                startActivity(intent);

                Toast.makeText(AbaTaskActivity.this, "Nueva tarea ha sido creada", Toast.LENGTH_SHORT).show();

                finish();
            } else {
                Toast.makeText(AbaTaskActivity.this, "Failed to create!", Toast.LENGTH_LONG).show();
            }
        }
    };
    private void getLocation() {
        if (mNewLocation == null)
            return;
        Common.getInstance().latitude = String.valueOf(mNewLocation.getLatitude());
        Common.getInstance().longitude = String.valueOf(mNewLocation.getLongitude());
    }

    private Runnable mRunnable_producto = new Runnable() {

        @Override
        public void run() {
            currentProductos.clear();
            ArrayList<String> lstCus = new ArrayList<String>();
            //lstCus = dbManager.getProductos_CUS(mRutaAbastecimiento, mTaskbusinesskey, tasktype);
            //lstCus = DBManager.getManager().getProductos_CUS(mRutaAbastecimiento, mMachineType, tasktype);
            currentProductos = DBManager.getManager().getProductos_CUS(mRutaAbastecimiento, mMachineType, tasktype);
//            for (int i = 0; i < Common.getInstance().arrProducto.size(); i++) {
//                for (int j = 0; j < lstCus.size(); j++) {
//                    if (Common.getInstance().arrProducto.get(i).cus.equals(lstCus.get(j))) {
//                        currentProductos.add(Common.getInstance().arrProducto.get(i));
//                        break;
//                    }
//                }
//            }
            mHandler_task.sendEmptyMessage(1);
        }
    };
    private Handler mHandler_task = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            mProgDlg.hide();
            if (msg.what == 1) {
                ArrayList<String> arrNus = new ArrayList<String>();
                for (int i = 0; i < currentProductos.size(); i++)
                    arrNus.add(currentProductos.get(i).nus);
                ArrayAdapter<String> adapterSelect = new ArrayAdapter<String>(AbaTaskActivity.this, android.R.layout.simple_spinner_dropdown_item, arrNus);
            } else if (msg.what == 0) {
                Toast.makeText(AbaTaskActivity.this, "Load failed!", Toast.LENGTH_SHORT).show();
            } else if (msg.what == -1) {
                Toast.makeText(AbaTaskActivity.this, "Load failed due to network problem! Please check your network status", Toast.LENGTH_SHORT).show();
            }
            //setTaskNumber();
        }
    };
    //setting the button for every functions.
    private void setTitleAndSummary() {
        TaskInfo taskInfo;
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
            taskInfo = Common.getInstance().arrIncompleteTasks.get(i);
            if (taskInfo.getTaskID() == nTaskID) {
                currentTask = taskInfo;
                txtCustomer.setText(taskInfo.getCustomer());
                txtSecond.setText(taskInfo.getAdress() + ", " + taskInfo.getLocationDesc());
                txtMachine.setText(taskInfo.getTaskBusinessKey() + ", " + taskInfo.getModel() + ", " + taskInfo.getMachineType());
                String actiondate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

                //txtDate.setText("Fecha: " + actiondate);
                                if (taskInfo.getepv().equals("ARC"))
                    txttipocaptura.setText("Tipo Captura " + taskInfo.getAux_valor1());

                if (taskInfo.getepv().equals("C")) {
                    txttipocaptura.setText("Tipo Captura " + taskInfo.getAux_valor1());
                    btnAbastec.setVisibility(View.GONE);
                }


                latitude = taskInfo.getLatitude();
                longitude = taskInfo.getLongitude();
                if(!taskInfo.getAux_valor5().equals("")) {
                    if (Integer.parseInt(taskInfo.getAux_valor5()) == 1)
                        btnRecalculate.setVisibility(View.VISIBLE);
                    else
                        btnRecalculate.setVisibility(View.GONE);
                }else{
                    btnRecalculate.setVisibility(View.GONE);
                }
                if(!taskInfo.getAux_valor4().equals("")) {
                    if (Integer.parseInt(taskInfo.getAux_valor4()) == 1)
                        btnContadores.setVisibility(View.VISIBLE);
                    else
                        btnContadores.setVisibility(View.GONE);
                }else{
                    btnContadores.setVisibility(View.GONE);
                }
                if(!taskInfo.getAux_valor3().equals("")) {
                    if (Integer.parseInt(taskInfo.getAux_valor3()) == 1)
                        btnCapturar.setVisibility(View.VISIBLE);
                    else
                        btnCapturar.setVisibility(View.GONE);
                }else{
                    btnCapturar.setVisibility(View.GONE);
                }
                if(!taskInfo.getAux_valor2().equals("")) {
                    if (!taskInfo.getAux_valor2().equals("") && Integer.parseInt(taskInfo.getAux_valor2()) == 1)
                        btnPhoto.setVisibility(View.VISIBLE);
                    else
                        btnPhoto.setVisibility(View.GONE);
                }else{
                    btnPhoto.setVisibility(View.GONE);
                }
                if(!taskInfo.getAux_valor6().equals("")){
                    if(!taskInfo.getAux_valor6().equals("") && Integer.parseInt(taskInfo.getAux_valor6()) == 1){
                        btnCapture_tar.setVisibility(View.VISIBLE);
                    }
                    else
                        btnCapture_tar.setVisibility(View.GONE);
                }else{
                    btnCapture_tar.setVisibility(View.GONE);
                }
                break;
            }
        }
    }
    private void DialogSelectOption() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirme Recaudación")
                .setMessage("Favor confirme que realizará recaudación")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        btnRecalculate.setBackgroundColor(getResources().getColor(R.color.clr_button_on));
                        recaudar = true;
                        saveTempRecaudar();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        recaudar = false;
                        saveTempRecaudar();
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void saveTempRecaudar() {
        SharedPreferences.Editor editor = getSharedPreferences(Common.PREF_KEY_TEMPSAVE, MODE_PRIVATE).edit();
        editor.putBoolean(Common.PREF_KEY_TEMPSAVE_RECAUDAR + nTaskID, recaudar);
        editor.commit();
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
    //after the user takes the photo, the photo with current time name is saved in android sdcard path.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureViewHolder.BT_REQUEST_CODE) {
            captureViewHolder.start();
            return;
        }
        if (resultCode != RESULT_OK) {
            return;
        }

        final String strFilePath =  Environment.getExternalStorageDirectory() + "/staffapp/"+ strFileName;
        String strScaledFilePath =  Environment.getExternalStorageDirectory() + "/staffapp/"+ "temp.jpg";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(strFilePath, options);
        try {
            FileOutputStream fos = new FileOutputStream(strScaledFilePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            File oldFile = new File(strFilePath);
            oldFile.delete();
            new File(strScaledFilePath).renameTo(oldFile);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("AbaTask", "Failed to scale image!");
        }
        File imgFile = new File(strFilePath);
        if (nCurIndex == 0)
            lnImages.setVisibility(View.VISIBLE);
        if (imgFile.exists()) {
            ImageView imgPhoto = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.space_50), (int) getResources().getDimension(R.dimen.space_50));
            params.leftMargin = (int) getResources().getDimension(R.dimen.space_10);
            params.gravity = Gravity.CENTER_VERTICAL;

            Bitmap myBitmap = loadLargeBitmapFromFile(imgFile.getAbsolutePath(), this);
            imgPhoto.setImageBitmap(myBitmap);
            imgPhoto.setLayoutParams(params);
            imgPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PhotoDialog dlg = new PhotoDialog(AbaTaskActivity.this);
                    dlg.setImage(strFilePath);
                    dlg.show();
                }
            });
            lnImages.addView(imgPhoto);
            mArrPhotos[nCurIndex] = strFilePath;
            nCurIndex++;
            btnPhoto.setBackgroundColor(getResources().getColor(R.color.clr_button_on));

        }
    }
    //when the user press the guardar button, this function is called. the task is saved in android sqlite db.
    private void addPendingTask(String strComment, String strError) {

        TaskInfo taskInfo;
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
            taskInfo = Common.getInstance().arrIncompleteTasks.get(i);
            if (taskInfo.getTaskID() == nTaskID) {
                String actiondate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String aux5_value = "0";
                if(recaudar == true)
                    aux5_value = "1";
                else
                    aux5_value = "0";

                String aux4_value = "0";
                if(Common.getInstance().arrDetailCounters.size() != 0)
                    aux4_value = "1";
                else
                    aux4_value = "0";

                int sumQuantity = 0;
                for(int j = 0; j < Common.getInstance().arrAbastTinTasks.size(); j++) {
                    if(!Common.getInstance().arrAbastTinTasks.get(j).quantity.equals(""))
                        sumQuantity += Integer.parseInt(Common.getInstance().arrAbastTinTasks.get(j).quantity);
                }
                String comment_nocap = "";
                if(strComment.equals(""))
                    comment_nocap = "0";
                else
                    comment_nocap = "1";

                PendingTasks task = new PendingTasks(Common.getInstance().getLoginUser().getUserId(), nTaskID, taskInfo.getDate(), taskInfo.getTaskType(), taskInfo.getRutaAbastecimiento(), taskInfo.getTaskBusinessKey(), taskInfo.getCustomer(), taskInfo.getAdress(), taskInfo.getLocationDesc(), taskInfo.getModel(), taskInfo.getLatitude(), taskInfo.getLongitude(), taskInfo.getepv(), Common.getInstance().latitude, Common.getInstance().longitude, actiondate, mArrPhotos[0], mArrPhotos[1], mArrPhotos[2], mArrPhotos[3], mArrPhotos[4], taskInfo.getMachineType(), Common.getInstance().signaturePath, "", "", taskInfo.getAux_valor1(), taskInfo.getAux_valor2(), taskInfo.getAux_valor3(), aux4_value, aux5_value, strComment.isEmpty() ? 1 : 0, strComment, taskInfo.getAux_valor6(), sumQuantity, strError);
                DBManager.getManager().insertPendingTask(task);
                Common.getInstance().arrPendingTasks.add(task);

                CompleteTask comtask = new CompleteTask(Common.getInstance().getLoginUser().getUserId(), nTaskID, taskInfo.getDate(), taskInfo.getTaskType(), taskInfo.getRutaAbastecimiento(), taskInfo.getTaskBusinessKey(), taskInfo.getCustomer(), taskInfo.getAdress(), taskInfo.getLocationDesc(), taskInfo.getModel(), taskInfo.getLatitude(), taskInfo.getLongitude(), taskInfo.getepv(), Common.getInstance().latitude, Common.getInstance().longitude, actiondate, mArrPhotos[0], mArrPhotos[1], mArrPhotos[2], mArrPhotos[3], mArrPhotos[4], taskInfo.getMachineType(), Common.getInstance().signaturePath, "", "", taskInfo.getAux_valor1(), taskInfo.getAux_valor2(), taskInfo.getAux_valor3(), aux4_value, aux5_value, strComment.isEmpty() ? 1 : 0, strComment, taskInfo.getAux_valor6(), sumQuantity, strError);
                DBManager.getManager().insertCompleteTask(comtask);
                Common.getInstance().arrCompleteTasks.add(comtask);

                for (int j = 0; j < Common.getInstance().arrAbastTinTasks.size(); j++) {
                    //EditText edtContent = (EditText)findViewById(DYNAMIC_EDIT_ID + j);
                    TinTask tinInfo = new TinTask();
                    tinInfo = Common.getInstance().arrAbastTinTasks.get(j);///2016
                    DBManager.getManager().insertPendingTinTask(tinInfo);
                    Common.getInstance().arrTinTasks.add(tinInfo);

                    CompltedTinTask comtinInfo = new CompltedTinTask(Common.getInstance().arrAbastTinTasks.get(j).userid, Common.getInstance().arrAbastTinTasks.get(j).taskid, Common.getInstance().arrAbastTinTasks.get(j).tasktype, Common.getInstance().arrAbastTinTasks.get(j).RutaAbastecimiento, Common.getInstance().arrAbastTinTasks.get(j).cus, Common.getInstance().arrAbastTinTasks.get(j).nus, Common.getInstance().arrAbastTinTasks.get(j).quantity);
                    DBManager.getManager().insertCompleteTinTask(comtinInfo);
                    Common.getInstance().arrCompleteTinTasks.add(comtinInfo);
                }
                for(int k = 0; k < Common.getInstance().arrDetailCounters.size(); k++){
                    //DBManager.getManager().insertDetailCounter(Common.getInstance().arrDetailCounters.get(k));

                    CompleteDetailCounter detail = new CompleteDetailCounter(Common.getInstance().arrDetailCounters.get(k).taskid, Common.getInstance().arrDetailCounters.get(k).CodCounter, Common.getInstance().arrDetailCounters.get(k).quantity);
                    DBManager.getManager().insertCompleteDetailCounter(detail);
                }
                break;
            }
        }
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
            if (Common.getInstance().arrIncompleteTasks.get(i).getTaskID() == nTaskID) {
                DBManager.getManager().deleteInCompleteTask(Common.getInstance().getLoginUser().getUserId(), nTaskID);
                Common.getInstance().arrIncompleteTasks.remove(i);
            }
        }

        SharedPreferences.Editor editor = getSharedPreferences(Common.PREF_KEY_TEMPSAVE, MODE_PRIVATE).edit();
        editor.remove(Common.PREF_KEY_TEMPSAVE_ABASTEC + nTaskID);
        editor.remove(Common.PREF_KEY_TEMPSAVE_CONTADORES + nTaskID);
        editor.remove(Common.PREF_KEY_TEMPSAVE_RECAUDAR + nTaskID);
        editor.commit();

        Intent intentMain = new Intent(AbaTaskActivity.this, MainActivity.class);
        Common.getInstance().arrAbastTinTasks.clear();
        Common.getInstance().arrDetailCounters.clear();
        intentMain.putExtra("position", 0);
        intentMain.putExtra("abastec", true);
        Common.getInstance().mainAbastec = true;
        startActivity(intentMain);
        finish();
    }

    private long mLastClickTime = 0;
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        Intent intent;
        switch (v.getId()) {
            case R.id.btnTomar:
                setService("The user takes some pictures.");
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mImageCaptureUri = createSaveCropFile();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);
                break;
            case R.id.btnSendForm:
                if(!mIsPending) {
                    ArrayList<LogFile> logs = DBManager.getManager().getLogs(nTaskID);
                    //if ((Common.getInstance().arrAbastTinTasks.size() == 0) ||
                    /*if(Common.getInstance().isAbastec == false ||
                            (btnPhoto.getVisibility() == View.VISIBLE && (mArrPhotos[0] == "")) ||
                            (btnCapturar.getVisibility() == View.VISIBLE && logs.isEmpty())) {*/
                    if(Common.getInstance().isAbastec == false ||
                            (btnPhoto.getVisibility() == View.VISIBLE && (mArrPhotos[0] == ""))) {
                        Toast.makeText(AbaTaskActivity.this, "Please input the full informations.", Toast.LENGTH_SHORT).show();
                    } else {
                        mIsPending = true;
                        setService("The user clicks the Send Form Button");
                        //addPendingTask("");
                        if(btnCapturar.getVisibility() == View.VISIBLE) {
                            if(logs.isEmpty() == true) {
                                final Dialog dlg_comment = new Dialog(AbaTaskActivity.this);
                                dlg_comment.setTitle("MAQUINA NO CAPTURADA");
                                View v_comment = LayoutInflater.from(AbaTaskActivity.this).inflate(R.layout.dialog_comment, null);
                                LinearLayout lnError = (LinearLayout)v_comment.findViewById(R.id.lnError);
                                final Spinner spError = (Spinner)v_comment.findViewById(R.id.spError);
                                lnError.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        spError.performClick();
                                    }
                                });
                                txtError = (TextView)v_comment.findViewById(R.id.txtError);
                                spError.setOnItemSelectedListener(mSpListener);
                                mStatusList = new ArrayList<String>();
                                for(int i = 0; i < Common.getInstance().arrCommentErrors.size(); i++){
                                    mStatusList.add(Common.getInstance().arrCommentErrors.get(i).error);
                                }

                                ArrayAdapter<String> adapterEst = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mStatusList);
                                adapterEst.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spError.setAdapter(adapterEst);

                                v_comment.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dlg_comment.dismiss();
                                    }
                                });
                                v_comment.findViewById(R.id.btnContinue).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mStrError = txtError.getText().toString();
                                        if (mStrError.isEmpty())
                                            return;
                                        dlg_comment.dismiss();
                                        mStrComment = "";
                                        completeTask();
                                    }
                                });
                                dlg_comment.setContentView(v_comment);
                                dlg_comment.setCancelable(false);
                                dlg_comment.show();
                            }else{
                                mStrError = "";
                                mStrComment = "";
                                AbastecTaskDlg dlg = new AbastecTaskDlg(this);
                                dlg.setTitle("Confirmar abastecimiento");
                                int quantity = 0;
                                for (int i = 0; i < Common.getInstance().arrAbastTinTasks.size(); i++) {
                                    if (!Common.getInstance().arrAbastTinTasks.get(i).quantity.equals(""))
                                        quantity += Integer.parseInt(Common.getInstance().arrAbastTinTasks.get(i).quantity);
                                }
                                dlg.setQuantity(quantity);
                                dlg.setRecaudado(recaudar);
                                if (Common.getInstance().arrDetailCounters.size() != 0) {
                                    dlg.setContadores(1);
                                } else
                                    dlg.setContadores(0);
                                if (btnCapturar.getVisibility() == View.VISIBLE) {
                                    if (logs.isEmpty() == false)
                                        dlg.setCaptura(1);
                                    else
                                        dlg.setCaptura(0);//////////////
                                } else
                                    dlg.setCaptura(0);

                                if (btnCapture_tar.getVisibility() == View.VISIBLE) {
                                    btnCapture_tar.buildDrawingCache();
                                    Bitmap bit1 = btnCapture_tar.getDrawingCache();
                                    int color1 = bit1.getPixel(1, 1);
                                    if (color1 == R.color.clr_green)
                                        dlg.setCapTar(1);
                                    else
                                        dlg.setCapTar(0);
                                } else
                                    dlg.setCapTar(0);

                                dlg.setCancelable(false);
                                dlg.setListener(mCancelListener1);
                                dlg.show();
                            }
                        }
                        else {
                            mStrError = "";
                            mStrComment = "";
                            AbastecTaskDlg dlg = new AbastecTaskDlg(this);
                            dlg.setTitle("Confirmar abastecimiento");
                            int quantity = 0;
                            for (int i = 0; i < Common.getInstance().arrAbastTinTasks.size(); i++) {
                                if (!Common.getInstance().arrAbastTinTasks.get(i).quantity.equals(""))
                                    quantity += Integer.parseInt(Common.getInstance().arrAbastTinTasks.get(i).quantity);
                            }
                            dlg.setQuantity(quantity);
                            dlg.setRecaudado(recaudar);
                            if (Common.getInstance().arrDetailCounters.size() != 0) {
                                dlg.setContadores(1);
                            } else
                                dlg.setContadores(0);
                            if (btnCapturar.getVisibility() == View.VISIBLE) {
                                if (logs.isEmpty() == false)
                                    dlg.setCaptura(1);
                                else
                                    dlg.setCaptura(0);//////////////
                            } else
                                dlg.setCaptura(0);

                            if (btnCapture_tar.getVisibility() == View.VISIBLE) {
                                btnCapture_tar.buildDrawingCache();
                                Bitmap bit1 = btnCapture_tar.getDrawingCache();
                                int color1 = bit1.getPixel(1, 1);
                                if (color1 == R.color.clr_green)
                                    dlg.setCapTar(1);
                                else
                                    dlg.setCapTar(0);
                            } else
                                dlg.setCapTar(0);

                            dlg.setCancelable(false);
                            dlg.setListener(mCancelListener1);
                            dlg.show();
                        }
                    }
                }
                break;
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnCapture:
                Common.getInstance().capture = false;
                checkPermissions();
                break;
            case R.id.btnCapture_tar:
                checkPermissions();
                break;
            case R.id.btnAbastec:
                intent = new Intent(AbaTaskActivity.this, AbastecTinTaskActivity.class);
                intent.putExtra("taskid", nTaskID);
                startActivity(intent);
                break;
            case R.id.btnRecalculate:
                DialogSelectOption();
                break;
            case R.id.btnContadores:
                intent = new Intent(AbaTaskActivity.this, AbastecContadoresActivity.class);
                intent.putExtra("taskid", nTaskID);
                startActivity(intent);
                break;
        }
    }
    private AdapterView.OnItemSelectedListener mSpListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {

            txtError.setText(mStatusList.get(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
    private void completeTask(){

        ArrayList<LogFile> logs = DBManager.getManager().getLogs(nTaskID);
        AbastecTaskDlg dlg = new AbastecTaskDlg(this);
        dlg.setTitle("Confirmar abastecimiento");
        int quantity = 0;
        for(int i = 0; i < Common.getInstance().arrAbastTinTasks.size(); i++){
            if(!Common.getInstance().arrAbastTinTasks.get(i).quantity.equals(""))
                quantity += Integer.parseInt(Common.getInstance().arrAbastTinTasks.get(i).quantity);
        }
        dlg.setQuantity(quantity);
        dlg.setRecaudado(recaudar);
        if(Common.getInstance().arrDetailCounters.size() != 0){
            dlg.setContadores(1);
        }else
            dlg.setContadores(0);
        if(btnCapturar.getVisibility() == View.VISIBLE) {
            if(logs.isEmpty() == false)
                dlg.setCaptura(1);
            else
                dlg.setCaptura(0);//////////////
        }else
            dlg.setCaptura(0);

        if(btnCapture_tar.getVisibility() == View.VISIBLE) {
            btnCapture_tar.buildDrawingCache();
            Bitmap bit1 = btnCapture_tar.getDrawingCache();
            int color1 = bit1.getPixel(1, 1);
            if (color1 == R.color.clr_green)
                dlg.setCapTar(1);
            else
                dlg.setCapTar(0);
        }else
            dlg.setCapTar(0);

        dlg.setCancelable(false);
        dlg.setListener(mCancelListener1);
        dlg.show();
    }
    private AbastecTaskDlg.OnCancelOrderListener mCancelListener1 = new AbastecTaskDlg.OnCancelOrderListener() {
        @Override
        public void OnCancel(String strReason, int iType) {
            if(iType == 1) {
                addPendingTask(mStrComment, mStrError);
            } else {
                //onBackPressed();
            }
        }
    };

    /*
    boton capturar

     */
    private void setCaptureMode(boolean captureMode) {
        if (captureMode) {
            captureViewHolder.start();
        }
        captureLayout.setVisibility(captureMode ? View.VISIBLE : View.GONE);
        btnCapturar.setVisibility(captureMode ? View.GONE : View.VISIBLE);
        btnAbastec.setVisibility(captureMode ? View.GONE : View.VISIBLE);
        btnRecalculate.setVisibility(captureMode ? View.GONE : View.VISIBLE);
        //btnCapture_tar.setVisibility(captureMode ? View.GONE : View.VISIBLE);
        //btnContadores.setVisibility(captureMode ? View.GONE : View.VISIBLE);
        //btnPhoto.setVisibility(captureMode ? View.GONE : View.VISIBLE);
        invalidateCaptureButton();
        //invalidateCaptureTarButton();

        btnSendForm.setEnabled(!captureMode);

    }



    /*

    fin holder captura
     */


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_FILE);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                setCaptureMode(true);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_FILE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setCaptureMode(true);
                }
            }
        }
    }

    private void invalidateCaptureButton() {
        final ArrayList<LogFile> logs = DBManager.getManager().getLogs(nTaskID);
        boolean enabled = logs.isEmpty();
        for (LogFile file : logs) {
            enabled &= AuditManagerJofemarRD.JOFEMAR_RD.equals(file.getFileType());
        }
        btnCapturar.setBackgroundColor(ContextCompat.getColor(this, enabled ? R.color.clr_button_off : R.color.clr_green));
        btnCapturar.setEnabled(enabled);
    }

    private void invalidateCaptureTarButton() {
        final ArrayList<LogFile> logs = DBManager.getManager().getLogs(nTaskID);
        boolean enabled = logs.isEmpty();
        for (LogFile file : logs) {
            enabled &= !AuditManagerJofemarRD.JOFEMAR_RD.equals(file.getFileType());
        }
        btnCapture_tar.setBackgroundColor(ContextCompat.getColor(this, enabled ? R.color.clr_button_off : R.color.clr_green));
        btnCapture_tar.setEnabled(enabled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        if (Common.getInstance().arrAbastTinTasks.size() != 0) {
            btnAbastec.setBackgroundColor(getResources().getColor(R.color.clr_button_on));
        }*/
        if(Common.getInstance().isAbastec == true)
            btnAbastec.setBackgroundColor(getResources().getColor(R.color.clr_button_on));
        if(Common.getInstance().arrDetailCounters.size() != 0){
            btnContadores.setBackgroundColor(getResources().getColor(R.color.clr_button_on));
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END);
        else if (captureLayout.getVisibility() == View.VISIBLE) {
            setCaptureMode(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //item_upload =  menu.add(0, 0, Menu.NONE, "").setIcon(R.drawable.upload);
        //item_upload.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        //item_loading =  menu.add(0, Menu.FIRST + 1, Menu.NONE, "").setIcon(R.drawable.loading_icon);
        //item_loading.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.imgMenu){
            drawerLayout.setEnabled(true);
            if (drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.closeDrawer(GravityCompat.END);
            else
                drawerLayout.openDrawer(GravityCompat.END);
        }
        return super.onOptionsItemSelected(item);
    }

    private void DialogShow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Please select the value.")
                .setMessage("Please select a product and quantity before add a new product.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void DialogExists() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Please select the value.")
                .setMessage("Please select the product again because you already added the product.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
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

        GpsInfo info = new GpsInfo(AbaTaskActivity.this);
        Intent service = new Intent(AbaTaskActivity.this, LogService.class);
        service.putExtra("userid", Common.getInstance().getLoginUser().getUserId());
        service.putExtra("taskid", String.valueOf(nTaskID));
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        service.putExtra("datetime", time);
        service.putExtra("description", description);
        service.putExtra("latitude", Common.getInstance().latitude);
        service.putExtra("longitude", Common.getInstance().longitude);
        mService = startService(service);
    }

    private Bitmap loadLargeBitmapFromFile(String strPath, Context context) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(strPath, opt);

        int REQUIRED_SIZE = 480;
        int width_tmp = opt.outWidth, height_tmp = opt.outHeight;
        int scale = 1;
        while (true) {
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

    private Uri createSaveCropFile() {
        Uri uri;
        strFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + String.valueOf(nCurIndex) + ".jpg";
        File file = new File(Environment.getExternalStorageDirectory() + "/staffapp");
        if ( !file.exists() )
        {
            file.mkdirs();
        }
        uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/staffapp", strFileName));
        return uri;
    }


    private File getImageFile(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        if (uri == null) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        Cursor mCursor = getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
        if (mCursor == null || mCursor.getCount() < 1) {
            return null; // no cursor or no record
        }
        int column_index = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        mCursor.moveToFirst();

        String path = mCursor.getString(column_index);

        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }

        return new File(path);
    }


    public boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    private boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
