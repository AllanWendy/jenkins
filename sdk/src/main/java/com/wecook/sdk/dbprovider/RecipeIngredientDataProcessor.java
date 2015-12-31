package com.wecook.sdk.dbprovider;

import android.content.Context;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodIngredient;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.sdk.dbprovider.tables.RecipeIngredientTable;
import com.wecook.sdk.policy.KitchenInspireSearchPolicy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * 菜谱食材数据装载器
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/11/14
 */
public class RecipeIngredientDataProcessor {

    /**
     * 菜谱--食材映射表
     */
    private Map<FoodRecipe, Set<FoodIngredient>> mAllRecipeIngredientMap;

    /**
     * 食材--菜谱映射表
     */
    private Map<FoodIngredient, Set<FoodRecipe>> mAllIngredientRecipeMap;

    /**
     * 食材库
     */
    private Set<FoodIngredient> mAllIngredients;

    /**
     * 菜谱库
     */
    private Set<FoodRecipe> mAllRecipes;

    /**
     * 食材名称拼音库
     */
    private Map<String, FoodIngredient> mPinyinNameListOfIngredients;

    private Map<String, String[]> mIngredientInfos;

    private RecipeIngredientTable mRecipeIngredientTable;

    public RecipeIngredientDataProcessor(RecipeIngredientTable table) {
        mRecipeIngredientTable = table;
        mAllRecipes = new HashSet<FoodRecipe>();
        mAllIngredients = new HashSet<FoodIngredient>();
        mPinyinNameListOfIngredients = new Hashtable<String, FoodIngredient>();
        mAllIngredientRecipeMap = new Hashtable<FoodIngredient, Set<FoodRecipe>>();
        mAllRecipeIngredientMap = new Hashtable<FoodRecipe, Set<FoodIngredient>>();
        mIngredientInfos = new Hashtable<String, String[]>();
    }


    public void writeToDatabase(final Context context, final String jsonFile, final KitchenInspireSearchPolicy.OnPrepareDataListener listener) {
        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            boolean result = false;

            @Override
            public void postUi() {
                super.postUi();
                if (listener != null) {
                    listener.onPrepared(result, null);
                }
            }

            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                Logger.d("inspire-search", "start..");
                InputStream is = null;
                InputStreamReader reader = null;
                try {
                    is = context.getAssets().open(jsonFile);
                    reader = new InputStreamReader(is);
                    JsonReader jsonReader = new JsonReader(reader);
                    jsonReader.setLenient(false);

                    readData(context, null, jsonReader);

                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }
        });
    }

    public void writeToDatabase(final Context context, final String recipeIngredientRelativeJson, final String ingredientInfoJson) {
        long startTime = System.currentTimeMillis();
        Logger.d("inspire-search", "start..");
        InputStream is = null;
        InputStreamReader reader = null;

        //读取食材信息
        try {
            is = context.getAssets().open(ingredientInfoJson);
            reader = new InputStreamReader(is);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(false);

            readIngredientData(context, jsonReader);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //读取食材－菜谱对应关系
        try {
            is = context.getAssets().open(recipeIngredientRelativeJson);
            reader = new InputStreamReader(is);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(false);

            readData(context, null, jsonReader);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private void readIngredientData(Context context, JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.peek();
        switch (token) {
            case BEGIN_OBJECT:
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    addIngredientInfo(context, jsonReader.nextName(), jsonReader);
                }
                jsonReader.endObject();
                break;

        }
    }

    private void addIngredientInfo(Context context, String ingredient, JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String[] infos = new String[2];
        while (jsonReader.hasNext()) {
            String key = jsonReader.nextName();
            String value = jsonReader.nextString();
            if ("url".equals(key)) {
                infos[0] = value;
            } else if ("type".equals(key)) {
                infos[1] = value;
            }
            Logger.d("key:" + key + " value:" + value);
        }
        jsonReader.endObject();
        mIngredientInfos.put(ingredient, infos);
    }

    private void readData(Context context, FoodRecipe recipe, JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.peek();
        switch (token) {
            case BEGIN_OBJECT:
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    addRecipeGroup(context, jsonReader.nextName(), jsonReader);
                }
                jsonReader.endObject();
                break;
            case BEGIN_ARRAY:
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    addIngredients(context, recipe, jsonReader.nextString());
                }
                jsonReader.endArray();
                break;
        }
    }

    private void addIngredients(Context context, FoodRecipe recipe, String ingredientName) {
        Logger.d("inspire-search", "ingredientName:" + ingredientName);
        FoodIngredient foodIngredient = new FoodIngredient();
        foodIngredient.setName(ingredientName);
        String pinyin = StringUtils.getPinyinString(context, foodIngredient.getName());
        foodIngredient.setPinyin(pinyin);
        Set<FoodIngredient> relativeIngredients = mAllRecipeIngredientMap.get(recipe);
        relativeIngredients.add(foodIngredient);
        //将所有数据加入食材列表进行排重过滤
        mAllIngredients.add(foodIngredient);
        //将食材的拼音数据加入列表
        mPinyinNameListOfIngredients.put(pinyin, foodIngredient);

        //建立食材和菜谱的对应关系表
        Set<FoodRecipe> foodRecipes = mAllIngredientRecipeMap.get(foodIngredient);
        if (foodRecipes == null) {
            foodRecipes = new HashSet<FoodRecipe>();
            mAllIngredientRecipeMap.put(foodIngredient, foodRecipes);
        }
        foodRecipes.add(recipe);

        RecipeIngredientTable.RecipeIngredientDB recipeIngredientDB = new RecipeIngredientTable.RecipeIngredientDB();
        recipeIngredientDB.recipeId = recipe.getId();
        recipeIngredientDB.ingredientName = ingredientName;
        recipeIngredientDB.ingredientNamePinYin = pinyin;
        String[] info = mIngredientInfos.get(ingredientName);
        if (info != null) {
            recipeIngredientDB.ingredientUrl = info[0];
            recipeIngredientDB.ingredientType = info[1];
        }
        mRecipeIngredientTable.insert(recipeIngredientDB);
    }

    private void addRecipeGroup(Context context, String recipeId, JsonReader jsonReader) throws IOException {
        Logger.d("inspire-search", "id:" + recipeId);
        FoodRecipe recipe = new FoodRecipe();
        recipe.setId(recipeId);
        Set<FoodIngredient> relativeIngredients = new HashSet<FoodIngredient>();
        mAllRecipeIngredientMap.put(recipe, relativeIngredients);
        mAllRecipes.add(recipe);
        readData(context, recipe, jsonReader);
    }
}
