package com.wecook.sdk.policy;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LruCache;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.database.DataLibraryManager;
import com.wecook.common.modules.filemaster.FileMaster;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodIngredient;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.sdk.dbprovider.InspireSearchDataLibrary;
import com.wecook.sdk.dbprovider.tables.RecipeIngredientTable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 厨房食材启发式检索策略
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/10/14
 */
public class KitchenInspireSearchPolicy {
    private static final String DB_ZIP_FILE_NAME = InspireSearchDataLibrary.DATABASE_NAME
            + "_" + InspireSearchDataLibrary.DATABASE_VERSION
            + ".zip";
    private static final String DB_FILE_NAME = InspireSearchDataLibrary.DATABASE_NAME;
    private static final String TAG = "inspire-search";

    /**
     * 食材库
     */
    private List<FoodIngredient> mAllIngredients;

    /**
     * 已选中食材库
     */
    private List<FoodIngredient> mSelectedIngredients;

    /**
     * 备选的缓存库
     */
    private LruCache<String, List<FoodIngredient>> mFilterCache;

    /**
     * 可做菜的缓存库
     */
    private LruCache<String, List<FoodRecipe>> mFoodRecipeCache;

    /**
     * 备选库的key
     */
    private String mSelectedFilterKey;

    private OnRecipeChangedListener mRecipeChangedListener;

    private static KitchenInspireSearchPolicy sInstance;

    private boolean mHasPrepared;

    private String mSelectedRecipeIds;

    private AtomicInteger mSearchThreads = new AtomicInteger();

    private InspireSearchDataLibrary mInspireSearchDataLibrary = DataLibraryManager.getDataLibrary(InspireSearchDataLibrary.class);

    private KitchenInspireSearchPolicy() {
        mFilterCache = new LruCache<String, List<FoodIngredient>>((int) FileUtils.ONE_MB);
        mFoodRecipeCache = new LruCache<String, List<FoodRecipe>>((int) FileUtils.ONE_MB);
        mSelectedIngredients = new ArrayList<FoodIngredient>();
        mAllIngredients = new ArrayList<FoodIngredient>();
    }

    public static KitchenInspireSearchPolicy getInstance() {
        if (sInstance == null) {
            sInstance = new KitchenInspireSearchPolicy();
        }
        return sInstance;
    }

    /**
     * 设置数据变化监听
     *
     * @param listener
     */
    public void setOnRecipeChangedListener(OnRecipeChangedListener listener) {
        mRecipeChangedListener = listener;
    }

    /**
     * 调用启发式搜索
     *
     * @param ingredient
     * @param add
     */
    public void inspireSearch(final FoodIngredient ingredient, final boolean add) {
        if (!mHasPrepared || mSearchThreads.get() != 0) {
            return;
        }

        if (ingredient != null) {
            if (add) {
                mSelectedIngredients.add(ingredient);
            } else {
                mSelectedIngredients.remove(ingredient);
            }
            if (mRecipeChangedListener != null) {
                mRecipeChangedListener.onSelected(add, ingredient, mSelectedIngredients);
            }
        }

        mSelectedFilterKey = "";
        Collections.sort(mSelectedIngredients, new Comparator<FoodIngredient>() {
            @Override
            public int compare(FoodIngredient lhs, FoodIngredient rhs) {
                return lhs.getPinyin().compareTo(rhs.getPinyin());
            }
        });

        for (int i = 0; i < mSelectedIngredients.size(); i++) {
            mSelectedFilterKey += mSelectedIngredients.get(i).getName();
        }

        Logger.d(TAG, "selected filter key : " + mSelectedFilterKey);

        // 可做菜谱
        AsyncUIHandler.postParallel(new AsyncUIHandler.AsyncJob() {
            @Override
            public void run() {
                mSearchThreads.incrementAndGet();
                if (ingredient != null) {
                    mSelectedRecipeIds = "";

                    List<FoodRecipe> recipeList = mFoodRecipeCache.get(mSelectedFilterKey);

                    if (recipeList == null) {
                        Logger.d(TAG, "recipe cache miss!");
                        recipeList = getRelativeRecipeIdListByIngredientNames(mSelectedIngredients);
                        if (!StringUtils.isEmpty(mSelectedFilterKey)) {
                            mFoodRecipeCache.put(mSelectedFilterKey, recipeList);
                        }
                    } else {
                        Logger.d(TAG, "recipe cache hit!");
                    }

                    if (recipeList != null) {
                        for (int i = 0; i < recipeList.size(); i++) {
                            FoodRecipe recipe = recipeList.get(i);
                            if (i == recipeList.size() - 1) {
                                mSelectedRecipeIds += "" + recipe.getId();
                            } else {
                                mSelectedRecipeIds += "" + recipe.getId() + ",";
                            }
                        }
                    }

                    if (mRecipeChangedListener != null) {
                        mRecipeChangedListener.onRecipeResult(recipeList);
                    }
                }
                mSearchThreads.decrementAndGet();
            }

        });

        //备选食材
        AsyncUIHandler.postParallel(new AsyncUIHandler.AsyncJob() {
            @Override
            public void run() {
                mSearchThreads.incrementAndGet();
                if (ingredient != null) {

                    List<FoodIngredient> filterList = mFilterCache.get(mSelectedFilterKey);
                    if (filterList == null) {
                        Logger.d(TAG, "filter cache miss!");
                        filterList = getRelativeFilterIngredientListByIngredientNames(mSelectedIngredients);
                        if (!StringUtils.isEmpty(mSelectedFilterKey)) {
                            mFilterCache.put(mSelectedFilterKey, filterList);
                        }
                    } else {
                        Logger.d(TAG, "filter cache hit!");
                    }

                    if (mRecipeChangedListener != null) {
                        mRecipeChangedListener.onFiltered(filterList);
                    }
                }
                mSearchThreads.decrementAndGet();
            }

        });

    }

