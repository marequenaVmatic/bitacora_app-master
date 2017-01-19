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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shevchenko.staffapp.Common.Common;
import com.shevchenko.staffapp.Model.CompleteTask;
import com.shevchenko.staffapp.Model.CompltedTinTask;
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
public class CompletedTinTaskActivity extends Activity implements View.OnClickListener {

    private int nTaskId;
    private TextView txtTitle;
    private LinearLayout lnContainer;
    private CompleteTask task;
    private LinearLayout lnImages;
    private String filePath, filePath1, filePath2, filePath3, filePath4, filePath5;
    public DownloadThread dThread;
    public ProgressDialog mProgDlg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abacompletedtask);

        txtTitle = (TextView)findViewById(R.id.txtTitle);
        lnContainer = (LinearLayout)findViewById(R.id.lnContainer);
        lnImages = (LinearLayout)findViewById(R.id.lnImages);
        nTaskId = getIntent().getIntExtra("taskid", 0);

        mProgDlg = new ProgressDialog(this);
        mProgDlg.setCancelable(false);
        mProgDlg.setMessage("Wait a little!");
        findTask();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CompletedTinTaskActivity.this, MainActivity.class);
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
                }
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
        ImageView imgPhoto = new ImageView(CompletedTinTaskActivity.this);
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
    private void addProduct(CompltedTinTask taskinfo){
        LinearLayout lnChild = new LinearLayout(CompletedTinTaskActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = (int) getResources().getDimension(R.dimen.space_15);
        params.rightMargin = (int) getResources().getDimension(R.dimen.space_15);
        params.topMargin = (int) getResources().getDimension(R.dimen.space_10);
        lnChild.setLayoutParams(params);
        lnChild.setOrientation(LinearLayout.HORIZONTAL);
        lnContainer.addView(lnChild);

        TextView txtContent = new TextView(CompletedTinTaskActivity.this);
        LinearLayout.LayoutParams param_text = new LinearLayout.LayoutParams(0, (int) getResources().getDimension(R.dimen.space_50));
        param_text.weight = 40;
        param_text.gravity = Gravity.CENTER;
        txtContent.setText(taskinfo.nus);
        txtContent.setLayoutParams(param_text);
        //txtContent.setTextSize((float) getResources().getDimension(R.dimen.space_15));
        txtContent.setTextColor(getResources().getColor(R.color.clr_graqy));
        lnChild.addView(txtContent);

        EditText edtContent = new EditText(CompletedTinTaskActivity.this);
        LinearLayout.LayoutParams param_edt = new LinearLayout.LayoutParams(0, (int) getResources().getDimension(R.dimen.space_40));
        param_edt.weight = 60;
        param_edt.leftMargin = (int) getResources().getDimension(R.dimen.space_3);
        edtContent.setLayoutParams(param_edt);
        ///edtContent.setTextSize((float) getResources().getDimension(R.dimen.space_15));
        edtContent.setTextColor(getResources().getColor(R.color.clr_edit));
        //edtContent.setId(DYNAMIC_EDIT_ID + i + 1);
        //edtContent.setId(i);
        edtContent.setBackgroundResource(R.drawable.back_edit);
        edtContent.setEnabled(false);
        edtContent.setText(taskinfo.quantity);
        lnChild.addView(edtContent);
    }
    private void setFieldsPending()
    {
        txtTitle.setText(task.RutaAbastecimiento);

        for(int i = 0; i < Common.getInstance().arrTinTasks.size(); i++){
            if(Common.getInstance().arrTinTasks.get(i).taskid == nTaskId){
                CompltedTinTask tin = new CompltedTinTask(Common.getInstance().arrTinTasks.get(i).userid, Common.getInstance().arrTinTasks.get(i).taskid, Common.getInstance().arrTinTasks.get(i).tasktype, Common.getInstance().arrTinTasks.get(i).RutaAbastecimiento, Common.getInstance().arrTinTasks.get(i).cus, Common.getInstance().arrTinTasks.get(i).nus, Common.getInstance().arrTinTasks.get(i).quantity);
                addProduct(tin);
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
        txtTitle.setText(task.RutaAbastecimiento);

        for(int i = 0; i < Common.getInstance().arrCompleteTinTasks.size(); i++){
            if(Common.getInstance().arrCompleteTinTasks.get(i).taskid == nTaskId){
                addProduct(Common.getInstance().arrCompleteTinTasks.get(i));
            }
        }

        ///SystemClock.sleep(3000);
        mProgDlg.dismiss();
        if(!task.file1.equals("")){
            showImage(filePath1);
        }
        if(!task.file2.equals("")){
            showImage(filePath2);
        }
        if(!task.file3.equals("")){
            showImage(filePath3);
        }
        if(!task.file4.equals("")){
            showImage(filePath4);
        }
        if(!task.file5.equals("")){
            showImage(filePath5);
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
