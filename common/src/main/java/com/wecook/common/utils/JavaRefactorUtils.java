package com.wecook.common.utils;

import com.wecook.common.core.debug.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * java反射辅助类
 *
 * @author kevin created at 9/22/14
 * @version 1.0
 */
public class JavaRefactorUtils {

    /**
     * 获得子类声明的范型class
     *
     * @param o
     * @param index
     * @return
     */
    public static Class getGenericType(Object o, int index) {
        if (o == null) {
            return null;
        }
        Type genType = o.getClass().getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            throw new RuntimeException("Index outof bounds");
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[index];
    }

    /**
     * 通过Class创建对象
     * @param orderCls
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    public static <T> T newInstanceForClass(Class<T> orderCls) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (orderCls != null) {
//            Constructor<T> c = orderCls.getConstructor(null);
//            if(c != null) {
//                c.setAccessible(true);
//                return orderCls.newInstance();
//            }

            Constructor<T> c = orderCls.getDeclaredConstructor();
            if (c != null) {
                c.setAccessible(true);
                return c.newInstance();
            }
        }
        return null;
    }

    /**
     * 通过Class创建对象
     * @param orderCls
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    public static <T> T newInstance(Class<T> orderCls) {
        if (orderCls != null) {
            Constructor<T> c = null;
            try {
                c = orderCls.getDeclaredConstructor();
                if (c != null) {
                    c.setAccessible(true);
                    return c.newInstance();
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    /**
     * 获得简单的类名
     * @param obj
     * @return
     */
    public static String getClassName(Object obj) {
        if (obj != null) {
            return obj.getClass().getSimpleName();
        }
        return "";
    }

    public static Object invokeMethod(Object obj, String methodName, Class<?>[] paramsTypes, Object[] params) {
        try {
            Method method = getDeclaredMethod(obj, methodName, paramsTypes);
            if (method != null) {
                method.setAccessible(true);
                return method.invoke(obj, params);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static Method getDeclaredMethod(Object obj, String methodName, Class<?>... paramsTypes) {
        Method method = null;

        for (Class<?> cls = obj.getClass(); cls != Object.class; cls = cls.getSuperclass()) {
            try {
                method = cls.getDeclaredMethod(methodName, paramsTypes);
                if (method != null) {
                    return method;
                }
            } catch (NoSuchMethodException e) {
                Logger.w("NoSuchMethodException: " + methodName);
            }
        }

        return method;
    }

    public static List<Field> getAllDeclaredFields(Class objCls) {
        List<Field> allFields = new ArrayList<Field>();
        for (Class<?> cls = objCls; cls != Object.class; cls = cls.getSuperclass()) {
            Field[] fields = cls.getDeclaredFields();
            Collections.addAll(allFields, fields);
        }
        return allFields;
    }

    public static Object getFieldValue(Object obj, Class cls, String fieldName) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(obj);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setFieldValue(Object obj, Class cls, String fieldName, Object value) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(obj, value);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