    /**
     * 通过关键字，获得名字相关的食材列表
     *
     * @param keyword
     * @param listener
     * @return
     */
    public void searchFoodIngredients(final String keyword, final OnSearchKeywordListener listener) {

        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            List<FoodIngredient> list = new ArrayList<FoodIngredient>();

            @Override
            public void run() {
                List<FoodIngredient> filterList = mFilterCache.get(mSelectedFilterKey);
                //获得备选库
                if (StringUtils.isEmpty(mSelectedFilterKey)) {
                    filterList = mAllIngredients;
                }

                if (filterList == null || filterList.isEmpty()) {
                    return;
                }

                if (StringUtils.isEmpty(keyword)) {
                    list.addAll(filterList);
                    return;
                }

                for (FoodIngredient ingredient : filterList) {
                    if (ingredient.getName().contains(keyword)
                            || ingredient.getPinyin().contains(keyword.toUpperCase())) {
                        list.add(ingredient);
                    }
                }
            }

            @Override
            public void postUi() {
                super.postUi();
                if (listener != null) {
                    listener.onResult(keyword, list);
                }
            }
        });

    }


    /**
     * 获得全部食材数据
     *
     * @return
     */
    private List<FoodIngredient> obtainAllIngredients() {
        if (mHasPrepared && !mAllIngredients.isEmpty()) {
            return mAllIngredients;
        }

        Cursor cursor = null;
        try {
            SQLiteDatabase db = mInspireSearchDataLibrary.getReadableDatabase();
            String sql = "SELECT distinct " + RecipeIngredientTable.NAME
                    + "," + RecipeIngredientTable.NAME_PINYIN
                    + "," + RecipeIngredientTable.TYPE
                    + "," + RecipeIngredientTable.URL
                    + " FROM " + RecipeIngredientTable.TABLE_NAME;
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    FoodIngredient foodIngredient = new FoodIngredient();
                    foodIngredient.setPinyin(cursor.getString(cursor.getColumnIndex(RecipeIngredientTable.NAME_PINYIN)));
                    foodIngredient.setName(cursor.getString(cursor.getColumnIndex(RecipeIngredientTable.NAME)));
                    foodIngredient.setImage(cursor.getString(cursor.getColumnIndex(RecipeIngredientTable.URL)));
                    foodIngredient.setCategory(cursor.getString(cursor.getColumnIndex(RecipeIngredientTable.TYPE)));
                    mAllIngredients.add(foodIngredient);
                }
            }
            mHasPrepared = true;
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mAllIngredients;
    }

    /**
     * 通过已选择的食材名字，获得可进行组合的备选食材列表
     *
     * @param name
     * @return
     */
    private List<FoodIngredient> getRelativeFilterIngredientListByIngredientNames(List<FoodIngredient> ingredients) {
        List<FoodIngredient> list = new ArrayList<FoodIngredient>();
        if (ingredients != null) {
            if (ingredients.isEmpty()) {
                return mAllIngredients;
            }
            int index = 0;
            String selectedNames = "";
            for (FoodIngredient ingredient : ingredients) {
                if (index == ingredients.size() - 1) {
                    selectedNames += "\"" + ingredient.getName() + "\"";
                } else {
                    selectedNames += "\"" + ingredient.getName() + "\",";
                }
                index++;
            }

            String resultSql = "select distinct " + RecipeIngredientTable.NAME
                    + "," + RecipeIngredientTable.NAME_PINYIN
                    + "," + RecipeIngredientTable.TYPE
                    + "," + RecipeIngredientTable.URL
                    + " from " + RecipeIngredientTable.TABLE_NAME
                    + " where " + RecipeIngredientTable.RECIPE_ID
                    + " in ( " + mSelectedRecipeIds
                    + " ) group by " + RecipeIngredientTable.NAME
                    + " having " + RecipeIngredientTable.NAME
                    + " not in ( " + selectedNames + " )";
            Logger.d(getClass(), "sql : " + resultSql);
            Cursor cursor = null;
            try {
                SQLiteDatabase db = mInspireSearchDataLibrary.getReadableDatabase();
                cursor = db.rawQuery(resultSql, null);
                while (cursor.moveToNext()) {
                    FoodIngredient foodIngredient = new FoodIngredient();
                    foodIngredient.setPinyin(cursor.getString(cursor.getColumnIndex(RecipeIngredientTable.NAME_PINYIN)));
                    foodIngredient.setName(cursor.getString(cursor.getColumnIndex(RecipeIngredientTable.NAME)));
                    foodIngredient.setCategory(cursor.getString(cursor.getColumnIndex(RecipeIngredientTable.TYPE)));
                    foodIngredient.setImage(cursor.getString(cursor.getColumnIndex(RecipeIngredientTable.URL)));
                    list.add(foodIngredient);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return list;
    }

    /**
     * 通过已选择的食材名字，获得食材组合结果的菜谱id列表
     *
     * @param ingredients
     * @return
     */
    private List<FoodRecipe> getRelativeRecipeIdListByIngredientNames(List<FoodIngredient> ingredients) {
        List<FoodRecipe> list = new ArrayList<FoodRecipe>();
        if (ingredients != null && !ingredients.isEmpty()) {
            String sql = "";
            int index = 0;
            for (FoodIngredient ingredient : ingredients) {
                if (index == ingredients.size() - 1) {
                    sql += "select " + RecipeIngredientTable.RECIPE_ID
                            + " from " + RecipeIngredientTable.TABLE_NAME
                            + " where " + RecipeIngredientTable.NAME
                            + " = \"" + ingredient.getName() + "\"";
                } else {
                    sql += "select " + RecipeIngredientTable.RECIPE_ID
                            + " from " + RecipeIngredientTable.TABLE_NAME
                            + " where " + RecipeIngredientTable.NAME
                            + " = \"" + ingredient.getName() + "\""
                            + " intersect ";
                }

                index++;
            }
            Logger.d(getClass(), "sql : " + sql + " limit 0, 1000");
            Cursor cursor = null;
            try {
                SQLiteDatabase db = mInspireSearchDataLibrary.getReadableDatabase();
                cursor = db.rawQuery(sql + " limit 0, 1000", null);
                while (cursor.moveToNext()) {
                    FoodRecipe recipe = new FoodRecipe();
                    recipe.setId(cursor.getString(cursor.getColumnIndex(RecipeIngredientTable.RECIPE_ID)));
                    list.add(recipe);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return list;
    }

    /**
     * 准备
     */
    public void prepare(final Context context, final OnPrepareDataListener listener) {

        if (mHasPrepared) {
            return;
        }

        mSelectedIngredients.clear();
        mSelectedFilterKey = "";

        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {

            @Override
            public void run() {
                //检查sdcard副本版本：如果sdcard没有，则重新写入到/data/data
                if (checkNewversion()) {
                    //删除./data/data/数据库
                    FileUtils.deleteFile(context.getDatabasePath(DB_FILE_NAME));
                }

                //检查/data/data/目录下有无数据库
                if (!checkDbExisted(context)) {
                    //检查/sdcard/目录下有无数据库
                    File zip = new File(FileMaster.getInstance().getLongCacheDir(), DB_ZIP_FILE_NAME);
                    File unzip = context.getDatabasePath(DB_FILE_NAME).getParentFile();
                    if (!checkDbInSdCard()) {
                        //复制并解压到sdcard
                        InputStream is = null;
                        try {
                            is = context.getAssets().open(DB_ZIP_FILE_NAME);
                            FileUtils.writeStreamToFile(is, zip);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    //解压到/data/data/package/databases
                    FileUtils.unzip(zip, unzip);
                    //打开数据库
                    mInspireSearchDataLibrary.open();
                }

                obtainAllIngredients();
            }

            @Override
            public void postUi() {
                super.postUi();
                if (listener != null) {
                    listener.onPrepared(mHasPrepared, mAllIngredients);
                }
            }
        });
    }

    private boolean checkNewversion() {
        File dir = FileMaster.getInstance().getLongCacheDir();
        if (dir != null && dir.list() != null && dir.list().length > 0) {
            for (String file : dir.list()) {
                if (file.startsWith(DB_FILE_NAME)) {
                    int start = file.indexOf("_") + 1;
                    int end = file.lastIndexOf(".");
                    if (start == 0 && start >= end) {
                        //原始测试版本
                        FileUtils.deleteFile(new File(dir, file));
                        return true;
                    }
                    String version = file.substring(start, end);
                    try {
                        int versionCode = StringUtils.parseInt(version);
                        boolean newversion = (versionCode != InspireSearchDataLibrary.DATABASE_VERSION);
                        if (newversion) {
                            FileUtils.deleteFile(new File(dir, file));
                        }
                        return newversion;
                    } catch (Throwable e) {
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查再存储卡上是否有数据库
     *
     * @return
     */
    private boolean checkDbInSdCard() {
        return FileUtils.isExist(new File(FileMaster.getInstance().getLongCacheDir(), DB_ZIP_FILE_NAME));
    }

    /**
     * 检查数据文件是否存在
     *
     * @param context
     * @return
     */
    private boolean checkDbExisted(Context context) {
        return FileUtils.isExist(context.getDatabasePath(DB_FILE_NAME));
    }

    /**
     * 释放数据
     */
    public void release() {
        mHasPrepared = false;
        mAllIngredients.clear();
        mSelectedIngredients.clear();
        mSelectedFilterKey = "";
        mFilterCache.evictAll();
        mFoodRecipeCache.evictAll();
    }

    public String getSelectedIds() {
        return mSelectedRecipeIds;
    }

    public List<FoodIngredient> getSelectedIngredients() {
        return mSelectedIngredients;
    }

    public List<FoodIngredient> getAllIngredients() {
        return mAllIngredients;
    }

    public List<FoodIngredient> getFilteredIngredients() {
        return mFilterCache.get(mSelectedFilterKey);
    }

    public boolean isPrepared() {
        return mHasPrepared;
    }

    /**
     * 关键词搜索结果
     */
    public interface OnSearchKeywordListener {
        void onResult(String key, List<FoodIngredient> foodIngredients);
    }

    /**
     * 数据准备监听
     */
    public interface OnPrepareDataListener {

        /**
         * 是否数据准备完备
         *
         * @param result
         * @param all
         */
        void onPrepared(boolean result, List<FoodIngredient> all);
    }

    /**
     * 数据变化
     */
    public interface OnRecipeChangedListener {
        /**
         * 可做菜谱
         *
         * @param isAdd
         * @param changedIngredient
         * @param addedList
         * @param recipeList        可组合食材对应生成的菜谱列表
         */
        void onRecipeResult(List<FoodRecipe> recipeList);

        /**
         * 已选择
         *
         * @param isAdd             当前操作动作
         * @param changedIngredient 当前操作食材
         * @param addedList         已选中的食材列表
         */
        void onSelected(boolean isAdd, FoodIngredient changedIngredient, List<FoodIngredient> addedList);

        /**
         * 备选食材
         *
         * @param filterList 通过选中食材得到的可组合食材列表
         */
        void onFiltered(List<FoodIngredient> filterList);
    }
}
