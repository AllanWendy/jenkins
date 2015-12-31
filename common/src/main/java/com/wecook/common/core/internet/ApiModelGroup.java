package com.wecook.common.core.internet;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Api列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class ApiModelGroup<T> implements Parcelable{

    private List<T> mModelList;
    private int mGroupN;
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String name) {
        this.groupName = name;
    }

    public ApiModelGroup(int N) {
        mGroupN = N;
        mModelList = new ArrayList<T>();
    }

    public List<ApiModelGroup<T>> loadChildrenFromList(ApiModelList list) {
        if (list != null) {
            return loadChildrenFromList(list.getList());
        }
        return null;
    }

    public List<ApiModelGroup<T>> loadChildrenFromList(List<T> list) {
        List<ApiModelGroup<T>> groups = new ArrayList<ApiModelGroup<T>>();
        if (list != null) {
            ApiModelGroup<T> group = null;
            for (int i = 0; i < list.size(); i++) {
                T t = list.get(i);
                if (i % mGroupN == 0) {
                    group = new ApiModelGroup<T>(mGroupN);
                    groups.add(group);
                }
                group.mModelList.add(t);
            }
        }
        return groups;
    }

    public ArrayList<T> parseToList(List<ApiModelGroup<T>> groups) {
        ArrayList<T> list = new ArrayList<T>();
        if (groups != null) {

            for (ApiModelGroup<T> group : groups) {
                list.addAll(group.getList());
            }
        }
        return list;
    }

    public List<T> getList() {
        return mModelList;
    }

    public T getItem(int pos) {
        if (pos >= mModelList.size() || pos < 0) {
            return null;
        }

        return mModelList.get(pos);
    }

    public void addItem(T item) {
        mModelList.add(item);
    }

    public void addItem(int index, T item) {
        mModelList.add(index, item);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupName);
        dest.writeInt(mGroupN);
        dest.writeList(mModelList);
    }

    public void readFromParcel(Parcel source) {
        groupName = source.readString();
        mGroupN = source.readInt();
        ArrayList list = source.readArrayList(null);
        if (list != null) {
            mModelList.addAll(list);
        }
    }

    public static final Creator<ApiModelGroup> CREATOR = new Creator<ApiModelGroup>() {
        @Override
        public ApiModelGroup createFromParcel(Parcel source) {
            ApiModelGroup group = new ApiModelGroup(0);
            group.readFromParcel(source);
            return group;
        }

        @Override
        public ApiModelGroup[] newArray(int size) {
            return new ApiModelGroup[0];
        }
    };
}
