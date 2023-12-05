package com.github.tvbox.osc.bean;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.App;

import kotlin.jvm.internal.markers.KMutableMap;

public class PkgEntry {
    private String name;
    private Drawable icon;
    private String  pkg;
    private int id;

    private LinearLayout btn;
    public String getName() {
        return name;
    }


    public void setId(int id){
        this.id=id;
    }

    public int getId() {
        return id;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setBtn(LinearLayout btn) {
        this.btn = btn;
    }

    public boolean setEntry( String name,String pkg) {
        if(pkg==""||name==""){
            return false;
        }

        this.pkg = pkg;
        this.name= name;
        try {
            this.icon = App.getInstance().getPackageManager().getApplicationIcon(pkg);
        }catch (PackageManager.NameNotFoundException e){
//            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Intent getIntent(){
        Intent intent= App.getInstance().getPackageManager().getLaunchIntentForPackage(this.pkg);
        return intent;
    }
}

