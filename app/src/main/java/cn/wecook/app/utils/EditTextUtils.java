package cn.wecook.app.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

/**
 * Created by LK on 2015/10/14.
 */
public class EditTextUtils {
    /**
     * 拦截空格输入
     *
     * @param editText
     */
    public static void addTrimInflate(EditText editText, final int maxLength) {
        if (null == editText) {
            return;
        }
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                //返回null表示接收输入的字符,返回空字符串表示不接受输入的字符
                if (source.equals(" ")) {
                    return "";
                } else if (null != source && source.toString().contains(" ")) {
                    return source.toString().replace(" ", "");
                } else if (maxLength > 0) {
                    if (null != dest && dest.length() >= maxLength) {
                        return "";
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }
}
