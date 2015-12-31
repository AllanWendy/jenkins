package com.wecook.sdk.dbprovider.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wecook.common.modules.database.BaseItem;
import com.wecook.common.modules.database.BaseTable;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 原材料数据表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/11/14
 */
public class ResourceTable extends BaseTable<ResourceTable.ResourceDB> {

    public static final String TABLE_NAME = "tb_resource";

    public static final String USER_ID = "userId";
    public static final String RESOURCE_ID = "id";
    public static final String NAME = "name";
    public static final String PINYIN = "pinyin";
    public static final String PINYIN_HEAD = "pinyinHead";
    public static final String TYPE = "type";
    public static final String IMAGE = "image";
    public static final String SHELFLIFE = "shelflife";

    public ResourceTable(SQLiteOpenHelper db) {
        super(TABLE_NAME, db);
    }

    public static class ResourceDB extends BaseItem {
        public String userId;
        public String id;
        public String name;
        public String pinyin;
        public String pinyinHead;
        public String type;
        public String image;
        public String shelflife;

        public void copy(FoodResource resource) {
            userId = UserProperties.getUserId();
            id = resource.getId();
            name = resource.getName();
            type = resource.getType();
            image = resource.getImage();
            shelflife = resource.getShelflife();
            createTime = resource.getCreateTime();
        }
    }

    @Override
    public ResourceDB getItemFromCursor(Cursor cursor) {
        ResourceDB resourceDB = new ResourceDB();
        resourceDB.userId = getValue(cursor, USER_ID, String.class);
        resourceDB.id = getValue(cursor, RESOURCE_ID, String.class);
        resourceDB.name = getValue(cursor, NAME, String.class);
        resourceDB.type = getValue(cursor, TYPE, String.class);
        resourceDB.pinyinHead = getValue(cursor, PINYIN_HEAD, String.class);
        resourceDB.pinyin = getValue(cursor, PINYIN, String.class);
        resourceDB.image = getValue(cursor, IMAGE, String.class);
        resourceDB.shelflife = getValue(cursor, SHELFLIFE, String.class);
        return resourceDB;
    }

    @Override
    public ContentValues getValueFromItem(ResourceDB item) {
        ContentValues values = new ContentValues();
        values.put(USER_ID, item.userId);
        values.put(RESOURCE_ID, item.id);
        values.put(NAME, item.name);
        values.put(PINYIN, item.pinyin);
        values.put(PINYIN_HEAD, item.pinyinHead);
        values.put(TYPE, item.type);
        values.put(IMAGE, item.image);
        values.put(SHELFLIFE, item.shelflife);
        return values;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        if (oldVersion == 1 && newVersion == 2) {
            addColumn(db, TABLE_NAME, COLUMN_MODIFY_TIME, String.class);
        }
    }

}
