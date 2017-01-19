package com.shevchenko.staffapp;
/*
This is the dialog when the user press the Guardar button in the abatec main screen.
This dialog shows the every function status.
 */
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Jimmy-PC on 5/19/2016.
 */
public class AbastecTaskDlg extends Dialog implements View.OnClickListener {
    private Context mContext;
    private OnCancelOrderListener mListener;
    private int mQuantity = 0;
    private boolean mRecaudado = false;
    private int mContadores = 0;
    private int mCaptura = 0;
    private int mCapTar = 0;
    private LinearLayout lnContainer;
    private Button btnOK, btnCancel;
    LinearLayout lnAbastec, lnRecaudado, lnContadores, lnCaptura, lnCapTar;
    TextView txtAbastec, txtRecaudado, txtContadores, txtCaptura, txtCapTar;

    public void setQuantity(int quantity) { mQuantity = quantity; }
    public void setRecaudado(boolean recaudado) { mRecaudado = recaudado; }
    public void setContadores(int contadores) { mContadores = contadores; }
    public void setCaptura(int captura) { mCaptura = captura; }
    public void setCapTar(int capTar) { mCapTar = capTar; }

    public void setListener(OnCancelOrderListener listener) {
        mListener = listener;
    }

    public AbastecTaskDlg(Context context) {
        super(context);
        mContext = context;

    }

    public AbastecTaskDlg(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    public AbastecTaskDlg(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirmtask);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        lnAbastec = (LinearLayout)findViewById(R.id.lnAbastec);
        lnRecaudado = (LinearLayout)findViewById(R.id.lnRecaudado);
        lnContadores = (LinearLayout)findViewById(R.id.lnContadores);
        lnCaptura = (LinearLayout)findViewById(R.id.lnCaptura);
        lnCapTar = (LinearLayout)findViewById(R.id.lnCapTar);

        txtAbastec = (TextView)findViewById(R.id.txtAbastec);
        txtRecaudado = (TextView)findViewById(R.id.txtRecaudado);
        txtContadores = (TextView)findViewById(R.id.txtContadores);
        txtCaptura = (TextView)findViewById(R.id.txtCaptura);
        txtCapTar = (TextView)findViewById(R.id.txtCapTar);

        btnOK = (Button)findViewById(R.id.btnOK);
        btnOK.setOnClickListener(this);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        lnContainer = (LinearLayout)findViewById(R.id.lnContainer);

        if(mQuantity == 0){
            lnAbastec.setVisibility(View.GONE);
        }else{
            lnAbastec.setVisibility(View.VISIBLE);
            txtAbastec.setText(String.valueOf(mQuantity));
        }

        lnRecaudado.setVisibility(View.VISIBLE);
        if(mRecaudado == false){
            txtRecaudado.setText("NO");
        }else{
            txtRecaudado.setText("SI");
        }

        lnContadores.setVisibility(View.VISIBLE);
        if(mContadores == 0){
            txtContadores.setText("NO");
        }else{
            txtContadores.setText("SI");
        }

        lnCaptura.setVisibility(View.VISIBLE);
        if(mCaptura == 0){
            txtCaptura.setText("NO");
        }else{
            txtCaptura.setText("SI");
        }
        lnCapTar.setVisibility(View.VISIBLE);
        if(mCapTar == 0){
            txtCapTar.setText("NO");
        }else{
            txtCapTar.setText("SI");
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnCancel) {
            this.dismiss();
        } else if(v.getId() == R.id.btnOK) {
            this.dismiss();
            mListener.OnCancel("yes", 1);
        }
    }
//////////////////
    public interface OnCancelOrderListener {
        void OnCancel(String strReason, int iType);
    }
}
