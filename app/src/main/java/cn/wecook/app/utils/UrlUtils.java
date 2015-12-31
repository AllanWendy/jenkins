package cn.wecook.app.utils;

/**
 * Created by LK on 2015/10/13.
 */
public class UrlUtils {
    /**
     * 添加p_title
     *
     * @param url
     * @param title
     * @return
     */
    public static String addPTitle(String url, String title) {
        if (null == url) {
            return url;
        }
        if (url.contains("p_title=")) {
            return url;
        }
        if (null == title || "".equals(title)) {
            return url;
        }

        if (url.contains("?")) {
            return url + "&p_title=" + title;
        } else {
            return url + "?p_title=" + title;
        }
    }
}
