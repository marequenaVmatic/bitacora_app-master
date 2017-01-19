package com.shevchenko.staffapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.Date;
import java.util.regex.Pattern;

public class TinTaskActivity extends Activity implements View.OnClickListener {

    private LinearLayout lnContainer;
    private TextView txtPicture;
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private Uri mImageCaptureUri = null;
    private int nCurIndex = 0;
    private LinearLayout lnImages;
    private ProgressDialog mProgDlg;
    private TextView txtRusta;
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
    private ArrayList<Producto> currentProductos = new ArrayList<Producto>();
    private ArrayList<Integer> mTotalQuantity = new ArrayList<Integer>();
    private String strFileName = "";
    LocationLoader mLocationLoader;
    private Location mNewLocation;
    private EditText txtNumero, txtGlosa;
    private RelativeLayout RnButtons;
    private Boolean isEnter = false;
    //private final int DYNAMIC_EDIT_ID = 0x8000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tintask);
        Common.getInstance().signaturePath="";
        nTaskID = getIntent().getIntExtra("taskid", 0);
        date = getIntent().getStringExtra("date");
        tasktype = getIntent().getStringExtra("tasktype");
        mRutaAbastecimiento = getIntent().getStringExtra("RutaAbastecimiento");
        mTaskbusinesskey = getIntent().getStringExtra("Taskbusinesskey");
        txtPicture = (TextView) findViewById(R.id.txtPic);
        txtPicture.setOnClickListener(this);
        txtRusta = (TextView) findViewById(R.id.txtRutaAbast);
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
        findViewById(R.id.btn_signature).setOnClickListener(this);
        lnImages = (LinearLayout) findViewById(R.id.lnImages);
        lnContainer = (LinearLayout) findViewById(R.id.lnContainer);
        lnContainer.requestFocus();
        txtNumero = (EditText) findViewById(R.id.txtNumeroGuia);
        txtGlosa = (EditText) findViewById(R.id.txtGlosa);
        txtGlosa.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if((i == KeyEvent.KEYCODE_ENTER) && (keyEvent.getAction() == KeyEvent.ACTION_DOWN))
                    RnButtons.setVisibility(View.VISIBLE);
                return false;
            }
        });
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
        mArrPhotos = new String[]{"", "", "", "", ""};
        setTitleAndSummary();
        new Thread(mRunnable_producto).start();

    }
    private void getLocation() {
        if (mNewLocation == null)
            return;
        Common.getInstance().latitude = String.valueOf(mNewLocation.getLatitude());
        Common.getInstance().longitude = String.valueOf(mNewLocation.getLongitude());
    }
    private Runnable mRunnable_producto = new Runnable() {

        @Override
        public void run() {
            //int nRet = NetworkManager.getManager().loadProducto(Common.getInstance().arrProducto, mRutaAbastecimiento, mTaskbusinesskey, tasktype);
            currentProductos.clear();
            ArrayList<String> lstCus = new ArrayList<String>();
            //lstCus = DBManager.getManager().getProductos_CUS(mRutaAbastecimiento, mTaskbusinesskey, tasktype);
            currentProductos = DBManager.getManager().getProductos_CUS(mRutaAbastecimiento, mTaskbusinesskey, tasktype);
//            for(int i = 0;  i < Common.getInstance().arrProducto.size(); i++){
//                for(int j = 0; j < lstCus.size(); j++){
//                    if(Common.getInstance().arrProducto.get(i).cus.equals(lstCus.get(j))){
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
                mTotalQuantity.clear();
                for (int i = 0; i < currentProductos.size(); i++) {
                    LinearLayout lnChild = new LinearLayout(TinTaskActivity.this);
                    final int a = i;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    params.leftMargin = (int) getResources().getDimension(R.dimen.space_10);
                    params.rightMargin = (int) getResources().getDimension(R.dimen.space_10);
                    params.topMargin = (int) getResources().getDimension(R.dimen.space_5);
                    lnChild.setLayoutParams(params);
                    lnChild.setOrientation(LinearLayout.HORIZONTAL);
                    lnContainer.addView(lnChild, i);

                    TextView txtContent = new TextView(TinTaskActivity.this);
                    LinearLayout.LayoutParams param_text = new LinearLayout.LayoutParams(0, (int) getResources().getDimension(R.dimen.space_40));
                    param_text.weight = 65;
                    param_text.gravity = Gravity.CENTER;
                    txtContent.setText(currentProductos.get(i).cus + "-" + currentProductos.get(i).nus + ":");
                    txtContent.setLayoutParams(param_text);
                    //txtContent.setTextSize((float) getResources().getDimension(R.dimen.space_15));
                    txtContent.setTextColor(getResources().getColor(R.color.clr_graqy));
                    lnChild.addView(txtContent);

                    final EditText edtContent = new EditText(TinTaskActivity.this);
                    LinearLayout.LayoutParams param_edt = new LinearLayout.LayoutParams(0, (int) getResources().getDimension(R.dimen.space_35));
                    param_edt.weight = 20;
                    param_edt.gravity = Gravity.CENTER;
                    param_edt.leftMargin = (int) getResources().getDimension(R.dimen.space_3);
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
                    edtContent.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                             if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                                if (!edtContent.getText().toString().equals("")) {
                                    TextView txtSum = (TextView) findViewById(a + 1000);
                                    //int previousSum = Integer.parseInt(txtSum.getText().toString());
                                    int previousSum = mTotalQuantity.get(a);
                                    //EditText edtNext = (EditText)findViewById(a + 1);
                                    //edtNext.requestFocus();
                                    int value = 0;
                                    value = previousSum + Integer.valueOf(edtContent.getText().toString());
                                    mTotalQuantity.set(a, value);
                                    String content = String.format("%," + String.valueOf(value).length() +"d", value);
                                    txtSum.setText(content);
                                    edtContent.setText("");
                                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    in.hideSoftInputFromWindow(edtContent.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                    isEnter = true;
                                    return true;
                                }
                            } else if ((keyCode == KeyEvent.KEYCODE_BACK) &&(event.getAction() == KeyEvent.ACTION_DOWN)){
                                RnButtons.setVisibility(View.VISIBLE);
                            }
                            return false;
                        }

                    });
                    edtContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            /*if(isEnter == true){
                                RnButtons.setVisibility(View.VISIBLE);
                                isEnter = false;
                            }else{
                                if(hasFocus){
                                    RnButtons.setVisibility(View.GONE);
                                }else {
                                    RnButtons.setVisibility(View.VISIBLE);
                                }
                            }*/
                            if(hasFocus == true){
                                if(isEnter == true){
                                    RnButtons.setVisibility(View.VISIBLE);
                                    isEnter = false;
                                } else
                                    RnButtons.setVisibility(View.VISIBLE);
                            } else{
                                if(isEnter == true){
                                    RnButtons.setVisibility(View.VISIBLE);
                                } else
                                    RnButtons.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    lnChild.addView(edtContent);

                    TextView txtSum = new TextView(TinTaskActivity.this);
                    LinearLayout.LayoutParams param_sum = new LinearLayout.LayoutParams(0, (int)getResources().getDimension(R.dimen.space_30));
                    param_sum.weight = 15;
                    param_sum.gravity = Gravity.CENTER;
                    param_sum.leftMargin = (int)getResources().getDimension(R.dimen.space_5);
                    txtSum.setLayoutParams(param_sum);
                    txtSum.setSingleLine(true);
                    txtSum.setTextColor(getResources().getColor(R.color.clr_graqy));
                    txtSum.setId(1000 + i);
                    txtSum.setText("0");
                    mTotalQuantity.add(i, 0);
                    lnChild.addView(txtSum);
                }
            } else if (msg.what == 0) {
                Toast.makeText(TinTaskActivity.this, "Load failed!", Toast.LENGTH_SHORT).show();
            } else if (msg.what == -1) {
                Toast.makeText(TinTaskActivity.this, "Load failed due to network problem! Please check your network status", Toast.LENGTH_SHORT).show();
            }
            //setTaskNumber();
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            RnButtons.setVisibility(View.VISIBLE);
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
            RnButtons.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(RnButtons.getVisibility() == View.GONE)
            RnButtons.setVisibility(View.VISIBLE);
    }

    private void setTitleAndSummary() {
        TaskInfo taskInfo;
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
            taskInfo = Common.getInstance().arrIncompleteTasks.get(i);
            if (taskInfo.getTaskID() == nTaskID) {
                txtRusta.setText(taskInfo.getRutaAbastecimiento());
                String actiondate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                latitude = taskInfo.getLatitude();
                longitude = taskInfo.getLongitude();
                break;
            }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        String strFilePath =  Environment.getExternalStorageDirectory() + "/staffapp/"+ strFileName;
        File imgFile = new File(strFilePath);
        if (nCurIndex == 0)
            lnImages.setVisibility(View.VISIBLE);
        if (imgFile.exists()) {
            ImageView imgPhoto = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.space_80), (int) getResources().getDimension(R.dimen.space_80));
            params.leftMargin = (int) getResources().getDimension(R.dimen.space_10);
            params.gravity = Gravity.CENTER_VERTICAL;

            Bitmap myBitmap = loadLargeBitmapFromFile(imgFile.getAbsolutePath(), this);
            imgPhoto.setImageBitmap(myBitmap);
            imgPhoto.setLayoutParams(params);
            lnImages.addView(imgPhoto);
            mArrPhotos[nCurIndex] = strFilePath;
            nCurIndex++;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!Common.getInstance().signaturePath.equals(""))
            findViewById(R.id.btn_signature).setBackgroundColor(Color.GREEN);
    }

    private void addPendingTask() {

        TaskInfo taskInfo;
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++) {
            taskInfo = Common.getInstance().arrIncompleteTasks.get(i);
            if (taskInfo.getTaskID() == nTaskID) {
                String actiondate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                PendingTasks task = new PendingTasks(Common.getInstance().getLoginUser().getUserId(), nTaskID, taskInfo.getDate(), taskInfo.getTaskType(), taskInfo.getRutaAbastecimiento(), taskInfo.getTaskBusinessKey(), taskInfo.getCustomer(), taskInfo.getAdress(), taskInfo.getLocationDesc(), taskInfo.getModel(), taskInfo.getLatitude(), taskInfo.getLongitude(), taskInfo.getepv(), Common.getInstance().latitude, Common.getInstance().longitude, actiondate, mArrPhotos[0], mArrPhotos[1], mArrPhotos[2], mArrPhotos[3], mArrPhotos[4], taskInfo.getMachineType(), Common.getInstance().signaturePath, txtNumero.getText().toString(), txtGlosa.getText().toString(), taskInfo.getAux_valor1(), taskInfo.getAux_valor2(), taskInfo.getAux_valor3(), taskInfo.getAux_valor4(), taskInfo.getAux_valor5(), 1, "", taskInfo.getAux_valor6(), 0, "0");
                CompleteTask comtask = new CompleteTask(Common.getInstance().getLoginUser().getUserId(), nTaskID, taskInfo.getDate(), taskInfo.getTaskType(), taskInfo.getRutaAbastecimiento(), taskInfo.getTaskBusinessKey(), taskInfo.getCustomer(), taskInfo.getAdress(), taskInfo.getLocationDesc(), taskInfo.getModel(), taskInfo.getLatitude(), taskInfo.getLongitude(), taskInfo.getepv(), Common.getInstance().latitude, Common.getInstance().longitude, actiondate, mArrPhotos[0], mArrPhotos[1], mArrPhotos[2], mArrPhotos[3], mArrPhotos[4], taskInfo.getMachineType(), Common.getInstance().signaturePath, txtNumero.getText().toString(), txtGlosa.getText().toString(), taskInfo.getAux_valor1(), taskInfo.getAux_valor2(), taskInfo.getAux_valor3(), taskInfo.getAux_valor4(), taskInfo.getAux_valor5(), 1, "", taskInfo.getAux_valor6(), 0, "0");
                DBManager.getManager().insertPendingTask(task);
                Common.getInstance().arrPendingTasks.add(task);
                DBManager.getManager().insertCompleteTask(comtask);
                Common.getInstance().arrCompleteTasks.add(comtask);

                for (int j = 0; j < currentProductos.size(); j++) {
                    //EditText edtContent = (EditText)findViewById(DYNAMIC_EDIT_ID + j);
                    EditText edtContent = (EditText) findViewById(j + 1);
                    TextView txtSum = (TextView) findViewById(1000 + j + 1);
                    //String quantity = edtContent.getText().toString();
                    //String quantity = txtSum.getText().toString();
                    String quantity = String.valueOf(mTotalQuantity.get(j));
                    TinTask tinInfo = new TinTask(Common.getInstance().getLoginUser().getUserId(), nTaskID, taskInfo.getTaskType(), taskInfo.getRutaAbastecimiento(), currentProductos.get(j).cus, currentProductos.get(j).nus, quantity);
                    //DBManager.getManager().insertPendingTinTask(tinInfo);
                    //Common.getInstance().arrTinTasks.add(tinInfo);

                    CompltedTinTask comtinInfo = new CompltedTinTask(Common.getInstance().getLoginUser().getUserId(), nTaskID, taskInfo.getTaskType(), taskInfo.getRutaAbastecimiento(), currentProductos.get(j).cus, currentProductos.get(j).nus, quantity);

                    DBManager.getManager().insertCompleteTinTask(comtinInfo);
                    Common.getInstance().arrCompleteTinTasks.add(comtinInfo);
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
        Intent intentMain = new Intent(TinTaskActivity.this, MainActivity.class);
        intentMain.putExtra("position", 0);
        startActivity(intentMain);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent;
        switch (v.getId()) {
            case R.id.txtPic:
                setService("The user takes some pictures.");
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mImageCaptureUri = createSaveCropFile();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);
                break;
            case R.id.btnSendForm:
                if(!Common.getInstance().signaturePath.equals("")) {
                    setService("The user clicks the Send Form Button");
                    addPendingTask();
                }else{
                    DialogSelectOption3();
                }
                break;
            case R.id.btn_signature:
                setService("The user clicks the Signature Button");
                startActivity(new Intent(TinTaskActivity.this, TinSignature.class));
                break;
        }
    }
    private void DialogSelectOption3() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Error")
                .setMessage("You have to capture the signature image.")
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

        GpsInfo info = new GpsInfo(TinTaskActivity.this);
        Intent service = new Intent(TinTaskActivity.this, LogService.class);
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
        //String url = "temp" + String.valueOf(nCurIndex) + ".jpg";
        strFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + String.valueOf(nCurIndex) + ".jpg";
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
