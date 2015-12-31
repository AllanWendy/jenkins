package com.wecook.sdk.dbprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.wecook.common.modules.database.BaseDataLibrary;
import com.wecook.common.utils.FileUtils;
import com.wecook.sdk.dbprovider.tables.RecipeIngredientTable;

import java.io.File;

/**
 * 启发式搜索数据库
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/12/14
 */
public class InspireSearchDataLibrary extends BaseDataLibrary {

    public static String DATABASE_NAME = "inspire.db";
    public static int DATABASE_VERSION = 4;
    private Context mContext;

    public InspireSearchDataLibrary(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onPrepareTables() {
        addTable(new RecipeIngredientTable(this));
    }

    @Override
    public void open() {
//        super.open();
        File dbFile = mContext.getDatabasePath(DATABASE_NAME);
        if (FileUtils.isExist(dbFile)) {
            onPrepareTables();
            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getPath(),
                    null, SQLiteDatabase.OPEN_READONLY);
            setSQLiteDatabase(db);
            onCreate(db);
        }
    }
}
