package com.wecook.common.modules.database;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * 表接口
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/18/14
 */
public interface Table<T> {

    /**
     * 创建
     *
     * @param db
     */
    public void onCreate(SQLiteDatabase db);

    /**
     * 升级
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    /**
     * 插入一条数据
     *
     * @param item
     */
    public long insert(T item);

    /**
     * 批量插入多条数据
     *
     * @param list
     */
    public void batchInsert(List<T> list);

    /**
     * 更新数据
     *
     * @param item
     * @param where
     * @param args
     * @return
     */
    public int update(T item, String where, String args[]);

    /**
     * 批量更新数据
     *
     * @param list
     * @param where
     * @param args
     */
    public void batchUpdate(List<T> list, String where, String args[]);

    /**
     * 删除数据
     *
     * @param whereClause
     * @param whereArgs
     */
    public int delete(String whereClause, String[] whereArgs);

    /**
     * 批量删除数据
     *
     * @param idColumn
     * @param ids
     */
    public int batchDelete(String idColumn, long[] ids);

    /**
     * 批量删除数据
     *
     * @param idColumn
     * @param ids
     */
    public int batchDelete(String idColumn, String ids);

    /**
     * 查询数据
     *
     * @param columns
     * @param where
     * @param args
     * @param orderBy
     * @return
     */
    public List<T> query(String[] columns, String where, String args[], String orderBy);


    /**
     * 获得可写入的数据库对象
     *
     * @return
     */
    public SQLiteDatabase getDatabase();

    /**
     * 获得表名
     *
     * @return
     */
    public String getTableName();

}
