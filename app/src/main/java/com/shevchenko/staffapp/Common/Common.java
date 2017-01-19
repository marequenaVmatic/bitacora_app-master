package com.shevchenko.staffapp.Common;

import android.os.Environment;
import android.os.StatFs;
import com.shevchenko.staffapp.Model.Category;
import com.shevchenko.staffapp.Model.CommentError;
import com.shevchenko.staffapp.Model.CompleteDetailCounter;
import com.shevchenko.staffapp.Model.CompleteTask;
import com.shevchenko.staffapp.Model.CompltedTinTask;
import com.shevchenko.staffapp.Model.DetailCounter;
import com.shevchenko.staffapp.Model.LoginUser;
import com.shevchenko.staffapp.Model.MachineCounter;
import com.shevchenko.staffapp.Model.PendingTasks;
import com.shevchenko.staffapp.Model.Report;
import com.shevchenko.staffapp.Model.TaskInfo;
import com.shevchenko.staffapp.Model.TaskType;
import com.shevchenko.staffapp.Model.TinTask;
import com.shevchenko.staffapp.Model.Producto;
import com.shevchenko.staffapp.Model.Producto_RutaAbastecimento;
import com.shevchenko.staffapp.Model.User;

import org.w3c.dom.Comment;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by shevchenko on 2015-11-26.
 * This has the global variables that uses in the app.
 */
public class Common {
    private static Common s_instance = null;

    public String gBatteryPercent;
    public boolean gChargingUSB;
    public boolean gChargingOther;

    public static Common getInstance() {
        if (s_instance == null) {
            s_instance = new Common();

        }
        synchronized (s_instance) {
            return s_instance;
        }
    }

    public final static String PREF_KEY_TEMPSAVE = "TEMPSAVE";
    public final static String PREF_KEY_TEMPSAVE_ABASTEC = "TEMPSAVE::ABASTEC";
    public final static String PREF_KEY_TEMPSAVE_CONTADORES = "TEMPSAVE::CONTADORES";
    public final static String PREF_KEY_TEMPSAVE_RECAUDAR = "TEMPSAVE::RECAUDAR";

    public final static String PREF_KEY_CLOSEDTIME = "CLOSEDTIME";

    public final static String PREF_KEY_LATEST_LAT = "LATEST::LAT";
    public final static String PREF_KEY_LATEST_LNG = "LATEST::LNG";

    public ArrayList<TaskInfo> arrIncompleteTasks;
    public ArrayList<CompleteTask> arrCompleteTasks;
    public ArrayList<PendingTasks> arrPendingTasks;
    public ArrayList<TinTask> arrTinTasks;
    public ArrayList<CompltedTinTask> arrCompleteTinTasks;
    public ArrayList<Category> arrCategory;
    public ArrayList<Producto> arrProducto;
    public ArrayList<Producto_RutaAbastecimento> arrProducto_Ruta;
    public ArrayList<User> arrUsers;
    public ArrayList<TaskType> arrTaskTypes;
    public ArrayList<CommentError> arrCommentErrors;
    public ArrayList<MachineCounter> arrMachineCounters;
    public ArrayList<TaskInfo> arrIncompleteTasks_copy;
    public ArrayList<CompleteTask> arrCompleteTasks_copy;
    public ArrayList<PendingTasks> arrPendingTasks_copy;
    public ArrayList<CompltedTinTask> arrCompleteTinTasks_copy;
    public ArrayList<TinTask> arrTinTasks_copy;
    public ArrayList<Category> arrCategory_copy;
    public ArrayList<Producto> arrProducto_copy;
    public String server_host = "http://201.236.65.58:6030/Upload/";  //producion
    //public String server_host = "http://api-desa.vendomatica.cl:68/Upload/";  //Desarrollo
    public boolean isUpload = false;
    public String latitude;
    public String longitude;
    public String signaturePath;
    public ArrayList<TinTask> arrAbastTinTasks;
    public ArrayList<DetailCounter> arrDetailCounters;
    public ArrayList<CompleteDetailCounter> arrCompleteDetailCounters;
    public ArrayList<Report> arrReports;
    public boolean isAbastec = false;
    public boolean isNeedRefresh = false;
    public boolean dayly = false;
    public String selectedNus;
    public String selectedQuantity;
    public boolean capture;
    public boolean mainAbastec = false;
    public boolean mainNoClick = false;
    public Timer mTimer;
    public Common()
    {
        arrIncompleteTasks = new ArrayList<TaskInfo>();
        arrCompleteTasks = new ArrayList<CompleteTask>();
        arrPendingTasks = new ArrayList<PendingTasks>();
        arrTinTasks = new ArrayList<TinTask>();
        arrCompleteTinTasks = new ArrayList<CompltedTinTask>();
        arrCategory = new ArrayList<Category>();
        arrProducto = new ArrayList<Producto>();
        arrProducto_Ruta = new ArrayList<Producto_RutaAbastecimento>();
        arrUsers = new ArrayList<User>();
        arrTaskTypes = new ArrayList<TaskType>();
        arrCommentErrors = new ArrayList<CommentError>();
        arrMachineCounters = new ArrayList<MachineCounter>();
        arrIncompleteTasks_copy = new ArrayList<TaskInfo>();
        arrCompleteTasks_copy = new ArrayList<CompleteTask>();
        arrPendingTasks_copy = new ArrayList<PendingTasks>();
        arrCompleteTinTasks_copy = new ArrayList<CompltedTinTask>();
        arrCategory_copy = new ArrayList<Category>();
        arrProducto_copy = new ArrayList<Producto>();
        arrTinTasks_copy = new ArrayList<TinTask>();
        latitude = new String();
        longitude = new String();
        signaturePath = new String();
        arrAbastTinTasks = new ArrayList<TinTask>();
        arrDetailCounters = new ArrayList<DetailCounter>();
        arrCompleteDetailCounters = new ArrayList<CompleteDetailCounter>();
        gBatteryPercent = "Unknown";
        gChargingUSB = false;
        gChargingOther = false;
        arrReports = new ArrayList<Report>();
        isAbastec = false;
        mainAbastec = false;
        mainNoClick = false;
        mTimer = new Timer();
    }

    private LoginUser loginUser;
    public final static int 		LEFTMENU_ANITIME = 250;

    public LoginUser getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(LoginUser loginUser) {
        this.loginUser = loginUser;
    }

    public boolean isPendingTaks(int taskid)
    {
        boolean bRet = false;
        for(int i = 0; i < arrPendingTasks.size(); i++)
        {
            PendingTasks task = arrPendingTasks.get(i);
            if(task.taskid == taskid)
            {
                bRet = true;
                break;
            }
        }
        return bRet;
    }

  public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return formatSize(availableBlocks * blockSize);
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }
}
