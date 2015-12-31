package com.wecook.sdk.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.database.DataLibraryManager;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.KitchenApi;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.dbprovider.AppDataLibrary;
import com.wecook.sdk.dbprovider.tables.ResourceTable;
import com.wecook.sdk.userinfo.UserProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 厨房我的食材管理策略
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/14/14
 */
public class KitchenHomePolicy {

    public static final int ACTION_ADD = 0;
    public static final int ACTION_REMOVE = 1;
    public static final int ACTION_BATCH_ADD = 2;
    public static final int ACTION_BATCH_REMOVE = 3;
    public static final int ACTION_UPDATE_CHANGE = 4;
    public static final int ACTION_SYNC_PUSH = 5;
    public static final int ACTION_SYNC_MERGE = 6;
    public static final int ACTION_SYNC_PULL = 7;
    public static final int ACTION_SELECT_ITEM = 8;
    public static final int ACTION_CLEAR_CHANGE = 9;

    private static KitchenHomePolicy sInstance;
    private OnDataChangedListener mDataChangedListener;

    private AppDataLibrary mAppDataLib;

    private ResourceTable mResourceTable;

    private List<FoodResource> mLocalAllList;
    private List<FoodResource> mNeedAddUpdateList;
    private List<FoodResource> mNeedRemoveUpdateList;
    private boolean mEditorState;

