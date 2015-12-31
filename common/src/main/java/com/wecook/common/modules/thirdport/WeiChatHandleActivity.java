package com.wecook.common.modules.thirdport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

/**
 * 从微信返回客户端的回调Activity
 * 需要配合包名下的 {@link 包名.wxapi.WXEntryActivity} 才能正常使用
 *
 * @author by kevin on 4/15/14.
 */
public class WeiChatHandleActivity extends Activity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		holdIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
		holdIntent(intent);
	}

	private void holdIntent(Intent intent) {
		PlatformManager.getInstance().onHandleNewIntent(this, intent);
		finish();
	}

    @Override
    public void onResp(BaseResp baseResp) {
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }
}