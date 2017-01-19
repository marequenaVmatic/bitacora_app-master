package com.shevchenko.staffapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shevchenko.staffapp.Common.Common;
import com.shevchenko.staffapp.Model.CompleteTask;
import com.shevchenko.staffapp.Model.PendingTasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by shevchenko on 2015-11-29.
 */
public class CompletedTaskActivity extends Activity implements View.OnClickListener {

    private int nTaskId;
    private TextView txtEst, txtMon,txtBill, txtTar, txtNiv, txtExt, txtInt, txtSer, txtSel, txtIlu;
    private TextView txtTitle, txtSummary, txtMachine, txtTaskBusiness;

    private CompleteTask task;
    private LinearLayout lnImages;
    private String filePath, filePath1, filePath2, filePath3, filePath4, filePath5;
    public DownloadThread dThread;
    public ProgressDialog mProgDlg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completedtask);

        txtTitle = (TextView)findViewById(R.id.txtTitle);
        txtSummary = (TextView)findViewById(R.id.txtSummary);
        txtMachine = (TextView)findViewById(R.id.txtMachine);
        txtTaskBusiness = (TextView)findViewById(R.id.txtTaskBusiness);

        txtEst= (TextView)findViewById(R.id.txtEst);
        txtMon= (TextView)findViewById(R.id.txtMon);
        txtBill= (TextView)findViewById(R.id.txtBill);
        txtTar= (TextView)findViewById(R.id.txtTar);
        txtNiv= (TextView)findViewById(R.id.txtNiv);
        txtExt= (TextView)findViewById(R.id.txtExt);
        txtInt= (TextView)findViewById(R.id.txtInt);
        txtSer= (TextView)findViewById(R.id.txtSer);
        txtSel= (TextView)findViewById(R.id.txtSel);
        txtIlu= (TextView)findViewById(R.id.txtIll);

        lnImages = (LinearLayout)findViewById(R.id.lnImages);
        nTaskId = getIntent().getIntExtra("taskid", 0);

        mProgDlg = new ProgressDialog(this);
        mProgDlg.setCancelable(false);
        mProgDlg.setMessage("Wait a little!");
        findTask();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CompletedTaskActivity.this, MainActivity.class);
        intent.putExtra("position", 1);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

    }
    private void findTask()
    {
        boolean bFind = false;
        for(int i = 0; i < Common.getInstance().arrCompleteTasks.size(); i++)
        {
            task = Common.getInstance().arrCompleteTasks.get(i);
            if(task.taskid == nTaskId)
            {
                bFind = true;
                /*
                mProgDlg.show();
                if(!task.file1.equals("")){
                    download(Common.getInstance().server_host + task.file1);
                    filePath1 = filePath;
                }
                if(!task.file2.equals("")){
                    download(Common.getInstance().server_host + task.file2);
                    filePath2 = filePath;
                }
                if(!task.file3.equals("")){
                    download(Common.getInstance().server_host + task.file3);
                    filePath3 = filePath;
                }
                if(!task.file4.equals("")){
                    download(Common.getInstance().server_host + task.file4);
                    filePath4 = filePath;
                }
                if(!task.file5.equals("")){
                    download(Common.getInstance().server_host + task.file5);
                    filePath5 = filePath;
                }*/
                break;
            }
        }
        if(bFind)
        {
            setFields();
            return;
        }
        for(int i = 0; i < Common.getInstance().arrPendingTasks.size(); i++)
        {
            PendingTasks taskInfo = Common.getInstance().arrPendingTasks.get(i);
            if(taskInfo.taskid == nTaskId)
            {
                bFind = true;
                break;
            }
        }
        if(bFind)
        {
            setFieldsPending();
        }
    }
    private void showImage(String path){
        lnImages.setVisibility(View.VISIBLE);
        ImageView imgPhoto = new ImageView(CompletedTaskActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.space_80), (int)getResources().getDimension(R.dimen.space_80));
        params.leftMargin = (int)getResources().getDimension(R.dimen.space_10);
        params.gravity = Gravity.CENTER_VERTICAL;
        //Bitmap bitmap = BitmapFactory.decodeFile(path);
        File imgFile = new File(path);
        Bitmap bitmap = loadLargeBitmapFromFile(imgFile.getAbsolutePath(), this);

        imgPhoto.setImageBitmap(bitmap);
        imgPhoto.setLayoutParams(params);
        imgPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        lnImages.addView(imgPhoto);
    }
    private void setFieldsPending()
    {
        txtTitle.setText(task.Customer);
        txtSummary.setText("Direccion: " + task.Adress + ", " + task.LocationDesc);
        txtMachine.setText("Tipo Maquina: " + task.MachineType);
        txtTaskBusiness.setText("Serie Maquina: " + task.TaskBusinessKey);
        for(int i = 0; i < Common.getInstance().arrTinTasks.size(); i++) {
            if(Common.getInstance().arrTinTasks.get(i).taskid == task.taskid) {

                if(Common.getInstance().arrTinTasks.get(i).cus.equals("estMaq"))
                    txtEst.setText(Common.getInstance().arrTinTasks.get(i).nus);

                if(Common.getInstance().arrTinTasks.get(i).cus.equals("moned"))
                    txtMon.setText(Common.getInstance().arrTinTasks.get(i).nus);

                if(Common.getInstance().arrTinTasks.get(i).cus.equals("billeter"))
                    txtBill.setText(Common.getInstance().arrTinTasks.get(i).nus);

                if(Common.getInstance().arrTinTasks.get(i).cus.equals("tarjet"))
                    txtTar.setText(Common.getInstance().arrTinTasks.get(i).nus);

                if(Common.getInstance().arrTinTasks.get(i).cus.equals("nivAb"))
                    txtNiv.setText(Common.getInstance().arrTinTasks.get(i).nus);

                if(Common.getInstance().arrTinTasks.get(i).cus.equals("higEx"))
                    txtExt.setText(Common.getInstance().arrTinTasks.get(i).nus);

                if(Common.getInstance().arrTinTasks.get(i).cus.equals("higIn"))
                    txtInt.setText(Common.getInstance().arrTinTasks.get(i).nus);

                if(Common.getInstance().arrTinTasks.get(i).cus.equals("atrSm"))
                    txtSer.setText(Common.getInstance().arrTinTasks.get(i).nus);

                if(Common.getInstance().arrTinTasks.get(i).cus.equals("atrSen"))
                    txtSel.setText(Common.getInstance().arrTinTasks.get(i).nus);

                if(Common.getInstance().arrTinTasks.get(i).cus.equals("atrIlu"))
                    txtIlu.setText(Common.getInstance().arrTinTasks.get(i).nus);
            }
        }

        if(task.file1 != null){
            showImage(task.file1);
        }
        if(task.file2 != null){
            showImage(task.file2);
        }
        if(task.file3 != null){
            showImage(task.file3);
        }
        if(task.file4 != null){
            showImage(task.file4);
        }
        if(task.file5 != null){
            showImage(task.file5);
        }
    }
    private void setFields()
    {
        txtTitle.setText(task.Customer);
        txtSummary.setText("Direccion: " + task.Adress + ", " + task.LocationDesc);
        txtMachine.setText("Tipo Maquina: " + task.MachineType);
        txtTaskBusiness.setText("Serie Maquina: " + task.TaskBusinessKey);
        for(int i = 0; i < Common.getInstance().arrCompleteTinTasks.size(); i++) {
            if(Common.getInstance().arrCompleteTinTasks.get(i).taskid == task.taskid) {

                if(Common.getInstance().arrCompleteTinTasks.get(i).cus.equals("estMaq"))
                    txtEst.setText(Common.getInstance().arrCompleteTinTasks.get(i).nus);

                if(Common.getInstance().arrCompleteTinTasks.get(i).cus.equals("moned"))
                    txtMon.setText(Common.getInstance().arrCompleteTinTasks.get(i).nus);

                if(Common.getInstance().arrCompleteTinTasks.get(i).cus.equals("billeter"))
                    txtBill.setText(Common.getInstance().arrCompleteTinTasks.get(i).nus);

                if(Common.getInstance().arrCompleteTinTasks.get(i).cus.equals("tarjet"))
                    txtTar.setText(Common.getInstance().arrCompleteTinTasks.get(i).nus);

                if(Common.getInstance().arrCompleteTinTasks.get(i).cus.equals("nivAb"))
                    txtNiv.setText(Common.getInstance().arrCompleteTinTasks.get(i).nus);

                if(Common.getInstance().arrCompleteTinTasks.get(i).cus.equals("higEx"))
                    txtExt.setText(Common.getInstance().arrCompleteTinTasks.get(i).nus);

                if(Common.getInstance().arrCompleteTinTasks.get(i).cus.equals("higIn"))
                    txtInt.setText(Common.getInstance().arrCompleteTinTasks.get(i).nus);

                if(Common.getInstance().arrCompleteTinTasks.get(i).cus.equals("atrSm"))
                    txtSer.setText(Common.getInstance().arrCompleteTinTasks.get(i).nus);

                if(Common.getInstance().arrCompleteTinTasks.get(i).cus.equals("atrSen"))
                    txtSel.setText(Common.getInstance().arrCompleteTinTasks.get(i).nus);

                if(Common.getInstance().arrCompleteTinTasks.get(i).cus.equals("atrIlu"))
                    txtIlu.setText(Common.getInstance().arrCompleteTinTasks.get(i).nus);
            }
        }
        ///SystemClock.sleep(3000);
        mProgDlg.dismiss();
        if(!task.file1.equals("")){
            showImage(task.file1);
        }
        if(!task.file2.equals("")){
            showImage(task.file2);
        }
        if(!task.file3.equals("")){
            showImage(task.file3);
        }
        if(!task.file4.equals("")){
            showImage(task.file4);
        }
        if(!task.file5.equals("")){
            showImage(task.file5);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    void download(String path) {
        String[] path_array = path.split("/");
        String FileName = path_array[path_array.length - 1];

        String save_path = Environment.getExternalStorageDirectory() + "/staffapp";

        File dir = new File(save_path);
        if (!dir.exists())
            dir.mkdir();
        save_path += "/" + FileName;
        filePath = save_path;
        if (new File(save_path).exists() == false) {
            //loading.setVisibility(View.VISIBLE);
            dThread = new DownloadThread(path, save_path);
            dThread.start();
        }
    }
    class DownloadThread extends Thread {
        String ServerUrl;
        String LocalPath;

        DownloadThread(String serverPath, String localPath) {
            ServerUrl = serverPath;
            LocalPath = localPath;
        }

        @Override
        public void run() {
            URL videourl;
            int Read;
            try {
                videourl = new URL(ServerUrl);
                HttpURLConnection conn = (HttpURLConnection) videourl
                        .openConnection();
                int len = conn.getContentLength();
                byte[] tmpByte = new byte[len];
                InputStream is = conn.getInputStream();
                File file = new File(LocalPath);
                FileOutputStream fos = new FileOutputStream(file);
                for (;;) {
                    Read = is.read(tmpByte);
                    if (Read <= 0) {
                        break;
                    }
                    fos.write(tmpByte, 0, Read);
                }
                is.close();
                fos.close();
                conn.disconnect();

            } catch (MalformedURLException e) {
                Log.e("ERROR1", e.getMessage());
            } catch (IOException e) {
                Log.e("ERROR2", e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