    private BroadcastReceiver mLoginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UserProperties.INTENT_LOGIN.equals(intent.getAction())) {
                syncAll(context);
            }
        }
    };
    private boolean mIsRegistered;
    private boolean mSyncing;
    private boolean mIsPrepared;

    private KitchenHomePolicy() {
        mAppDataLib = DataLibraryManager.getDataLibrary(AppDataLibrary.class);
        mResourceTable = mAppDataLib.getTable(ResourceTable.class);

        mNeedAddUpdateList = new ArrayList<FoodResource>();
        mNeedRemoveUpdateList = new ArrayList<FoodResource>();
        mLocalAllList = new ArrayList<FoodResource>();
    }

    /**
     * 获得对象实例
     *
     * @return
     */
    public static KitchenHomePolicy getInstance() {
        if (sInstance == null) {
            sInstance = new KitchenHomePolicy();
        }

        return sInstance;
    }

    /**
     * 同步厨房
     * 1.获取网络数据
     * 2.合并本地
     * 3.同步网络数据
     */
    public void syncAll(Context context) {
        if (!mIsRegistered) {
            mIsRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(UserProperties.INTENT_LOGIN);
            LocalBroadcastManager.getInstance(context).registerReceiver(mLoginReceiver, filter);
        }

        if (!UserProperties.isLogin()) {
            return;
        }

        if (mSyncing) {
            return;
        }

        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            @Override
            public void run() {
                mSyncing = true;
                final List<FoodResource> allLocalList = new ArrayList<FoodResource>();
                String where = ResourceTable.USER_ID + "='" + UserProperties.getUserId() + "'";
                List<ResourceTable.ResourceDB> localList = mResourceTable.query(where, null, null);
                if (!ListUtils.isEmpty(localList)) {
                    for (ResourceTable.ResourceDB resourceDB : localList) {
                        FoodResource resource = new FoodResource();
                        resource.copyFrom(resourceDB);
                        allLocalList.add(resource);
                    }
                }

                //1.获取网络数据
                KitchenApi.syncPull(new FoodResource(), new ApiCallback<ApiModelList<FoodResource>>() {
                    @Override
                    public void onResult(ApiModelList<FoodResource> result) {
                        if (mDataChangedListener != null) {
                            mDataChangedListener.onResult(ACTION_SYNC_PULL,
                                    result != null && result.available(), result);
                        }
                        //2.合并
                        if (result != null && result.available()) {
                            final List<FoodResource>[] lists = ListUtils.merge(allLocalList, result.getList());
                            if (mDataChangedListener != null) {
                                mDataChangedListener.onResult(ACTION_SYNC_MERGE, lists != null, lists);
                            }
                            //3.同步网络
                            KitchenApi.syncPush(lists[2], new ApiCallback<State>() {
                                @Override
                                public void onResult(State result) {
                                    mNeedAddUpdateList.addAll(lists[0]);
                                    mNeedRemoveUpdateList.addAll(lists[1]);
                                    updateChangeList();
                                    mSyncing = false;
                                    if (mDataChangedListener != null) {
                                        mDataChangedListener.onResult(ACTION_SYNC_PUSH,
                                                result != null && result.available(), lists);
                                    }
                                }
                            });
                        } else {
                            mSyncing = false;
                        }
                    }
                });
            }
        });


    }

    /**
     * 清楚状态变化
     */
    public void clearChangeList() {
        for (FoodResource resource : mNeedRemoveUpdateList) {
            resource.setSelected(false);
        }
        mNeedRemoveUpdateList.clear();

        for (FoodResource resource : mNeedAddUpdateList) {
            resource.setSelected(false);
        }
        mNeedAddUpdateList.clear();

        if (mDataChangedListener != null) {
            mDataChangedListener.onResult(ACTION_CLEAR_CHANGE, true, null);
        }
    }

    /**
     * 更新改变的数据列表
     */
    public void updateChangeList() {

        boolean result = false;
        if (!mNeedRemoveUpdateList.isEmpty()) {
            batchRemove(mNeedRemoveUpdateList);
            mNeedRemoveUpdateList.clear();
            result = true;
        }

        if (!mNeedAddUpdateList.isEmpty()) {
            batchAdd(mNeedAddUpdateList);
            for (FoodResource resource : mNeedAddUpdateList) {
                resource.setSelected(false);
            }
            mNeedAddUpdateList.clear();
            result = true;
        }

        if (mDataChangedListener != null) {
            mDataChangedListener.onResult(ACTION_UPDATE_CHANGE, result, null);
        }
    }

    /**
     * 添加到厨房
     *
     * @param resource
     */
    public void add(FoodResource resource) {
        ResourceTable.ResourceDB item = new ResourceTable.ResourceDB();
        item.copy(resource);
        long rs = mResourceTable.insert(item);
        if (rs != 0) {
            mLocalAllList.add(resource);
        }
        KitchenApi.add(resource.getType(), resource.getId(), new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (mDataChangedListener != null) {
                    mDataChangedListener.onResult(ACTION_ADD,
                            result != null && result.available(), null);
                }
            }
        });
    }

    /**
     * 删除
     *
     * @param type
     * @param id
     */
    public void remove(String type, String id) {
        String where = ResourceTable.TYPE + "=\"" + type + "\""
                + " and " + ResourceTable.RESOURCE_ID + "=" + id;
        int rs = mResourceTable.delete(where, null);
        if (rs != 0) {
            for (FoodResource resource : mLocalAllList) {
                if (resource != null
                        && resource.getType().equals(type)
                        && resource.getId().equals(id)) {
                    mLocalAllList.remove(resource);
                }
            }
        }
        KitchenApi.remove(type, id, new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (mDataChangedListener != null) {
                    mDataChangedListener.onResult(ACTION_REMOVE,
                            result != null && result.available(), null);
                }
            }
        });
    }

    /**
     * 批量增加
     *
     * @param sources
     */
    public <T extends FoodResource> void batchAdd(List<T> sources) {
        List<ResourceTable.ResourceDB> list = new ArrayList<ResourceTable.ResourceDB>();
        for (FoodResource resource : sources) {
            ResourceTable.ResourceDB db = new ResourceTable.ResourceDB();
            resource.setCreateTime(System.currentTimeMillis() + "");
            db.copy(resource);
            list.add(db);
        }
        mResourceTable.batchInsert(list);

        mLocalAllList.addAll(sources);

        KitchenApi.batchAdd(sources, new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (mDataChangedListener != null) {
                    mDataChangedListener.onResult(ACTION_BATCH_ADD,
                            result != null && result.available(), null);
                }
            }
        });
    }

    /**
     * 批量减少
     *
     * @param sources
     */
    public <T extends FoodResource> void batchRemove(List<T> sources) {
        mResourceTable.batchDelete(ResourceTable.RESOURCE_ID, generateIdString(sources));
        mLocalAllList.removeAll(sources);
        KitchenApi.batchRemove(sources, new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (mDataChangedListener != null) {
                    mDataChangedListener.onResult(ACTION_BATCH_REMOVE,
                            result != null && result.available(), null);
                }
            }
        });
    }

    /**
     * [{},...{},...]
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T extends FoodResource> String generateUpdateJsonArray(List<T> list) {
        List<UpdateJsonOut> jsonOutList = new ArrayList<UpdateJsonOut>();
        for (T t : list) {
            UpdateJsonOut jsonOut = new UpdateJsonOut();
            jsonOut.copy(t);
            jsonOutList.add(jsonOut);
        }
        Gson gson = new Gson();
        String json = gson.toJson(jsonOutList);
        Logger.d("update:" + json);
        return json;
    }

    /**
     * [{},...{},...]
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T extends FoodResource> String generateDeleteJsonArray(List<T> list) {
        List<DeleteJsonOut> jsonOutList = new ArrayList<DeleteJsonOut>();
        for (T t : list) {
            DeleteJsonOut jsonOut = new DeleteJsonOut();
            jsonOut.copy(t);
            jsonOutList.add(jsonOut);
        }
        Gson gson = new Gson();
        String json = gson.toJson(jsonOutList);
        Logger.d("delete:" + json);
        return json;
    }

    public static <T extends FoodResource> String generateIdString(List<T> list) {
        String ids = "";
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            if (i == list.size() - 1) {
                ids += t.getId();
            } else {
                ids += t.getId() + ",";
            }
        }
        return ids;
    }

    public boolean isPrepared() {
        return mIsPrepared;
    }

    /**
     * 准备数据
     *
     * @param listener
     */
    public void prepare(final Context context, final OnPreparedListener listener) {
        if (mIsPrepared) {
            return;
        }
        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            @Override
            public void run() {
                prepareAllLocalList();
                syncAll(context);
            }

            @Override
            public void postUi() {
                super.postUi();
                if (!ListUtils.isEmpty(mLocalAllList)) {
                    mIsPrepared = true;
                }

                if (listener != null) {
                    listener.onPrepared(!ListUtils.isEmpty(mLocalAllList));
                }
            }
        });

    }

    private void prepareAllLocalList() {
        mLocalAllList.clear();
        String where = ResourceTable.USER_ID + "='" + UserProperties.getUserId() + "'";
        List<ResourceTable.ResourceDB> localList = mResourceTable.query(where, null, null);
        if (!ListUtils.isEmpty(localList)) {
            for (ResourceTable.ResourceDB resourceDB : localList) {
                FoodResource resource = new FoodResource();
                resource.copyFrom(resourceDB);
                mLocalAllList.add(resource);
            }
        }
    }

    /**
     * 设置数据变化
     *
     * @param onDataChangedListener
     */
    public void setOnDataChangedListener(OnDataChangedListener onDataChangedListener) {
        mDataChangedListener = onDataChangedListener;
    }

    /**
     * 释放
     */
    public void release(Context context) {
        mIsPrepared = false;
        mDataChangedListener = null;
        mNeedAddUpdateList.clear();
        mNeedRemoveUpdateList.clear();
        mEditorState = false;
        if (mIsRegistered) {
            mIsRegistered = false;
            LocalBroadcastManager.getInstance(context).unregisterReceiver(mLoginReceiver);
        }
    }

    /**
     * 获得不同类型的数据列表
     *
     * @param type
     * @return
     */
    public List<FoodResource> getLocalListByType(String type) {
        List<FoodResource> list = new ArrayList<FoodResource>();
        for (FoodResource resource : mLocalAllList) {
            if (resource != null && type.equals(resource.getType())) {
                list.add(resource);
            }
        }
        return list;
    }

    /**
     * 是否正在编辑中
     *
     * @return
     */
    public boolean isInEditor() {
        return mEditorState;
    }

    /**
     * 设置编辑状态
     *
     * @param editor
     */
    public void setInEditor(boolean editor) {
        mEditorState = editor;
    }

    /**
     * 检查是否选中
     *
     * @param itemView
     * @param checkView
     * @param data
     */
    public void checkSelectState(View itemView, final View checkView, final FoodResource data) {
        if (itemView != null && checkView != null) {
            boolean select = isExistInDatabase(data);
            checkView.setSelected(select);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkView.setSelected(!data.isSelected());
                    if (data.isSelected()) {
                        //删除
                        data.setSelected(false);
                        mNeedAddUpdateList.remove(data);
                        mNeedRemoveUpdateList.add(data);
                    } else {
                        //添加
                        data.setSelected(true);
                        mNeedAddUpdateList.add(data);
                        mNeedRemoveUpdateList.remove(data);
                    }

                    if (mDataChangedListener != null) {
                        mDataChangedListener.onResult(ACTION_SELECT_ITEM, true, null);
                    }
                }
            });
        }
    }

    /**
     * 从数据库中查找是否存在
     *
     * @param data
     * @return
     */
    private boolean isExistInDatabase(FoodResource data) {

        if (mLocalAllList.contains(data)
                || mNeedAddUpdateList.contains(data)) {
                data.setSelected(true);
            return true;
        }
        return false;
    }

    /**
     * 获得选中的个数
     *
     * @return
     */
    public int getSelectedCount() {
        int selectedCount = 0;
        for (FoodResource resource : mLocalAllList) {
            if (resource.isSelected()) {
                selectedCount++;
            }
        }
        return mNeedAddUpdateList.size() + selectedCount;
    }

    public int getAllLocalCount() {
        if (mLocalAllList == null || mLocalAllList.isEmpty()) {
            return 0;
        }
        return mLocalAllList.size();
    }

    /**
     * 更新选中item的策略
     *
     * @param imageView
     * @param item
     * @param detail
     */
    public void updateSelectItem(final ImageView imageView, final FoodResource item, final View.OnClickListener detail) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (KitchenHomePolicy.getInstance().isInEditor()) {
                    item.setSelected(!item.isSelected());
                    if (item.isSelected()) {
                        mNeedRemoveUpdateList.add(item);
                    } else {
                        mNeedRemoveUpdateList.remove(item);
                    }
                    if (mDataChangedListener != null) {
                        mDataChangedListener.onResult(ACTION_SELECT_ITEM, true, null);
                    }
                }
                //跳转到详情
                if (detail != null) {
                    detail.onClick(v);
                }
            }
        });
    }

    private static class UpdateJsonOut {

        @SerializedName("type")
        private String type;

        @SerializedName("foreign_id")
        private String foreignId;

        @SerializedName("create_time")
        private String createTime;

        public void copy(FoodResource resource) {
            if (resource != null) {
                type = resource.getType();
                foreignId = resource.getId();
                createTime = resource.getCreateTime();
                if (StringUtils.isEmpty(createTime)) {
                    createTime = "0";
                }
            }
        }

        @Override
        public String toString() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }

    public static class DeleteJsonOut {
        @SerializedName("type")
        private String type;

        @SerializedName("foreign_id")
        private String foreignId;

        public void copy(FoodResource resource) {
            if (resource != null) {
                type = resource.getType();
                foreignId = resource.getId();
            }
        }

        @Override
        public String toString() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }

    public static interface OnPreparedListener {
        /**
         * 是否准备完备
         *
         * @param result
         */
        void onPrepared(boolean result);
    }

    public static interface OnDataChangedListener {
        void onResult(int action, boolean success, Object data);
    }
}
