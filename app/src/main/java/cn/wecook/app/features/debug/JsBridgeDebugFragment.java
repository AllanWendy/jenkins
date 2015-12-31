package cn.wecook.app.features.debug;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.wecook.common.core.debug.Logger;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.features.webview.WebViewFragment;

/**
 * 测试jsbridge
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/3/25
 */
public class JsBridgeDebugFragment extends WebViewFragment implements IDebug {

    private static final String JS_TEST_URL = "http://wecook-jsbridge.wecook.com.cn/";

    @Override
    public String getTitle() {
        return "JSBridge测试";
    }

    @Override
    public Class<? extends BaseFragment> getFragmentCls() {
        return getClass();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        loadUrl(JS_TEST_URL);
        loadAssetHtml("bridgedemo.html");

        TitleBar.ActionTextView nav = new TitleBar.ActionTextView(getContext(), "浏览器");
        getTitleBar().addActionView(nav);
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(JS_TEST_URL);
                intent.setData(uri);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                String intentString = intent.toString();
                Logger.d("intent schema : " + intentString);
                startActivity(intent);
            }
        });
    }
}
