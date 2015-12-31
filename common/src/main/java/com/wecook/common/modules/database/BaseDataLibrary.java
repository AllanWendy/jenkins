package com.wecook.common.modules.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wecook.common.modules.asynchandler.AsyncUIHandler;

import java.util.HashSet;

/**
 * 抽象数据库
 *
 * @author kevin created at 9/22/14
 * @version 1.0
 */
public abstract class BaseDataLibrary extends SQLiteOpenHelper {

    private String mDatabaseName;
    private boolean mIsPreCreate = true;
    private SQLiteDatabase mSqlDb;
    private HashSet<Table> sTables = new HashSet<Table>();

    private Context mContext;

    public BaseDataLibrary(Context context, String name, int version) {
        super(context, name, null, version);
        mDatabaseName = name;
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * 获得表
     *
     * @param tableCls
     * @return
     */
    public <T extends Table> T getTable(Class<T> tableCls) {
        for (Table table : sTables) {
            if (table.getClass().getName().equalsIgnoreCase(tableCls.getName())) {
                return (T) table;
            }
        }
        return null;
    }

    /**
     * 添加表数据
     *
     * @param table
     */
    public void addTable(Table table) {
        if (mIsPreCreate) {
            sTables.add(table);
        }
    }

    @Override
    public String getDatabaseName() {
        return mDatabaseName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mIsPreCreate = false;
        for (Table table : sTables) {
            table.onCreate(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Table table : sTables) {
            table.onUpgrade(db, oldVersion, newVersion);
        }
    }

    /**
     * 准备表数据
     */
    public abstract void onPrepareTables();

    /**
     * 打开数据库
     */
    public void open() {
        //StrictMode
        AsyncUIHandler.postParallel(new AsyncUIHandler.AsyncJob() {
            @Override
            public void run() {
                onPrepareTables();
                if (mSqlDb == null) {
                    mSqlDb = BaseDataLibrary.super.getWritableDatabase();
                }
            }
        });
    }

    protected void setSQLiteDatabase(SQLiteDatabase db) {
        mSqlDb = db;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return mSqlDb;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return mSqlDb;
    }
}
