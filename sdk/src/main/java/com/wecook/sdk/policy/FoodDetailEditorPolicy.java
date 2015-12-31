package com.wecook.sdk.policy;

import com.google.gson.stream.JsonWriter;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.database.DataLibraryManager;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.uper.UpperManager;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.FoodApi;
import com.wecook.sdk.api.model.FoodAssist;
import com.wecook.sdk.api.model.FoodIngredient;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.sdk.api.model.FoodStep;
import com.wecook.sdk.api.model.ID;
import com.wecook.sdk.api.model.Media;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.dbprovider.AppDataLibrary;
import com.wecook.sdk.dbprovider.tables.RecipeTable;
import com.wecook.sdk.userinfo.UserProperties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * 编辑菜谱详情策略
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/22/15
 */
public class FoodDetailEditorPolicy {

    public static final int STATE_PUBLISH_CHECK_SUCCESS = 0;
    public static final int STATE_PUBLISH_CHECK_ERROR_EMPTY_TITLE = 1;
    public static final int STATE_PUBLISH_CHECK_ERROR_EMPTY_INGREDIENTS = 2;
    public static final int STATE_PUBLISH_CHECK_ERROR_EMPTY_STEPS = 3;
    public static final int STATE_PUBLISH_CHECK_ERROR_INVALID_STEP = 5;
    public static final int STATE_PUBLISH_CHECK_ERROR_INVALID_INGREDIENT = 6;
    public static final int STATE_PUBLISH_CHECK_ERROR_DESC_TIPS_TOO_LONG = 7;
    public static final int STATE_PUBLISH_CHECK_ERROR_EMPTY_COVER = 8;

    private static final String TAG = "foodedit";
    private static final int MAX_SYNC_COUNT = 2;
    public static final int MAX_STEP_DESC_LENGTH = 100;
    public static final int MAX_RECIPE_DESC_AND_TIPS_LENGTH = 120;

    private static FoodDetailEditorPolicy sPolicy;

    private UpperManager.UpperListener mListener;
    private boolean mIsCancelUpload;
    private FoodRecipe mFoodRecipe;
    private List<FoodStep> mFoodSteps;
    private List<FoodIngredient> mFoodIngredients;
    private Map<String, Object> mImageMap;
    private int mSyncLoopCount;

    private RecipeTable.RecipeDB mBeforeEditData;
    private boolean mIsNeedToChange;
    private OnEditorChangedListener mOnEditorChanged;

    private FoodDetailEditorPolicy() {
    }

    public static FoodDetailEditorPolicy get() {
        if (sPolicy == null) {
            sPolicy = new FoodDetailEditorPolicy();
        }
        return sPolicy;
    }

    /**
     * 创建
     */
    public void onCreateFoodRecipe() {
        onCreateFoodRecipe(new FoodRecipe());
    }

