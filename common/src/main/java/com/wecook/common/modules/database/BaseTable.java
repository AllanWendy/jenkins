package com.wecook.common.modules.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.utils.JavaRefactorUtils;
import com.wecook.common.utils.StringUtils;

import java.lang.reflect.Field;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽象数据表
 *
 * @author kevin created at 9/22/14
 * @version 1.0
 */
public abstract class BaseTable<T extends BaseItem> implements Table<T> {

    private static final String[][] sColumnTypes = {
            {"int", "INTEGER"},
            {"long", "INTEGER"},
            {"string", "TEXT"},
            {"boolean", "TEXT"},
    };

    private String mTableName;
    private SQLiteOpenHelper mSqliteOpenHelper;

    public static final String COLUMN_ROW_ID = "rowid";//伪列
    public static final String COLUMN_CREATE_TIME = "createTime";
    public static final String COLUMN_MODIFY_TIME = "modifyTime";

    public BaseTable(String tableName, SQLiteOpenHelper db) {
        mTableName = tableName;
        mSqliteOpenHelper = db;
    }

    /**
     * 获得String
     *
     * @param cursor
     * @param columnName
     * @return
     */
    @SuppressLint("UseValueOf")
    @SuppressWarnings({"hiding", "unchecked"})
    public <T> T getValue(Cursor cursor, String columnName, Class<T> t) {
        int index = cursor.getColumnIndex(columnName);
        if (String.class.getName().equals(t.getName())) {
            if (index >= 0)
                return (T) cursor.getString(index);
            return (T) "";
        } else if (Integer.class.getName().equals(t.getName())) {
            if (index >= 0)
                return (T) new Integer(cursor.getInt(index));
            return (T) new Integer(0);
        } else if (Long.class.getName().equals(t.getName())) {
            if (index >= 0)
                return (T) new Long(cursor.getLong(index));
            return (T) new Long(0);
        } else if (Float.class.getName().equals(t.getName())) {
            if (index >= 0)
                return (T) new Float(cursor.getFloat(index));
            return (T) new Float(0);
        } else if (Date.class.getName().equals(t.getName())) {
            if (index >= 0)
                return (T) new Date(cursor.getLong(index));
            return (T) new Date(System.currentTimeMillis());
        } else if (Boolean.class.getName().equals(t.getName())) {
            if (index >= 0)
                return (T) Boolean.valueOf(cursor.getString(index));
            return (T) Boolean.FALSE;
        }
        return null;
    }

