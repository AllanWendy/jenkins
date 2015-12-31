package com.wecook.sdk.dbprovider;

import android.content.Context;

import com.wecook.common.modules.database.BaseDataLibrary;
import com.wecook.sdk.dbprovider.tables.MessageTable;
import com.wecook.sdk.dbprovider.tables.RecipeTable;
import com.wecook.sdk.dbprovider.tables.ResourceTable;

/**
 * 软件数据库
 *
 * @author kevin created at 9/22/14
 * @version 1.0
 */
public class AppDataLibrary extends BaseDataLibrary {

    private static final String APP_DL_NAME = "wecook-app.db";
    private static final int APP_DL_VERSION = 2;

    public AppDataLibrary(Context context) {
        super(context, APP_DL_NAME, APP_DL_VERSION);
    }

    @Override
    public void onPrepareTables() {
        addTable(new RecipeTable(this));
        addTable(new ResourceTable(this));
        addTable(new MessageTable(this));
    }

}
