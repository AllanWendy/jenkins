package com.wecook.sdk.dbprovider.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import com.wecook.common.modules.database.BaseItem;
import com.wecook.common.modules.database.BaseTable;

/**
 * 菜谱食材关系表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/11/14
 */
public class RecipeIngredientTable extends BaseTable<RecipeIngredientTable.RecipeIngredientDB> {

    public static final String TABLE_NAME = "tb_recipe_ingredient";

    public static final String RECIPE_ID = "recipeId";
    public static final String NAME = "ingredientName";
    public static final String TYPE = "ingredientType";
    public static final String URL = "ingredientUrl";
    public static final String NAME_PINYIN = "ingredientNamePinYin";

    public RecipeIngredientTable(SQLiteOpenHelper db) {
        super(TABLE_NAME, db);
    }

    @Override
    public RecipeIngredientDB getItemFromCursor(Cursor cursor) {
        RecipeIngredientDB recipeDB = new RecipeIngredientDB();
        recipeDB.recipeId = getValue(cursor, RECIPE_ID, String.class);
        recipeDB.ingredientName = getValue(cursor, NAME, String.class);
        recipeDB.ingredientNamePinYin = getValue(cursor, NAME_PINYIN, String.class);
        recipeDB.ingredientType = getValue(cursor, TYPE, String.class);
        recipeDB.ingredientUrl = getValue(cursor, URL, String.class);
        return recipeDB;
    }

    @Override
    public ContentValues getValueFromItem(RecipeIngredientDB item) {
        ContentValues values = new ContentValues();
        values.put(RECIPE_ID, item.recipeId);
        values.put(NAME, item.ingredientName);
        values.put(NAME_PINYIN, item.ingredientNamePinYin);
        values.put(TYPE, item.ingredientType);
        values.put(URL, item.ingredientUrl);
        return values;
    }

    public static class RecipeIngredientDB extends BaseItem {
        public String recipeId;
        public String ingredientType;
        public String ingredientUrl;
        public String ingredientName;
        public String ingredientNamePinYin;
    }
}