    /**
     * 获得数据行数
     *
     * @param where
     * @param args
     * @return
     */
    public int getCount(String where, String args[]) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getDatabase();
            if (db == null)
                return 0;
            cursor = db.query(getTableName(), null, where, args, null, null, null);
            return cursor.getCount();
        } catch (Exception e) {
            Logger.e(getTableName(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }


    @Override
    public long insert(T item) {
        try {
            SQLiteDatabase db = getDatabase();
            if (db == null)
                return -1;
            String current = System.currentTimeMillis() + "";
            if (StringUtils.isEmpty(item.createTime)) {
                item.createTime = current;
            }
            item.modifyTime = current;
            return db.insert(getTableName(), null, innerGetValueFromItem(item));
        } catch (Exception e) {
            Logger.e(getTableName(), e);
        }
        return -1;
    }

    @Override
    public void batchInsert(List<T> list) {
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return;
        }
        db.beginTransaction();
        for (T t : list) {
            insert(t);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public int update(T item, String where, String args[]) {
        try {
            SQLiteDatabase db = getDatabase();
            if (db == null)
                return -1;
            db.beginTransaction();
            item.modifyTime = System.currentTimeMillis() + "";
            ContentValues values = innerGetValueFromItem(item);
            int result = db.update(getTableName(), values, where, args);
            db.setTransactionSuccessful();
            db.endTransaction();
            return result;
        } catch (Exception e) {
            Logger.e(getTableName(), e);
        }
        return -1;
    }

    @Override
    public void batchUpdate(List<T> list, String where, String args[]) {
        SQLiteDatabase db = getDatabase();
        if (db == null) {
            return;
        }
        db.beginTransaction();
        for (T t : list) {
            update(t, where, args);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public int delete(String whereClause, String[] whereArgs) {
        try {
            SQLiteDatabase db = getDatabase();
            if (db == null)
                return -1;
            db.beginTransaction();
            int result = db.delete(getTableName(), whereClause, whereArgs);
            db.setTransactionSuccessful();
            db.endTransaction();
            return result;
        } catch (Exception e) {
            Logger.e(getTableName(), e);
        }
        return -1;
    }

    @Override
    public int batchDelete(String idColumn, long[] ids) {
        String idsString = StringUtils.getIdScopeString(ids);
        return batchDelete(idColumn, idsString);
    }

    @Override
    public int batchDelete(String idColumn, String ids) {
        String where = idColumn + " IN (" + ids + ")";
        return delete(where, null);
    }

    public List<T> query(String where, String args[], String orderBy) {
        return query(null, where, args, orderBy);
    }

    @Override
    public List<T> query(String[] columns, String where, String args[], String orderBy) {
        List<T> items = null;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getDatabase();
            if (db == null)
                return null;
            db.beginTransaction();
            items = new ArrayList<T>();
            cursor = db.query(getTableName(), columns, where, args, null, null, orderBy);
            while (cursor.moveToNext()) {
                items.add(innerGetItemFromCursor(cursor));
            }
            db.endTransaction();
        } catch (Exception e) {
            Logger.e(getTableName(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return items;
    }

    @Override
    public SQLiteDatabase getDatabase() {
        if (mSqliteOpenHelper != null) {
            return mSqliteOpenHelper.getWritableDatabase();
        }
        return null;
    }

    @Override
    public String getTableName() {
        return mTableName;
    }

    /**
     * 通过特定条件检索数据
     *
     * @return
     */
    public List<T> queryAll() {
        return query(null, null, null);
    }

    /**
     * 清空数据
     */
    public void clear() {
        onCreate(mSqliteOpenHelper.getWritableDatabase());
    }

    /**
     * 删除表
     *
     * @param db
     */
    protected void dropTable(SQLiteDatabase db) {
        try {
            if (db != null && db.isOpen()) {
                db.execSQL("DROP TABLE IF EXISTS " + mTableName);
            }
        } catch (Throwable e) {
            Logger.e("dropTable", e);
        }

    }

    /**
     * 创建表
     *
     * @param db
     * @param itemCls
     */
    protected void createTable(SQLiteDatabase db, Class<T> itemCls) {
        try {
            String columns = "";
            if (itemCls != null) {
                //获得中的属性集合
                List<Field> fields = JavaRefactorUtils.getAllDeclaredFields(itemCls);
                for (Field field : fields) {
                    try {

                        String fieldName = field.getName();
                        String fieldType = field.getType().getSimpleName();
                        if ("".equals(columns)) {
                            columns += fieldName + " " + getDBType(fieldType);
                        } else {
                            columns += "," + fieldName + " " + getDBType(fieldType);
                        }

                    } catch (Throwable e) {
                        Logger.e("error when createTable", e);
                    }

                }
            }

            if (db != null && db.isOpen()) {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + mTableName + "(" + columns + ");");
            }
        } catch (Throwable e) {
            Logger.e("dropTable", e);
        }
    }

    /**
     * 获得类型的名字
     *
     * @param fieldType
     * @return
     * @throws NoSuchFieldError 没有找到
     */
    private String getDBType(String fieldType){
        for (int i = 0; i < sColumnTypes.length; i++) {
            if (sColumnTypes[i][0].equalsIgnoreCase(fieldType)) {
                return sColumnTypes[i][1];
            }
        }

        Logger.e("No found the " + fieldType + "'s DB type!");
        return "TEXT";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.d("onCreate Table[" + mTableName + "]");
        if (!db.isReadOnly()) {
            dropTable(db);
            createTable(db, JavaRefactorUtils.getGenericType(this, 0));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //nothing
        Logger.d("onUpgrade Table[" + mTableName + "] from " + oldVersion + " to " + newVersion);
        if (!db.isReadOnly()) {
            createTable(db, JavaRefactorUtils.getGenericType(this, 0));
        }
    }

    /**
     * 表增加一列
     *
     * @param db
     * @param tableName 表名
     * @param column 列名
     * @param columnType  仅支持Integer Long String类型
     */
    protected void addColumn(SQLiteDatabase db, String tableName, String column, Class<?> columnType) {
        db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + column + " " + getDBType(columnType.getSimpleName()));
    }

    /**
     * 执行一条sql语句
     *
     * @param sql
     */
    public void exeSQL(String sql) {
        try {
            SQLiteDatabase db = getDatabase();
            if (db == null)
                return;
            db.execSQL(sql);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private T innerGetItemFromCursor(Cursor cursor) {
        T t = getItemFromCursor(cursor);
        t.createTime = getValue(cursor, COLUMN_CREATE_TIME, String.class);
        t.modifyTime = getValue(cursor, COLUMN_MODIFY_TIME, String.class);
        return t;
    }

    private ContentValues innerGetValueFromItem(T item) {
        ContentValues contentValues = getValueFromItem(item);
        contentValues.put(COLUMN_MODIFY_TIME, item.modifyTime);
        contentValues.put(COLUMN_CREATE_TIME, item.createTime);
        return contentValues;
    }

    /**
     * 通过Cursor获得Item
     *
     * @param cursor
     * @return
     */
    public abstract T getItemFromCursor(Cursor cursor);

    /**
     * 通过Item获得ContentValues
     *
     * @param item
     * @return
     */
    public abstract ContentValues getValueFromItem(T item);
}
