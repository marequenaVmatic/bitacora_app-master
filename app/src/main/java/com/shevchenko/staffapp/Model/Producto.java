package com.shevchenko.staffapp.Model;

import java.io.Serializable;

/**
 * Created by shevchenko on 2015-11-29.
 */
public class Producto implements Serializable {

    public final static String TABLENAME = "tb_producto";
    public final static String CUS = "cus";
    public final static String NUS = "nus";

    public String cus;
    public String nus;

    public Producto()
    {
        this.cus = "";
        this.nus = "";
    }
    public Producto(String cus, String nus)
    {
        this.cus = cus;
        this.nus = nus;
    }
}
