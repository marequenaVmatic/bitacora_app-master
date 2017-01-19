package com.shevchenko.staffapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.shevchenko.staffapp.Model.TinTask;

import java.util.ArrayList;

/**
 * Created by Jimmy-PC on 5/19/2016.
 */
public class AbastecDlg extends Dialog implements View.OnClickListener {
    private Context mContext;
    private OnCancelOrderListener mListener;
    private RadioGroup rgCancelType;
    private int mType = 0;
    private ArrayList<TinTask> mTinTasks = new ArrayList<TinTask>();
    private String mResult = "";
    private LinearLayout lnContainer;
    private Button btnOK, btnCancel;
    public void setType(int iType) { mType = iType; }

    public void setListener(OnCancelOrderListener listener) {
        mListener = listener;
    }

    public AbastecDlg(Context context) {
        super(context);
        mContext = context;

    }

    public AbastecDlg(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    public AbastecDlg(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirmabastec);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        btnOK = (Button)findViewById(R.id.btnOK);
        btnOK.setOnClickListener(this);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        lnContainer = (LinearLayout)findViewById(R.id.lnContainer);

        for(int i = 0; i < mTinTasks.size(); i++){
            if(Integer.parseInt(mTinTasks.get(i).quantity)>0) {

                LinearLayout lnChild = new LinearLayout(mContext);
                final int a = i;
                LinearLayout.LayoutParams params_child = new LinearLayout.LayoutParams(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
                params_child.leftMargin = (int) mContext.getResources().getDimension(R.dimen.space_10);
                params_child.rightMargin = (int) mContext.getResources().getDimension(R.dimen.space_10);
                params_child.topMargin = (int) mContext.getResources().getDimension(R.dimen.space_20);
                params.gravity = Gravity.CENTER;
                lnChild.setLayoutParams(params_child);
                lnChild.setOrientation(LinearLayout.HORIZONTAL);
                lnContainer.addView(lnChild, i);

                TextView txtContent = new TextView(mContext);
                LinearLayout.LayoutParams param_text = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT/*(int) getResources().getDimension(R.dimen.space_40)*/);
                param_text.weight = 70;
                param_text.gravity = Gravity.CENTER_VERTICAL;
                txtContent.setText(mTinTasks.get(i).nus);
                txtContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.space_15));
                txtContent.setLayoutParams(param_text);
                txtContent.setTextColor(mContext.getResources().getColor(R.color.clr_white));
                lnChild.addView(txtContent);

                final TextView txtQuantity = new TextView(mContext);
                LinearLayout.LayoutParams param_content = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
                param_content.weight = 30;
                param_content.gravity = Gravity.CENTER;
                param_content.leftMargin = (int) mContext.getResources().getDimension(R.dimen.space_3);
                txtQuantity.setPadding((int) mContext.getResources().getDimension(R.dimen.space_5), (int) mContext.getResources().getDimension(R.dimen.space_5), (int) mContext.getResources().getDimension(R.dimen.space_5), (int) mContext.getResources().getDimension(R.dimen.space_5));
                txtQuantity.setGravity(Gravity.CENTER);
                txtQuantity.setLayoutParams(param_content);
                txtQuantity.setId(i + 1);
                txtQuantity.setText(mTinTasks.get(i).quantity);
                txtQuantity.setBackgroundResource(R.drawable.tineditborder);
                txtQuantity.setTextColor(mContext.getResources().getColor(R.color.clr_graqy));
                txtQuantity.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.space_15));
                lnChild.addView(txtQuantity);
            }
        }

        if(mType == 1){
            btnOK.setVisibility(View.GONE);
            btnCancel.setText("VOLVER");
        }
    }

    public void setTinTasks(ArrayList<TinTask> arrTasks){
        for(int i = 0; i < arrTasks.size(); i++)
            mTinTasks.add(arrTasks.get(i));
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnCancel) {
            if(btnCancel.getText().toString().equals("VOLVER")) {
                this.dismiss();
                mListener.OnCancel("volver", mType);
            }else
                this.dismiss();

        } else if(v.getId() == R.id.btnOK) {
            this.dismiss();
            mListener.OnCancel("yes", mType);
        }
    }

    public interface OnCancelOrderListener {
        void OnCancel(String strReason, int iType);
    }
}
