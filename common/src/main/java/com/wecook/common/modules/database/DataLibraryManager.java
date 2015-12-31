package com.wecook.common.modules.database;

import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.ModuleManager;

import java.util.Hashtable;

/**
 * 数据库管理
 *
 * @author kevin created at 9/22/14
 * @version 1.0
 */
public class DataLibraryManager extends BaseModule{

    private static Hashtable<String, BaseDataLibrary> sLibraries = new Hashtable<String, BaseDataLibrary>();

    private static DataLibraryManager sDataLibraryManager;

    private DataLibraryManager(){}

    public static DataLibraryManager getInstance() {
        if (sDataLibraryManager == null) {
            sDataLibraryManager = (DataLibraryManager) ModuleManager.asInstance().getModule(DataLibraryManager.class);
        }

        return sDataLibraryManager;
    }

    /**
     * 添加数据库
     *
     * @param library
     */
    public static <T extends BaseDataLibrary> void addLibrary(T library) {
        if (library != null && !sLibraries.containsKey(library.getClass().getName())) {
            sLibraries.put(library.getClass().getName(), library);
            library.open();
        }
    }

    /**
     * 获得对象实例
     *
     * @param clsName
     * @return
     */
    public static <T extends BaseDataLibrary> T getDataLibrary(Class<T> clsName) {
        return (T) sLibraries.get(clsName.getName());
    }


}
