package com.wecook.common.modules.thirdport.platform;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.wecook.common.R;
import com.wecook.common.modules.thirdport.object.IShareObject;
import com.wecook.common.utils.HttpUtils;
import com.wecook.common.utils.StringUtils;

import java.util.ArrayList;

/**
 * QQ空间
 */
public class QQZone extends QQConnect {

    public QQZone(Context context) {
        super(context);
    }

	@Override
	public void onShare(IShareObject... shareObject) {

		if (shareObject == null || shareObject.length == 0) {
			return;
		}

		IShareObject object = shareObject[0];

		final Bundle params = new Bundle();
		int shareType = QQShare.SHARE_TO_QQ_TYPE_DEFAULT;
		switch (object.getType()) {
			case TYPE_IMAGE://只支持本地
				shareType = QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE;
				ArrayList<String> arrayList = new ArrayList<String>();
				arrayList.add(object.getOnlineThumbnailUrl()[0]);
				params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, arrayList);
				params.putString(QzoneShare.SHARE_TO_QQ_TITLE, object.getTitle());
				params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, object.getRedirectUrl());
				params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, object.getSecondTitle());
				params.putString(QzoneShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, object.getLocalThumbnailPaths()[0]);
				break;
			case TYPE_TEXT:
				shareType = QzoneShare.SHARE_TO_QZONE_TYPE_NO_TYPE;
				params.putString(QzoneShare.SHARE_TO_QQ_TITLE, object.getTitle());
				params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, object.getRedirectUrl());
				params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, object.getSecondTitle());
				break;
			case TYPE_WEBURL:
			case TYPE_MUSIC:
			case TYPE_VIDEO:
				shareType = QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;
				ArrayList<String> arrayList1 = new ArrayList<String>();
				arrayList1.add(object.getOnlineThumbnailUrl()[0]);
				params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, arrayList1);
				params.putString(QzoneShare.SHARE_TO_QQ_TITLE, object.getTitle());
				params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, object.getRedirectUrl());
				params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, object.getSecondTitle());
				params.putString(QzoneShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, object.getLocalThumbnailPaths()[0]);
				break;
		}

		params.putString(QQShare.SHARE_TO_QQ_APP_NAME, getName());
		params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, shareType);


		new Thread(){
			@Override
			public void run() {
				super.run();
				mTencent.shareToQzone((Activity) getContext(), params, QQZone.this);
			}
		}.start();
	}

	@Override
	public boolean shareValidateCheck(IShareObject... shareObjects) {
		if (shareObjects == null || shareObjects.length == 0) {
			logE("数据为NULL或者空");
			notifyEvent(getString(R.string.share_invalidate_datas));
			return false;
		}

		for (IShareObject object : shareObjects) {
			if (object == null) {
				notifyEvent(getString(R.string.share_invalidate_datas));
				return false;
			}

			if (StringUtils.isEmpty(object.getTitle())) {
				notifyEvent(getString(R.string.share_text_empty));
				return false;
			} else if(StringUtils.isEmpty(object.getRedirectUrl())
					|| !HttpUtils.isValidHttpUri(object.getRedirectUrl())) {
				notifyEvent(getString(R.string.share_invalidate_redirect_url));
				return false;
			}

			IShareObject.TYPE type = object.getType();

			switch (type) {
				case TYPE_WEBURL:
					String[] urls = object.getOnlineThumbnailUrl();
					if (urls == null || urls.length == 0 || StringUtils.isEmpty(urls[0])
							|| !HttpUtils.isValidHttpUri(urls[0])) {
						notifyEvent(getString(R.string.share_webpage_invalidate_url));
						return false;
					}
					break;
				case TYPE_IMAGE:
					String local = object.getLocalThumbnailPaths()[0];
					if (local == null || StringUtils.isEmpty(local)
							|| !HttpUtils.isValidSdcardUri(local)) {
						notifyEvent(getString(R.string.share_image_only_for_local));
						return false;
					}
					break;
				case TYPE_MUSIC:
				case TYPE_VIDEO:
					String[] online = object.getOnlineThumbnailUrl();
					if (online == null || online.length == 0 || StringUtils.isEmpty(online[0])
							|| !HttpUtils.isValidHttpUri(online[0])) {
						notifyEvent(getString(R.string.share_image_invalid_url));
						return false;
					} else if(StringUtils.isEmpty(object.getMediaUrl())
							|| !HttpUtils.isValidHttpUri(object.getMediaUrl())) {
						notifyEvent(type == IShareObject.TYPE.TYPE_IMAGE ?
								getString(R.string.share_music_invalidate_url)
								:
								getString(R.string.share_video_invalidate_url));
						return false;
					}
					break;
			}
		}
		return true;
	}

	@Override
	public void onCancel() {
		String action = getCurrentAction();
		if (ACTION_LOGIN.equals(action)) {
			performLogin(false, getString(R.string.share_errcode_login_cancel));
		} else if (ACTION_SHARE.equals(action)) {
			performShare(false, "");
		}
	}

}
