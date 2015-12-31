package com.wecook.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 列表辅助类
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/31/14
 */
public class ListUtils {

    /**
     * 获得列表数据
     *
     * @param list
     * @param index
     * @param <T>
     * @return
     */
    public static <T> T getItem(List<T> list, int index) {
        if (list != null && list.size() > index && index >= 0) {
            return list.get(index);
        }

        return null;
    }

    public static boolean isEmpty(List<?> list) {
        if (list != null) {
            return list.isEmpty();
        }
        return true;
    }

    /**
     * 列表合并
     *
     * @param fromList 从列表
     * @param toList   到列表
     * @param <T>      需要实现equals方法和hashCode方法
     * @return 0:添加1:删除2:所有
     */
    public static <T> List<T>[] merge(List<T> fromList, List<T> toList) {
        List<T>[] result = new List[3];
        List<T> add = new ArrayList<T>();
        List<T> remove = new ArrayList<T>();
        List<T> all = new ArrayList<T>();
        if (!isEmpty(fromList) && !isEmpty(toList)) {

            add.addAll(toList);
            add.removeAll(fromList);

            remove.addAll(fromList);
            remove.removeAll(toList);

            Set<T> set = new HashSet<T>();
            set.addAll(toList);
            set.addAll(fromList);

            all.addAll(set);

            result[0] = add;
            result[1] = remove;
            result[2] = all;
        } else if (!isEmpty(fromList) && isEmpty(toList)) {
            result[0] = add;
            result[1] = remove;
            result[2] = fromList;
        } else if (isEmpty(fromList) && !isEmpty(toList)) {
            result[0] = toList;
            result[1] = remove;
            result[2] = toList;
        } else if (isEmpty(fromList) && isEmpty(toList)) {
            result[0] = add;
            result[1] = remove;
            result[2] = all;
        }

        return result;
    }

    public static boolean equals(List<?> first, List<?> second) {
        if (first != null) {
            return first.equals(second);
        }
        return second == null;
    }

    public static <T> List<T> subList(List<T> list, int start, int end) {
        if (start < end && start < list.size()) {
            if (list.size() > end) {
                return list.subList(start, end);
            } else {
                return list.subList(start, list.size());
            }
        }
        return null;
    }

    public static int getSize(Collection list) {
        if (list != null) {
            return list.size();
        }
        return 0;
    }
}
