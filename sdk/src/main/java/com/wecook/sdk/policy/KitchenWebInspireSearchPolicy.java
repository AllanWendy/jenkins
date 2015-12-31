package com.wecook.sdk.policy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.InspireSearchApi;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.FoodIngredient;
import com.wecook.uikit.alarm.ToastAlarm;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 网络版引导搜索
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/4/10
 */
public class KitchenWebInspireSearchPolicy {

    public static final int MAX_PAGE_SIZE = 30;

    private boolean mLoading;
    private boolean mCurrentAction;
    private List<FoodIngredient> mSelectedIngredients = new ArrayList<FoodIngredient>();
    private String mCurrentCategory = "";
    private FoodIngredient mCurrentIngredient;
    private List<String> mReportMissIngredients = new ArrayList<String>();

    private OnInspireSearchIngredientChangedListener ingredientChangedListener;

    private static KitchenWebInspireSearchPolicy sPolicy;
    private boolean mSelectItemChanged;

    public static KitchenWebInspireSearchPolicy get() {
        if (sPolicy == null) {
            sPolicy = new KitchenWebInspireSearchPolicy();
        }

        return sPolicy;
    }

    public List<FoodIngredient> getSelectedIngredients() {
        return mSelectedIngredients;
    }

    /**
     * 状态加载中
     *
     * @return
     */
    public boolean isLoading() {
        return mLoading;
    }

    public String getCurrentCategory() {
        return mCurrentCategory;
    }

    /**
     * 执行引导搜索
     *
     * @param ingredient
     */
    public void inspireSearch(FoodIngredient ingredient, boolean add) {
        mCurrentAction = add;
        if (add) {
            if (!mSelectedIngredients.contains(ingredient)) {
                mSelectedIngredients.add(ingredient);
                mCurrentIngredient = ingredient;
                doInspireSearch(true);
            }
        } else {
            if (mSelectedIngredients.remove(ingredient)) {
                mCurrentIngredient = ingredient;
                doInspireSearch(true);
            }
        }
    }


    /**
     * 更新分类
     *
     * @param category
     */
    public void selectCategory(String category) {

        if (!mCurrentCategory.equals(category)) {
            mCurrentCategory = category;
            doInspireSearch(false);
        }
    }

