package cn.wecook.app;

import android.test.AndroidTestCase;

import com.wecook.sdk.dbprovider.PinyinProcess;

/**
 * 处理拼音工具
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/4/10
 */
public class TestPinyin extends AndroidTestCase {

    public void testProcess() {
        PinyinProcess.processPinyin(getContext(), "sj_ingredients.json");
    }
}