    /**
     * 指定对象创建
     *
     * @param recipe
     */
    public void onCreateFoodRecipe(FoodRecipe recipe) {
        if (recipe == null) {
            recipe = new FoodRecipe();
        } else {
            final FoodRecipe finalRecipe = recipe;
            AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
                @Override
                public void run() {
                    mBeforeEditData = FoodRecipeDBHelper.createRecipeDB(finalRecipe, false);
                }
            });

        }

        mFoodRecipe = recipe;

        if (mFoodRecipe.getAuthor() == null) {
            mFoodRecipe.setAuthor(UserProperties.getUser());
        }

        if (mFoodRecipe.getStepList() == null) {
            mFoodRecipe.setStepList(new ApiModelList<FoodStep>(new FoodStep()));
        }
        mFoodSteps = mFoodRecipe.getStepList().getList();

        if (mFoodRecipe.getIngredientsList() == null) {
            mFoodRecipe.setIngredientsList(new ApiModelList<FoodIngredient>(new FoodIngredient()));
        }
        mFoodIngredients = mFoodRecipe.getIngredientsList().getList();

        mImageMap = new HashMap<String, Object>();

        mIsCancelUpload = false;

        mListener = new UpperManager.SimpleUpperListener() {

            @Override
            public void onEnd(String path, String id, String url, boolean success) {
                if (!success) {
                    return;
                }
                Object object = mImageMap.remove(path);
                if (object != null) {
                    if (object instanceof FoodStep) {
                        Media media = new Media();
                        media.setId(id);
                        media.setImage(url);
                        ((FoodStep) object).setMedia(media);
                    } else if (object instanceof FoodRecipe) {
                        ((FoodRecipe) object).setImage(url);
                        ((FoodRecipe) object).setImageId(id);
                    }
                }
            }

            @Override
            public void onFinish(int success, int total) {
                super.onFinish(success, total);
                UpperManager.asInstance().removeUpperListener(this);
            }

            @Override
            public void onCancel(String path) {
                super.onCancel(path);
                mImageMap.remove(path);
            }
        };

    }

    public void onRelease() {
        mListener = null;
        mFoodRecipe = null;
        mBeforeEditData = null;
        if (mFoodIngredients != null) {
            mFoodIngredients.clear();
            mFoodIngredients = null;
        }

        if (mFoodSteps != null) {
            mFoodSteps.clear();
            mFoodSteps = null;
        }

        if (mImageMap != null) {
            mImageMap.clear();
            mImageMap = null;
        }

        mIsCancelUpload = true;
        mIsNeedToChange = false;
    }

    public String getTitle() {
        return mFoodRecipe.getTitle();
    }

    public void setTitle(String title) {
        if (!StringUtils.isEmpty(title)
              && !getTitle().equals(title)) {
            needToChange();
        }
        mFoodRecipe.setTitle(title);
    }

    public String getLocalImage() {
        return mFoodRecipe.getLocalImage();
    }

    public void setLocalImage(String localImage) {
        if (!StringUtils.isEmpty(getLocalImage())
                && getLocalImage().equals(localImage)) {
            needToChange();
        }
        mFoodRecipe.setLocalImage(localImage);
    }

    public String getImage() {
        return mFoodRecipe.getImage();
    }

    public void setImage(String image) {
        if (!StringUtils.isEmpty(getImage())
                && getImage().equals(image)) {
            needToChange();
        }
        mFoodRecipe.setImage(image);
    }

    public String getDescription() {
        return mFoodRecipe.getDescription();
    }

    public void setDescription(String description) {
        if (!StringUtils.isEmpty(getDescription())
                && getDescription().equals(description)) {
            needToChange();
        }

        mFoodRecipe.setDescription(description);
    }

    public String getTips() {
        return mFoodRecipe.getTips();
    }

    public void setTips(String tips) {
        if (!StringUtils.isEmpty(getTips())
                && getTips().equals(tips)) {
            needToChange();
        }
        mFoodRecipe.setTips(tips);
    }

    public String getDifficulty() {
        int difficultyLevel = 0;
        try {
            difficultyLevel = StringUtils.parseInt(mFoodRecipe.getDifficulty());
        } catch (Exception e) {
        }
        if (difficultyLevel > 0 && difficultyLevel <= FoodRecipe.DIFFICULTY_LEVEL.length) {
            return FoodRecipe.DIFFICULTY_LEVEL[difficultyLevel - 1];
        }

        return "";
    }

    public void setDifficulty(String difficulty) {
        if (!StringUtils.isEmpty(getDifficulty())
                && getDifficulty().equals(difficulty)) {
            needToChange();
        }
        int index = 0;
        for (String difficultyTxt : FoodRecipe.DIFFICULTY_LEVEL) {
            index++;
            if (difficultyTxt.equals(difficulty)) {
                break;
            }
        }
        mFoodRecipe.setDifficulty(index + "");
    }

    public String getTime() {
        int timeLevel = 0;
        try {
            timeLevel = StringUtils.parseInt(mFoodRecipe.getTime());
        } catch (Exception e) {
        }
        if (timeLevel > 0 && timeLevel <= FoodRecipe.TIME_LEVEL.length) {
            return FoodRecipe.TIME_LEVEL[timeLevel - 1];
        }

        return "";
    }

    public void setTime(String time) {
        if (!StringUtils.isEmpty(getTime())
                && getTime().equals(time)) {
            needToChange();
        }
        int index = 0;
        for (String timeTxt : FoodRecipe.TIME_LEVEL) {
            index++;
            if (timeTxt.equals(time)) {
                break;
            }
        }

        mFoodRecipe.setTime(index + "");
    }


    public User getAuthor() {
        return mFoodRecipe.getAuthor();
    }

    public void setAuthor(User author) {
        mFoodRecipe.setAuthor(author);
    }

    public String getTags() {
        return mFoodRecipe.getTags();
    }

    public String[] getTagArray() {
        String tags = getTags();
        return tags.split(",");
    }

    public void setTags(String tags) {
        if (!StringUtils.isEmpty(getTags())
                && getTags().equals(tags)) {
            needToChange();
        }
        mFoodRecipe.setTags(tags);
    }

    public void setTags(List<String> tagList) {
        String tags = "";
        if (tagList != null && !tagList.isEmpty()) {
            int index = 0;
            for (String tag : tagList) {
                index++;
                if (index == tagList.size()) {
                    tags += tag;
                } else {
                    tags += tag + ",";
                }
            }
        }
        setTags(tags);
    }

    public List<FoodIngredient> getIngredientsList() {
        return mFoodIngredients;
    }

    public void setIngredientsList(ApiModelList<FoodIngredient> ingredientsList) {
        if (!ListUtils.equals(mFoodIngredients, ingredientsList.getList())) {
            needToChange();
        }

        mFoodRecipe.setIngredientsList(ingredientsList);
    }

    public List<FoodStep> getStepList() {
        return mFoodSteps;
    }

    public void setStepList(ApiModelList<FoodStep> stepList) {
        if (!ListUtils.equals(mFoodSteps, stepList.getList())) {
            needToChange();
        }

        mFoodRecipe.setStepList(stepList);
    }

    public FoodRecipe getFoodRecipe() {
        return mFoodRecipe;
    }

    /**
     * 获得预览数据对象
     *
     * @return
     */
    public FoodRecipe getReviewFoodRecipe() {
        FoodRecipe recipe = new FoodRecipe();
        recipe.setId(mFoodRecipe.getId());
        recipe.setModifyTime(mFoodRecipe.getModifyTime());
        recipe.setImage(mFoodRecipe.getImage());
        recipe.setCreateTime(mFoodRecipe.getCreateTime());
        recipe.setTitle(mFoodRecipe.getTitle());
        recipe.setTime(mFoodRecipe.getTime());
        recipe.setAuthor(mFoodRecipe.getAuthor());
        recipe.setDescription(mFoodRecipe.getDescription());
        recipe.setTags(mFoodRecipe.getTags());
        recipe.setTips(mFoodRecipe.getTips());
        recipe.setLocalImage(mFoodRecipe.getLocalImage());
        recipe.setDifficulty(mFoodRecipe.getDifficulty());
        recipe.setImageId(mFoodRecipe.getImageId());

        ApiModelList<FoodIngredient> foodIngredientApiModelList = new ApiModelList<FoodIngredient>(new FoodIngredient());
        foodIngredientApiModelList.addAll(mFoodIngredients);
        updateValidateIngredientList(foodIngredientApiModelList.getList());
        recipe.setIngredientsList(foodIngredientApiModelList);

        ApiModelList<FoodStep> stepList = new ApiModelList<FoodStep>(new FoodStep());
        stepList.addAll(mFoodSteps);
        recipe.setStepList(stepList);

        return recipe;
    }

    /**
     * 变换位置
     *
     * @param fromPosition
     * @param toPosition
     */
    public void onChangePosition(int fromPosition, int toPosition) {
        if (fromPosition != toPosition) {
            needToChange();
            FoodStep step = mFoodSteps.remove(fromPosition);
            mFoodSteps.add(toPosition, step);
        }
    }

    /**
     * 步骤总数
     *
     * @return
     */
    public int getStepCount() {
        return mFoodSteps.size();
    }

    /**
     * 删除步骤
     *
     * @param pos
     * @return
     */
    public FoodStep deleteStep(int pos) {
        FoodStep step = mFoodSteps.remove(pos);
        if (step != null) {
            needToChange();
        }
        return step;
    }

    /**
     * 添加步骤
     *
     * @param step
     */
    public void addStep(FoodStep step) {
        if (step != null) {
            needToChange();
        }
        mFoodSteps.add(step);
    }

    /**
     * 添加步骤
     *
     * @param newPos
     * @param step
     */
    public void addStep(int newPos, FoodStep step) {
        if (step != null) {
            needToChange();
        }
        if (newPos >= mFoodSteps.size()) {
            mFoodSteps.add(step);
        } else {
            mFoodSteps.add(newPos, step);
        }
    }


    /**
     * 添加食材
     *
     * @param ingredient
     */
    public void addIngredient(FoodIngredient ingredient) {
        if (ingredient != null) {
            needToChange();
        }
        mFoodIngredients.add(ingredient);
    }

    /**
     * 删除食材
     *
     * @param ingredient
     */
    public void deleteIngredient(FoodIngredient ingredient) {
        if (ingredient != null) {
            needToChange();
        }
        mFoodIngredients.remove(ingredient);
    }

    /**
     * 上传图片
     *
     * @param pathMap
     */
    public void uploadImages(Map<String, ? extends Object> pathMap) {
        for (Map.Entry<String, ? extends Object> entry : pathMap.entrySet()) {
            String path = entry.getKey();
            Object o = entry.getValue();
            mImageMap.put(path, o);
        }

        Set<String> strings = pathMap.keySet();
        String[] paths = new String[strings.size()];
        paths = strings.toArray(paths);
        UpperManager.asInstance().setUserId(UserProperties.getUserId());
        UpperManager.asInstance().addUpperListener(mListener);
        UpperManager.asInstance().upImages(paths);
        needToChange();
    }

    /**
     * 上传单一图片
     *
     * @param path
     * @param obj
     */
    public void uploadImage(String path, Object obj) {
        mImageMap.put(path, obj);
        UpperManager.asInstance().setUserId(UserProperties.getUserId());
        UpperManager.asInstance().addUpperListener(mListener);
        UpperManager.asInstance().upImage(path);
        needToChange();
    }

    /**
     * 取消上传
     *
     * @param path
     */
    public void cancelUploadImage(String path) {
        UpperManager.asInstance().cancelImage(path);
    }

    /**
     * 发布
     *
     * @return
     */
    public void publish(final PublishListener listener) {
        saveToDraft(true, new SaveListener() {
            @Override
            public void onSave(final boolean local, final boolean remote) {

                AsyncUIHandler.postParallel(new AsyncUIHandler.AsyncJob() {
                    boolean publishResult;

                    @Override
                    public void run() {
                        if (local && remote && checkPublishValidate() == STATE_PUBLISH_CHECK_SUCCESS) {
                            Logger.d(TAG, "publish recipe to remote : " + mFoodRecipe.getId());
                            if (!mIsCancelUpload) {
                                FoodApi.publishFood(mFoodRecipe.getId(), new ApiCallback<State>() {
                                    @Override
                                    public void onResult(final State result) {
                                        Logger.d(TAG, "publish recipe result : " + result.available());
                                        if (result.available()) {
                                            //删除草稿箱
                                            deleteFoodRecipeFromDB(mFoodRecipe);
                                            publishResult = true;
                                        }

                                        UIHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (listener != null) {
                                                    listener.onPublish(publishResult, result.getErrorMsg());
                                                }
                                            }
                                        });
                                    }
                                });
                            } else {
                                UIHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (listener != null) {
                                            listener.onPublish(false, "上传被取消");
                                        }
                                    }
                                });

                            }
                        } else {
                            UIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null) {
                                        listener.onPublish(false, "上传失败");
                                    }
                                }
                            });
                        }
                    }

                });


            }
        });
    }

    /**
     * 检查上传状态
     */
    private boolean checkUploadState() {
        //主菜谱图没传完
        if (StringUtils.isEmpty(mFoodRecipe.getImageId())) {
            mImageMap.put(mFoodRecipe.getLocalImage(), mFoodRecipe);
        }

        //步骤图存在没传完
        for (FoodStep step : mFoodSteps) {
            if (!step.isOnlyText() && !StringUtils.isEmpty(step.getLocalImage())) {
                if (hasUnuploadStepPic(step)) {
                    mImageMap.put(step.getLocalImage(), step);
                }
            }
        }

        return mImageMap.isEmpty();
    }

    private boolean hasUnuploadStepPic(FoodStep step) {
        if (step.getMedia() == null || StringUtils.isEmpty(step.getMedia().getId())) {
            return true;
        }
        return false;
    }

    /**
     * 同步上传进度
     */
    private void syncUploadProcess() {
        if (!checkUploadState()) {

            //存在上传失败的问题
            if (!mImageMap.isEmpty() && !mIsCancelUpload) {
                uploadImages(mImageMap);
                Logger.w(TAG, "sync upload process ......");
            }

            //同步上传结果
            SyncHandler.sync(new SyncHandler.Sync() {
                @Override
                public void syncStart() {

                }

                @Override
                public boolean waiting() {
                    return UpperManager.asInstance().isWorking();
                }

                @Override
                public Object syncEnd() {
                    return null;
                }
            });

            if (mSyncLoopCount >= MAX_SYNC_COUNT) {
                return;
            }
            mSyncLoopCount++;
            syncUploadProcess();
        }

    }



    /**
     * 检察数据
     *
     * @return
     */
    public int checkPublishValidate() {

        //菜谱名为空
        Logger.d(TAG, "check recipe name:" + mFoodRecipe.getTitle());
        if (StringUtils.isEmpty(mFoodRecipe.getTitle())) {
            return STATE_PUBLISH_CHECK_ERROR_EMPTY_TITLE;
        }

        if (StringUtils.isEmpty(mFoodRecipe.getLocalImage()) && StringUtils.isEmpty(mFoodRecipe.getImageId())) {
            return STATE_PUBLISH_CHECK_ERROR_EMPTY_COVER;
        }

        //菜谱描述或小提示
        if (StringUtils.chineseLength(mFoodRecipe.getDescription()) > MAX_RECIPE_DESC_AND_TIPS_LENGTH
                || StringUtils.chineseLength(mFoodRecipe.getTips()) > MAX_RECIPE_DESC_AND_TIPS_LENGTH) {
            return STATE_PUBLISH_CHECK_ERROR_DESC_TIPS_TOO_LONG;
        }

        Logger.d(TAG, "check recipe ingredients");
        //食材为空
        if (mFoodIngredients == null || mFoodIngredients.isEmpty()) {
            return STATE_PUBLISH_CHECK_ERROR_EMPTY_INGREDIENTS;
        }

        Logger.d(TAG, "check recipe steps");
        //步骤为空
        if (mFoodSteps == null || mFoodSteps.isEmpty()) {
            return STATE_PUBLISH_CHECK_ERROR_EMPTY_STEPS;
        }

        //存在无效食材
        if (hasInvalidateIngredientList()) {
            return STATE_PUBLISH_CHECK_ERROR_INVALID_INGREDIENT;
        }

        //存在无效步骤
        if (hasInvalidateStepList()) {
            return STATE_PUBLISH_CHECK_ERROR_INVALID_STEP;
        }

        Logger.d(TAG, "check recipe success");
        return STATE_PUBLISH_CHECK_SUCCESS;
    }

    public void updatePublishValidate(){
        syncUploadProcess();
        updateValidateStepList();
        updateValidateIngredientList();
        updateValidateTipsAndDesc();
    }

    private void updateValidateTipsAndDesc() {
        if (StringUtils.chineseLength(mFoodRecipe.getDescription()) > MAX_RECIPE_DESC_AND_TIPS_LENGTH) {
            final String desc = mFoodRecipe.getDescription();
            mFoodRecipe.setDescription(StringUtils.subChineseString(desc, 0, MAX_RECIPE_DESC_AND_TIPS_LENGTH));
        }

        if (StringUtils.chineseLength(mFoodRecipe.getTips()) > MAX_RECIPE_DESC_AND_TIPS_LENGTH) {
            final String tips = mFoodRecipe.getTips();
            mFoodRecipe.setTips(StringUtils.subChineseString(tips, 0, MAX_RECIPE_DESC_AND_TIPS_LENGTH));
        }
    }

    /**
     * 检查食材是否有效
     *
     * @return
     */
    private boolean checkFoodIngredients() {
        if (mFoodIngredients == null || mFoodIngredients.isEmpty()) {
            return false;
        }

        for (FoodIngredient ingredient : mFoodIngredients) {
            Logger.d(TAG, "ingredient:" + ingredient.getName());
            boolean validate = isValidateFoodIngredient(ingredient);
            if (!validate) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查步骤是否有效
     *
     * @return
     */
    private boolean checkFoodSteps() {
        if (mFoodSteps == null || mFoodSteps.isEmpty()) {
            return false;
        }

        for (FoodStep step : mFoodSteps) {
            Logger.d(TAG, "step:" + step.getText());
            boolean validate = isValidateFoodStep(step);
            if (!validate) {
                return false;
            }
        }

        return true;
    }

    /**
     * 食材项是否有效 必须要有食材名
     *
     * @return
     */
    private boolean isValidateFoodIngredient(FoodIngredient ingredient) {
        return ingredient != null && !StringUtils.isEmpty(ingredient.getName());
    }

    /**
     * 步骤是否有效 仅文字的步骤必须有文字, 带图的步骤必须有图。
     *
     * @return
     */
    private boolean isValidateFoodStep(FoodStep foodStep) {
        if (foodStep == null
                || (foodStep.isOnlyText() && StringUtils.isEmpty(foodStep.getText()))
                || (!foodStep.isOnlyText() && StringUtils.isEmpty(foodStep.getLocalImage()))
                || StringUtils.chineseLength(foodStep.getText()) > MAX_STEP_DESC_LENGTH) {
            return false;
        }
        return true;
    }

    /**
     * 是否有无效食材
     * @return
     */
    private boolean hasInvalidateIngredientList() {

        boolean hasInvalidateData = false;
        if (!mFoodIngredients.isEmpty()) {
            ListIterator<FoodIngredient> iterator = mFoodIngredients.listIterator();
            while (iterator.hasNext()) {
                FoodIngredient ingredient = iterator.next();
                if (!isValidateFoodIngredient(ingredient)) {
                    hasInvalidateData = true;
                    break;
                }
            }
        }

        return hasInvalidateData;
    }

    /**
     * 更新有用的食材列表
     */
    public boolean updateValidateIngredientList() {
        return updateValidateIngredientList(mFoodIngredients);
    }

    private boolean updateValidateIngredientList(List<FoodIngredient> ingredients) {

        boolean hasInvalidateData = false;
        if (!ingredients.isEmpty()) {
            ListIterator<FoodIngredient> iterator = ingredients.listIterator();
            while (iterator.hasNext()) {
                FoodIngredient ingredient = iterator.next();
                if (!isValidateFoodIngredient(ingredient)) {
                    iterator.remove();
                    hasInvalidateData = true;
                }
            }
        }

        return hasInvalidateData;
    }

    public boolean hasInvalidateStepList() {
        boolean hasInvalidateData = false;
        if (!mFoodSteps.isEmpty()) {
            ListIterator<FoodStep> iterator = mFoodSteps.listIterator();
            while (iterator.hasNext()) {
                FoodStep foodStep = iterator.next();
                if (!isValidateFoodStep(foodStep)) {
                    hasInvalidateData = true;
                    break;
                }
            }
        }
        return hasInvalidateData;
    }

    private void updateValidateStepList() {
        updateValidateStepList(mFoodSteps);
    }

    private boolean updateValidateStepList(List<FoodStep> list) {
        boolean hasInvalidateData = false;
        if (!list.isEmpty()) {
            ListIterator<FoodStep> iterator = list.listIterator();
            while (iterator.hasNext()) {
                FoodStep foodStep = iterator.next();
                if (!isValidateFoodStep(foodStep)) {
                    iterator.remove();
                    hasInvalidateData = true;
                }
            }
        }

        return hasInvalidateData;
    }

    public void saveToDraft() {
        saveToDraft(null);
    }

    public void saveToDraft(final SaveListener listener) {
        saveToDraft(false, listener);
    }

    /**
     * 保存草稿箱
     */
    public void saveToDraft(final boolean publish, final SaveListener listener) {
        //1.保存到数据库
        AppDataLibrary library = DataLibraryManager.getDataLibrary(AppDataLibrary.class);
        final RecipeTable recipeTable = library.getTable(RecipeTable.class);

        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            boolean localSaveSuccess = false;
            boolean remoteSaveSuccess = false;
            RecipeTable.RecipeDB remoteRecipeDB;
            RecipeTable.RecipeDB localRecipeDB;

            @Override
            public void run() {
                if (publish) {
                    Logger.d(TAG, "sync upload for save recipe in publish..");
                    mSyncLoopCount = 0;
                    updatePublishValidate();
                }

                remoteRecipeDB = FoodRecipeDBHelper.createRecipeDB(mFoodRecipe, false);
                localRecipeDB = FoodRecipeDBHelper.createRecipeDB(mFoodRecipe, true);

                Logger.d(TAG, "save recipe id:" + mFoodRecipe.getId());

                long ret = 0;
                if (StringUtils.isEmpty(mFoodRecipe.getCreateTime()) && StringUtils.isEmpty(mFoodRecipe.getId())) {
                    ret = recipeTable.insert(localRecipeDB);
                } else if (!StringUtils.isEmpty(mFoodRecipe.getId())) {
                    int count = recipeTable.getCount(RecipeTable.RECIPE_ID + " = '" + mFoodRecipe.getId() + "'", null);
                    if (count > 0) {
                        ret = recipeTable.update(localRecipeDB, RecipeTable.RECIPE_ID + " = '" + mFoodRecipe.getId() + "'", null);
                    } else {
                        ret = recipeTable.insert(localRecipeDB);
                    }
                } else if (!StringUtils.isEmpty(mFoodRecipe.getCreateTime())) {
                    int count = recipeTable.getCount(RecipeTable.COLUMN_CREATE_TIME + " = '" + mFoodRecipe.getCreateTime() + "'", null);
                    if (count > 0) {
                        ret = recipeTable.update(localRecipeDB, RecipeTable.COLUMN_CREATE_TIME + " = '" + mFoodRecipe.getCreateTime() + "'", null);
                    } else {
                        ret = recipeTable.insert(localRecipeDB);
                    }
                }

                localSaveSuccess = ret > 0;

            }

            @Override
            public void postUi() {
                super.postUi();
                if (!localSaveSuccess) {
                    if (listener != null) {
                        listener.onSave(false, false);
                    }
                    return;
                }

                if (!checkValidateRecipeDBToRemote(remoteRecipeDB)) {
                    if (listener != null) {
                        listener.onSave(localSaveSuccess, remoteSaveSuccess);
                    }
                    return;
                }


                if (!checkLocalUnchange(remoteRecipeDB) || publish) {
                    Logger.d(TAG, "save recipe to remote : " + mFoodRecipe.getId());
                    //2.存储成功同步上传到网络
                    FoodApi.saveFoodRecipe(remoteRecipeDB, new ApiCallback<ID>() {
                        @Override
                        public void onResult(ID result) {
                            if (result.available()) {
                                mFoodRecipe.setId(result.id);
                                localRecipeDB.id = result.id;
                                recipeTable.update(localRecipeDB, RecipeTable.COLUMN_CREATE_TIME
                                        + " = '" + localRecipeDB.createTime + "' ", null);
                                remoteSaveSuccess = true;
                            }
                            Logger.d(TAG, "save recipe[" + mFoodRecipe.getId() + "] remote state : "
                                    + remoteSaveSuccess + " local state : " + localSaveSuccess);
                            if (listener != null) {
                                listener.onSave(localSaveSuccess, remoteSaveSuccess);
                            }
                        }
                    });
                } else {
                    if (listener != null) {
                        listener.onSave(localSaveSuccess, publish);
                    }
                }
            }
        });
    }

    /**
     * 检查数据项是否满足同步到网络
     *
     * @param recipeDB
     * @return
     */
    private boolean checkValidateRecipeDBToRemote(RecipeTable.RecipeDB recipeDB) {
        if (recipeDB == null
                || StringUtils.isEmpty(UserProperties.getUserId())
                || !NetworkState.available()) {
            return false;
        }

        if (StringUtils.isEmpty(recipeDB.name)
                || StringUtils.isEmpty(recipeDB.imageId)
                || StringUtils.isEmpty(recipeDB.ingredients)
                || StringUtils.isEmpty(recipeDB.steps)) {
            return false;
        }

        return true;
    }

    /**
     * 判断本地没有进行修改的时候
     * @param recipeDB
     * @return
     */
    private boolean checkLocalUnchange(RecipeTable.RecipeDB recipeDB) {
        if (mBeforeEditData != null && recipeDB != null
                && StringUtils.equals(mBeforeEditData.id,recipeDB.id)
                && StringUtils.equals(mBeforeEditData.uid,recipeDB.uid)
                && StringUtils.equals(mBeforeEditData.name,recipeDB.name)
                && StringUtils.equals(mBeforeEditData.imageId,recipeDB.imageId)
                && StringUtils.equals(mBeforeEditData.description,recipeDB.description)
                && StringUtils.equals(mBeforeEditData.ingredients,recipeDB.ingredients)
                && StringUtils.equals(mBeforeEditData.assists,recipeDB.assists)
                && StringUtils.equals(mBeforeEditData.steps,recipeDB.steps)
                && StringUtils.equals(mBeforeEditData.tags,recipeDB.tags)
                && StringUtils.equals(mBeforeEditData.difficulty,recipeDB.difficulty)
                && StringUtils.equals(mBeforeEditData.cookTime,recipeDB.cookTime)
                && StringUtils.equals(mBeforeEditData.tips,recipeDB.tips)) {
            return true;
        }
        return false;
    }

    /**
     * 加载未发布的数据
     */
    public List<FoodRecipe> restoreFromLocal() throws JSONException {
        AppDataLibrary library = DataLibraryManager.getDataLibrary(AppDataLibrary.class);
        final RecipeTable recipeTable = library.getTable(RecipeTable.class);
        List<RecipeTable.RecipeDB> all = recipeTable.query(RecipeTable.USER_ID + " = '"
                + UserProperties.getUserId() + "'", null, RecipeTable.COLUMN_MODIFY_TIME + " DESC");
        List<FoodRecipe> recipes = new ArrayList<FoodRecipe>();
        if (all != null && !all.isEmpty()) {
            for (RecipeTable.RecipeDB db : all) {
                recipes.add(FoodRecipeDBHelper.createFoodRecipeFromDB(db));
            }
        }
        return recipes;
    }

    /**
     * 删除
     *
     * @param recipe
     */
    private void deleteFoodRecipeFromDB(FoodRecipe recipe) {
        if (recipe == null) {
            return;
        }
        AppDataLibrary library = DataLibraryManager.getDataLibrary(AppDataLibrary.class);
        final RecipeTable recipeTable = library.getTable(RecipeTable.class);
        if (!StringUtils.isEmpty(recipe.getCreateTime())) {
            recipeTable.delete(RecipeTable.COLUMN_CREATE_TIME + " = '" + recipe.getCreateTime() + "'", null);
        } else if (!StringUtils.isEmpty(recipe.getId())) {
            recipeTable.delete(RecipeTable.RECIPE_ID + " = '" + recipe.getId() + "'", null);
        }
    }

    /**
     * 是否改变
     * @return
     */
    public boolean hasChanged() {
        return mIsNeedToChange;
    }

    public void needToChange(){
        mIsNeedToChange = true;
        if (mOnEditorChanged != null) {
            mOnEditorChanged.onChanged(mIsNeedToChange);
        }
    }

    public void setOnChangedListener(OnEditorChangedListener onChangedListener) {
        mOnEditorChanged = onChangedListener;
    }
    /**
     * 数据库操作辅助类
     */
    private static class FoodRecipeDBHelper {

        /**
         * 创建菜谱数据结构
         *
         * @param recipe
         * @return
         */
        private static RecipeTable.RecipeDB createRecipeDB(FoodRecipe recipe, boolean local) {
            RecipeTable.RecipeDB db = new RecipeTable.RecipeDB();
            if (recipe != null) {
                db.id = recipe.getId();
                db.uid = UserProperties.getUserId();
                db.name = recipe.getTitle();
                db.imageId = recipe.getImageId();
                db.localImage = recipe.getLocalImage();
                db.onlineImage = recipe.getImage();
                db.description = recipe.getDescription();
                db.ingredients = buildIngredientsJson(recipe.getIngredientsList(), local);
                db.assists = buildAssistsJson(recipe.getAssistList(), local);
                db.steps = buildStepsJson(recipe.getStepList(), local);
                db.tags = recipe.getTags();
                db.difficulty = recipe.getDifficulty();
                db.cookTime = recipe.getTime();
                db.tips = recipe.getTips();
                db.createTime = recipe.getCreateTime();
            }
            return db;
        }

        /**
         * 创建菜谱
         *
         * @param db
         * @return
         * @throws JSONException
         */
        private static FoodRecipe createFoodRecipeFromDB(RecipeTable.RecipeDB db) throws JSONException {
            FoodRecipe recipe = new FoodRecipe();
            recipe.setId(db.id);
            recipe.setTitle(db.name);
            recipe.setImageId(db.imageId);
            recipe.setImage(db.onlineImage);
            recipe.setLocalImage(db.localImage);
            recipe.setDescription(db.description);
            if (!StringUtils.isEmpty(db.ingredients)) {
                recipe.setIngredientsList(getFoodIngredientsFromJson(db.ingredients));
            }

            if (!StringUtils.isEmpty(db.assists)) {
                recipe.setAssistList(getFoodAssistsFromJson(db.assists));
            }

            if (!StringUtils.isEmpty(db.steps)) {
                recipe.setStepList(getFoodStepsFromJson(db.steps));
            }

            recipe.setTags(db.tags);
            recipe.setDifficulty(db.difficulty);
            recipe.setTime(db.cookTime);
            recipe.setTips(db.tips);
            recipe.setCreateTime(db.createTime);
            recipe.setModifyTime(db.modifyTime);
            return recipe;
        }

        private static ApiModelList<FoodStep> getFoodStepsFromJson(String json) throws JSONException {
            ApiModelList<FoodStep> list = new ApiModelList<FoodStep>(new FoodStep());
            JSONArray array = JsonUtils.getJSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                FoodStep step = new FoodStep();
                JSONObject obj = array.getJSONObject(i);
                step.setText(obj.optString("text"));
                step.setOnlyText(obj.optBoolean("only_text"));
                step.setLocalImage(obj.optString("local_image"));
                JSONArray medias = obj.optJSONArray("media_ids");
                if (medias != null && medias.length() > 0) {
                    JSONObject media = medias.getJSONObject(0);
                    Media image = new Media();
                    image.setId(media.optString("id"));
                    image.setImage(media.optString("url"));
                    step.setMedia(image);
                }
                list.add(step);
            }
            return list;
        }

        private static ApiModelList<FoodIngredient> getFoodIngredientsFromJson(String json) throws JSONException {
            ApiModelList<FoodIngredient> list = new ApiModelList<FoodIngredient>(new FoodIngredient());
            JSONArray array = JsonUtils.getJSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                FoodIngredient ingredient = new FoodIngredient();
                JSONObject obj = array.getJSONObject(i);
                ingredient.setName(obj.getString("name"));
                ingredient.setQuantity(obj.getString("quantity"));
                list.add(ingredient);
            }
            return list;
        }

        private static ApiModelList<FoodAssist> getFoodAssistsFromJson(String json) throws JSONException {
            ApiModelList<FoodAssist> list = new ApiModelList<FoodAssist>(new FoodAssist());
            JSONArray array = JsonUtils.getJSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                FoodAssist assist = new FoodAssist();
                JSONObject obj = array.getJSONObject(i);
                assist.setName(obj.getString("name"));
                assist.setQuantity(obj.getString("quantity"));
                list.add(assist);
            }
            return list;
        }

        /**
         * 步骤的json数据
         *
         * @param stepList
         * @param saveTodb
         * @return
         */
        private static String buildStepsJson(ApiModelList<FoodStep> stepList, boolean saveTodb) {
            if (stepList != null && !stepList.isEmpty()) {

                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out, "utf-8"));
                    jsonWriter.setIndent("");
                    jsonWriter.setLenient(true);

                    jsonWriter.beginArray();
                    for (FoodStep foodStep : stepList.getList()) {
                        jsonWriter.beginObject();
                        jsonWriter.name("text").value(foodStep.getText());
                        if (saveTodb) {
                            jsonWriter.name("only_text").value(foodStep.isOnlyText());
                            jsonWriter.name("local_image").value(foodStep.getLocalImage());
                        }
                        jsonWriter.name("media_ids");
                        jsonWriter.beginArray();
                        Media media = foodStep.getMedia();
                        if (media != null) {
                            if (saveTodb) {
                                jsonWriter.beginObject();
                                jsonWriter.name("id").value(media.getId());
                                jsonWriter.name("url").value(media.getImage());
                                jsonWriter.endObject();
                            } else {
                                jsonWriter.value(media.getId());
                            }
                        }
                        jsonWriter.endArray();
                        jsonWriter.endObject();
                    }
                    jsonWriter.endArray();

                    jsonWriter.close();
                    byte[] jsonBytes = out.toByteArray();
                    return new String(jsonBytes);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return "";
        }

        /**
         * 创建辅材json
         *
         * @param assistList
         * @param saveTodb
         * @return
         */
        private static String buildAssistsJson(ApiModelList<FoodAssist> assistList, boolean saveTodb) {
            if (assistList != null && !assistList.isEmpty()) {

                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out, "utf-8"));
                    jsonWriter.setIndent("");

                    jsonWriter.beginArray();
                    for (FoodAssist foodAssist : assistList.getList()) {
                        jsonWriter.beginObject();
                        String name = foodAssist.getName();
                        String quality = foodAssist.getQuality();
                        jsonWriter.name("name").value(StringUtils.isEmpty(name) ? "" : name);
                        jsonWriter.name("quantity").value(StringUtils.isEmpty(quality) ? "" : quality);
                        jsonWriter.endObject();
                    }
                    jsonWriter.endArray();

                    jsonWriter.close();
                    byte[] jsonBytes = out.toByteArray();
                    return new String(jsonBytes);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return "";
        }

        /**
         * 创建食材json
         *
         * @param ingredientsList
         * @param saveTodb
         * @return
         */
        private static String buildIngredientsJson(ApiModelList<FoodIngredient> ingredientsList, boolean saveTodb) {
            if (ingredientsList != null && !ingredientsList.isEmpty()) {

                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(out, "utf-8"));
                    jsonWriter.setIndent("");

                    jsonWriter.beginArray();
                    for (FoodIngredient foodIngredient : ingredientsList.getList()) {
                        jsonWriter.beginObject();
                        String name = foodIngredient.getName();
                        String quality = foodIngredient.getQuality();
                        jsonWriter.name("name").value(StringUtils.isEmpty(name) ? "" : name);
                        jsonWriter.name("quantity").value(StringUtils.isEmpty(quality) ? "" : quality);
                        jsonWriter.endObject();
                    }
                    jsonWriter.endArray();

                    jsonWriter.close();
                    byte[] jsonBytes = out.toByteArray();
                    return new String(jsonBytes);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return "";
        }
    }

    /**
     * 保存监听
     */
    public static interface SaveListener {
        void onSave(boolean local, boolean remote);
    }

    /**
     * 发布监听
     */
    public static interface PublishListener {
        void onPublish(boolean success, String message);
    }

    public static interface OnEditorChangedListener {
        void onChanged(boolean changed);
    }
}