    /**
     * 准备数据
     *
     * @param listener
     */
    public void prepare(final OnInspireSearchPreparedListener listener) {
        if (listener != null) {
            listener.onStart();
        }
        InspireSearchApi.getCategoryList(new ApiCallback() {

            @Override
            public void onResult(ApiModel result) {
                AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
                    List<String> categoryList;

                    @Override
                    public void run() {
                        String json = getResponseString();
                        if (!StringUtils.isEmpty(json)) {
                            try {
                                JsonObject jsonObject = JsonUtils.getJsonObject(json);
                                if (jsonObject != null) {
                                    if (jsonObject.has("result")) {
                                        jsonObject = jsonObject.get("result").getAsJsonObject();
                                        if (jsonObject.has("ing_types")) {
                                            JsonArray array = jsonObject.get("ing_types").getAsJsonArray();
                                            Iterator<JsonElement> list = array.iterator();
                                            if (array.size() > 0) {
                                                categoryList = new ArrayList<String>();
                                                while (list.hasNext()) {
                                                    JsonElement element = list.next();
                                                    categoryList.add(element.getAsString());
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void postUi() {
                        super.postUi();
                        if (listener != null) {
                            listener.onPreparedCategories(categoryList);
                        }
                    }
                });
            }
        });

        final String ingredients = getSelectedKeys();

        InspireSearchApi.getIngredientList(ingredients, mCurrentCategory, 1, MAX_PAGE_SIZE, new ApiCallback() {
            @Override
            public void onResult(ApiModel result) {
                AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
                    InspireSearchIngredientsResult result;

                    @Override
                    public void run() {
                        String json = getResponseString();

                        if (!StringUtils.isEmpty(json)) {
                            result = new InspireSearchIngredientsResult(ingredients,
                                    mCurrentCategory, 1, MAX_PAGE_SIZE);
                            result.parseJson(json);
                        }
                    }

                    @Override
                    public void postUi() {
                        super.postUi();
                        if (listener != null) {
                            listener.onPreparedInspireSearch(result);
                        }
                    }
                });
            }
        });
    }

    /**
     * 搜索引导
     */
    public void doInspireSearch(boolean selectChange) {
        if (mLoading) {
            return;
        }
        mLoading = true;

        mSelectItemChanged = selectChange;
        obtainIngredientArray(1);
    }

    /**
     * 通过关键字搜索菜谱
     *
     * @param keyword
     * @param page
     * @param listener
     */
    public void searchFoodIngredients(final String keyword, final int page, final OnSearchKeywordListener listener) {
        searchFoodIngredients(keyword, page, MAX_PAGE_SIZE, listener);
    }

    /**
     * 通过关键字搜索菜谱
     *
     * @param keyword
     * @param page
     * @param pageSize
     * @param listener
     */
    public void searchFoodIngredients(final String keyword, final int page, final int pageSize, final OnSearchKeywordListener listener) {
        if (StringUtils.isEmpty(keyword)) {
            if (listener != null) {
                listener.onResult(keyword, null);
            }
            return;
        }
        if (listener != null) {
            listener.onStart();
        }
        final String ingredients = getSelectedKeys();

        InspireSearchApi.searchIngredientList(ingredients, keyword, page, pageSize, new ApiCallback() {
            @Override
            public void onResult(ApiModel result) {
                InspireSearchIngredientsResult ingredientsResult =
                        new InspireSearchIngredientsResult(ingredients, mCurrentCategory, page, MAX_PAGE_SIZE);
                ingredientsResult.parseJson(getResponseString());

                if (listener != null) {
                    listener.onResult(keyword, ingredientsResult.result);
                }
            }
        });
    }

    /**
     * 通过选中食材和分类获得食材列表
     *
     * @param page
     * @param page
     * @return
     */
    public void obtainIngredientArray(final int page) {
        if (ingredientChangedListener != null) {
            ingredientChangedListener.onStart();
        }

        final String ingredients = getSelectedKeys();

        InspireSearchApi.getIngredientList(ingredients, mCurrentCategory, page, MAX_PAGE_SIZE, new ApiCallback() {
            @Override
            public void onResult(ApiModel result) {
                mLoading = false;
                InspireSearchIngredientsResult ingredientsResult =
                        new InspireSearchIngredientsResult(ingredients, mCurrentCategory, page, MAX_PAGE_SIZE);
                ingredientsResult.parseJson(getResponseString());
                if (ingredientChangedListener != null) {
                    if (mSelectItemChanged) {
                        ingredientChangedListener.onSelectChange(mCurrentAction, mCurrentIngredient,
                                mSelectedIngredients);
                    }
                    ingredientChangedListener.onResult(ingredientsResult);
                }
            }
        });
    }

    /**
     * 通过选中食材和分类获得食材列表
     *
     * @param page
     * @param pageSize
     * @param listener
     */
    public void obtainIngredientArray(final int page, final int pageSize, final OnInspireSearchIngredientChangedListener listener) {
        if (listener != null) {
            listener.onStart();
        }

        final String ingredients = getSelectedKeys();

        InspireSearchApi.getIngredientList(ingredients, mCurrentCategory, page, pageSize, new ApiCallback() {
            @Override
            public void onResult(ApiModel result) {
                mLoading = false;
                InspireSearchIngredientsResult ingredientsResult =
                        new InspireSearchIngredientsResult(ingredients, mCurrentCategory, page, MAX_PAGE_SIZE);
                ingredientsResult.parseJson(getResponseString());
                if (listener != null) {
                    listener.onResult(ingredientsResult);
                }
            }
        });
    }

    /**
     * 获得菜谱列表IDS
     *
     * @param page
     * @param pageSize
     * @param callback
     */
    public void obtainRecipeIds(int page, int pageSize, final OnInspireSearchRecipeIdsListener callback) {
        InspireSearchApi.getRecipeListIds(getSelectedKeys(), page, pageSize, new ApiCallback() {
            @Override
            public void onResult(ApiModel result) {
                if (callback != null) {
                    callback.onStart();
                }
                AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {

                    List<String> list;

                    @Override
                    public void run() {
                        String json = getResponseString();
                        if (!StringUtils.isEmpty(json)) {
                            try {
                                JsonObject object = JsonUtils.getJsonObject(json);
                                if(object.has("result")){
                                    JsonObject resultObject = object.get("result").getAsJsonObject();

                                    if(resultObject.has("recipe")){
                                        JsonArray idArray = resultObject.get("recipe").getAsJsonArray();
                                        Iterator<JsonElement> iterator = idArray.iterator();
                                        list = new ArrayList<String>();
                                        while (iterator.hasNext()) {
                                            JsonElement value = iterator.next();
                                            list.add(value.getAsString());
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void postUi() {
                        super.postUi();
                        if (callback != null) {
                            callback.onResult(list);
                        }
                    }
                });

            }
        });
    }

    /**
     * 通过选中食材获得菜谱列表
     *
     * @param listener
     * @param page
     * @param pageSize
     * @return
     */
    public void obtainRecipeArray(final int page, final int pageSize,
                                  final OnInspireSearchRecipeChangedListener listener) {
        if (listener != null) {
            listener.onStart();
        }

        final String ingredients = getSelectedKeys();

        InspireSearchApi.getRecipeList(ingredients, page, pageSize, new ApiCallback() {

            @Override
            public void onResult(ApiModel result) {

                InspireSearchRecipesResult recipesResult
                        = new InspireSearchRecipesResult(ingredients, page, pageSize);
                recipesResult.parseJson(getResponseString());

                if (listener != null) {
                    listener.onResult(recipesResult);
                }
            }
        });
    }

    /**
     * 报缺失食材
     *
     * @param ingredientName
     */
    public void reportMissIngredient(String ingredientName) {
        if (!mReportMissIngredients.contains(ingredientName)) {
            mReportMissIngredients.add(ingredientName);
            InspireSearchApi.reportMissIngredient(ingredientName, new ApiCallback() {

                @Override
                public void onResult(ApiModel result) {
                    if (getStatusState() == ApiModel.STATE_OK) {
                        ToastAlarm.show("已上报成功!");
                    }
                }
            });
        } else {
            ToastAlarm.show("你已上报了该食材!");
        }
    }


    public void setIngredientChangedListener(OnInspireSearchIngredientChangedListener ingredientChangedListener) {
        this.ingredientChangedListener = ingredientChangedListener;
    }


    public void release() {
        ingredientChangedListener = null;
        mCurrentCategory = "";
        mCurrentIngredient = null;
        mSelectedIngredients.clear();
        mLoading = false;
    }

    public String getSelectedKeys() {
        String ingredients = "";
        Collections.sort(mSelectedIngredients, new Comparator<FoodIngredient>() {
            @Override
            public int compare(FoodIngredient lhs, FoodIngredient rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        int index = 0;
        for (FoodIngredient ingredient : mSelectedIngredients) {
            index ++;
            if (index == mSelectedIngredients.size()) {
                ingredients += ingredient.getName();
            } else {
                ingredients += ingredient.getName() + ",";
            }
        }
        return ingredients;
    }

    /**
     * 关键词搜索结果
     */
    public interface OnSearchKeywordListener {
        void onStart();

        void onResult(String key, List<FoodIngredient> foodIngredients);
    }


    /**
     * 食材数据结果监听
     */
    public interface OnInspireSearchIngredientChangedListener {
        void onStart();

        /**
         * 返回结果
         *
         * @param result
         */
        void onResult(InspireSearchIngredientsResult result);

        void onSelectChange(boolean action, FoodIngredient ingredient,
                            List<FoodIngredient> selected);
    }

    /**
     * 菜谱数据结果监听
     */
    public interface OnInspireSearchRecipeChangedListener {
        void onStart();

        /**
         * 返回结果
         *
         * @param result
         */
        void onResult(InspireSearchRecipesResult result);
    }

    public interface OnInspireSearchRecipeIdsListener {
        void onStart();

        /**
         * 返回结果
         *
         * @param result
         */
        void onResult(List<String> result);
    }

    public interface OnInspireSearchPreparedListener {
        void onStart();

        void onPreparedCategories(List<String> categories);

        void onPreparedInspireSearch(InspireSearchIngredientsResult result);
    }

    /**
     * 引导食材组合
     */
    public static class InspireSearchIngredientsResult {
        //IN
        public String ingredientNames;
        public String category;
        public int page;
        public int pageSize;

        //OUT
        /**
         * 食材总数
         */
        public int ingredientCount;
        /**
         * 可做菜谱总数
         */
        public int recipeCount;
        /**
         * 食材信息列表
         */
        public List<FoodIngredient> result;

        public InspireSearchIngredientsResult(String ingredientNames, String category, int page, int pageSize) {
            this.ingredientNames = ingredientNames;
            this.category = category;
            this.page = page;
            this.pageSize = pageSize;
        }

        /**
         * @param json
         */
        public void parseJson(String json) {

            if (!StringUtils.isEmpty(json)) {
                try {
                    JsonObject jsonObject = JsonUtils.getJsonObject(json);
                    if (jsonObject != null) {
                        if (jsonObject.has("result")) {
                            jsonObject = jsonObject.get("result").getAsJsonObject();
                            if (jsonObject.has("icount")) {
                                ingredientCount = jsonObject.get("icount").getAsInt();
                            }

                            if (jsonObject.has("rcount")) {
                                recipeCount = jsonObject.get("rcount").getAsInt();
                            }

                            if (jsonObject.has("ing")) {
                                JsonArray array = jsonObject.get("ing").getAsJsonArray();
                                Iterator<JsonElement> list = array.iterator();
                                if (array.size() > 0) {
                                    result = new ArrayList<FoodIngredient>();
                                    while (list.hasNext()) {
                                        JsonElement element = list.next();
                                        JsonObject object = element.getAsJsonObject();
                                        FoodIngredient ingredient = new FoodIngredient();
                                        ingredient.setType(object.get("type").getAsString());
                                        ingredient.setName(object.get("name").getAsString());
                                        ingredient.setImage(object.get("img").getAsString());
                                        result.add(ingredient);
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 引导菜谱组合
     */
    public static class InspireSearchRecipesResult {
        //IN
        public String ingredientNames;
        public int page;
        public int pageSize;

        //OUT
        /**
         * 菜谱总数
         */
        public int recipeCount;
        /**
         * 菜谱信息列表
         */
        public List<Food> result;
        /**
         * 食材别称列表
         */
        public List<String> aliasIngredients;

        public InspireSearchRecipesResult(String ingredientNames, int page, int pageSize) {
            this.ingredientNames = ingredientNames;
            this.page = page;
            this.pageSize = pageSize;
        }

        public void parseJson(String json) {
            if (!StringUtils.isEmpty(json)) {
                try {
                    JsonObject jsonObject = JsonUtils.getJsonObject(json);
                    if (jsonObject.has("result")) {
                        JsonObject resultObject = jsonObject.get("result").getAsJsonObject();

                        if (resultObject.has("rcount")) {
                            recipeCount = resultObject.get("rcount").getAsInt();
                        }

                        if (resultObject.has("sim_ing")) {
                            JsonArray aliasIngredientArray = resultObject.get("sim_ing").getAsJsonArray();
                            Iterator<JsonElement> aliasIngredientIterator = aliasIngredientArray.iterator();
                            if (aliasIngredientArray.size() > 0) {
                                aliasIngredients = new ArrayList<String>();
                                while (aliasIngredientIterator.hasNext()) {
                                    JsonElement element = aliasIngredientIterator.next();
                                    aliasIngredients.add(element.getAsString());
                                }
                            }
                        }

                        if (resultObject.has("recipe")) {
                            JsonArray recipeArray = resultObject.get("recipe").getAsJsonArray();
                            Iterator<JsonElement> recipeIterator = recipeArray.iterator();
                            if (recipeArray.size() > 0) {
                                result = new ArrayList<Food>();
                                while (recipeIterator.hasNext()) {
                                    JsonElement element = recipeIterator.next();
                                    JsonObject recipeObject = element.getAsJsonObject();
                                    Food recipe = new Food();

                                    recipe.setId(recipeObject.get("rid").getAsString());
                                    recipe.setTitle(recipeObject.get("title").getAsString());
                                    recipe.setTag(recipeObject.get("tags").getAsString());
                                    recipe.setImage(recipeObject.get("img").getAsString());
                                    result.add(recipe);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
