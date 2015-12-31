package com.wecook.sdk.dbprovider.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wecook.common.modules.database.BaseItem;
import com.wecook.common.modules.database.BaseTable;

/**
 * 消息纪录
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/19/14
 */
public class MessageTable extends BaseTable<MessageTable.MessageDB> {

    public static final String TABLE_NAME = "tb_message";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_MESSAGE_ID = "messageId";
    public static final String COLUMN_RECEIVER_ID = "receiverId";

    public MessageTable(SQLiteOpenHelper db) {
        super(TABLE_NAME, db);
    }

    @Override
    public MessageDB getItemFromCursor(Cursor cursor) {
        MessageDB db = new MessageDB();
        db.content = getValue(cursor, COLUMN_CONTENT, String.class);
        db.messageId = getValue(cursor, COLUMN_MESSAGE_ID, String.class);
        db.receiverId = getValue(cursor, COLUMN_RECEIVER_ID, String.class);
        return db;
    }

    @Override
    public ContentValues getValueFromItem(MessageDB item) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTENT, item.content);
        values.put(COLUMN_MESSAGE_ID, item.messageId);
        values.put(COLUMN_RECEIVER_ID, item.receiverId);
        return values;
    }

    public static class MessageDB extends BaseItem {
        public String content;
        public String messageId;
        public String receiverId;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        if (oldVersion == 1 && newVersion >= 2) {
            //增加字段
            addColumn(db, TABLE_NAME, COLUMN_MODIFY_TIME, String.class);
        }

        if (oldVersion == 2 && newVersion >= 3) {
            //
        }
    }
}
