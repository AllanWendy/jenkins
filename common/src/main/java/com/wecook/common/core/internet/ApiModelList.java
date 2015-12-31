package com.wecook.common.core.internet;

import com.wecook.common.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Api列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class ApiModelList<T extends ApiModel> extends ApiModel {

    private List<T> mModelList;
    private T mModel;
    private int mListCount;

    public ApiModelList(T model) {
        mModel = model;
        mModelList = new ArrayList<T>();
    }

    public T getItem(int position) {
        if (position >= mModelList.size() || position < 0) {
            return null;
        } else {
            return mModelList.get(position);
        }
    }

    public List<T> getList() {
        return mModelList;
    }

    public void addAll(ApiModelList<T> apiModelList) {
        if (apiModelList != null) {
            List<T> list = apiModelList.getList();
            addAll(list);
        }
    }

    public void addAll(List<T> list) {
        if (list != null && !list.isEmpty()) {
            mModelList.addAll(list);
        }
    }

    public void add(T item) {
        if (item != null) {
            mModelList.add(item);
        }
    }

    public void remove(T item) {
        if (item != null) {
            mModelList.remove(item);
        }
    }

    public void clear() {
        mModelList.clear();
        mListCount = 0;
    }

    public int getCountOfServer() {
        return mListCount;
    }

    public int getCountOfList() {
        if (mModelList != null) {
            return mModelList.size();
        }
        return 0;
    }

    public boolean isEmpty() {
        if (mModelList != null) {
            return mModelList.isEmpty();
        }
        return false;
    }

    @Override
    public boolean available() {
        return super.available() && !isEmpty();
    }

    @Override
    public void parseJson(String json) throws JSONException {
        if (JsonUtils.isJsonArray(json)) {
            JSONArray array = JsonUtils.getJSONArray(json);
            if (array != null) {
                List<T> modelList = parseJSONArray(array, mModel);
                addAll(modelList);
            }
        } else if (JsonUtils.isJsonObject(json)) {
            JSONObject result = JsonUtils.getJSONObject(json);
            if (result != null) {
                Iterator<String> keys = result.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object o = result.opt(key);
                    if (o != null
                            && !(o instanceof JSONArray)
                            && !(o instanceof JSONObject)) {
                        getExtra().putString(key, o.toString());
                    }
                }
                mListCount = result.optInt("count");
                JSONArray array = null;
                if (result.has("list")) {
                    array = result.optJSONArray("list");
                } else {
                    array = mModel.findJSONArray(json);
                }

                if (array != null) {
                    List<T> modelList = parseJSONArray(array, mModel);
                    addAll(modelList);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ApiModelList{" +
                "mModelList=" + mModelList +
                ", mModel=" + mModel +
                ", mListCount=" + mListCount +
                '}';
    }
}
