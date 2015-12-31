package com.wecook.sdk.dbprovider.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import com.wecook.common.modules.database.BaseItem;
import com.wecook.common.modules.database.BaseTable;

/**
 * 菜谱表
 *
 * @author kevin created at 9/22/14
 * @version 1.0
 */
public class RecipeTable extends BaseTable<RecipeTable.RecipeDB> {

    public static final String TABLE_NAME = "tb_food_recipe";

    public static final String RECIPE_ID = "id";
    public static final String USER_ID = "uid";
    public static final String NAME = "name";
    public static final String IMAGE_ID = "imageId";
    public static final String LOCAL_IMAGE = "localImage";
    public static final String ONLINE_IMAGE = "onlineImage";
    public static final String DESCRIPTION = "description";
    public static final String INGREDIENTS = "ingredients";
    public static final String ASSISTS = "assists";
    public static final String STEPS = "steps";
    public static final String TAGS = "tags";
    public static final String DIFFICULTY = "difficulty";
    public static final String COOK_TIME = "cookTime";
    public static final String TIPS = "tips";

    public static class RecipeDB extends BaseItem {
        public static final int STATE_PUBLISH = 1;//已发布状态
        public static final int STATE_EDIT = 2;//编辑状态
        public String id;//菜谱id
        public String uid;//用户id
        public String name;//名称
        public String imageId;//主图id
        public String localImage;//主图本地路径
        public String onlineImage;//主图在线路径
        public String description;//描述
        public String ingredients;//食材json
        public String assists;//辅材json
        public String steps;//步骤json
        public String tags;//标签
        public String difficulty;//难度
        public String cookTime;//烹饪时间
        public String tips;//小提示
    }

    public RecipeTable(SQLiteOpenHelper db) {
        super(TABLE_NAME, db);
    }

    @Override
    public RecipeDB getItemFromCursor(Cursor cursor) {
        RecipeDB recipeDB = new RecipeDB();
        recipeDB.id = getValue(cursor, RECIPE_ID, String.class);
        recipeDB.uid = getValue(cursor, USER_ID, String.class);
        recipeDB.name = getValue(cursor, NAME, String.class);
        recipeDB.imageId = getValue(cursor, IMAGE_ID, String.class);
        recipeDB.localImage = getValue(cursor, LOCAL_IMAGE, String.class);
        recipeDB.onlineImage = getValue(cursor, ONLINE_IMAGE, String.class);
        recipeDB.description = getValue(cursor, DESCRIPTION, String.class);
        recipeDB.ingredients = getValue(cursor, INGREDIENTS, String.class);
        recipeDB.assists = getValue(cursor, ASSISTS, String.class);
        recipeDB.steps = getValue(cursor, STEPS, String.class);
        recipeDB.tags = getValue(cursor, TAGS, String.class);
        recipeDB.difficulty = getValue(cursor, DIFFICULTY, String.class);
        recipeDB.cookTime = getValue(cursor, COOK_TIME, String.class);
        recipeDB.tips = getValue(cursor, TIPS, String.class);
        return recipeDB;
    }

    @Override
    public ContentValues getValueFromItem(RecipeDB item) {
        ContentValues values = new ContentValues();
        values.put(RECIPE_ID, item.id);
        values.put(USER_ID, item.uid);
        values.put(NAME, item.name);
        values.put(IMAGE_ID, item.imageId);
        values.put(LOCAL_IMAGE, item.localImage);
        values.put(ONLINE_IMAGE, item.onlineImage);
        values.put(DESCRIPTION, item.description);
        values.put(INGREDIENTS, item.ingredients);
        values.put(ASSISTS, item.assists);
        values.put(STEPS, item.steps);
        values.put(TAGS, item.tags);
        values.put(DIFFICULTY, item.difficulty);
        values.put(COOK_TIME, item.cookTime);
        values.put(TIPS, item.tips);
        return values;
    }

}