package com.wecook.common.modules.thirdport.platform;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.tencent.connect.share.QQShare;
import com.wecook.common.R;
import com.wecook.common.modules.thirdport.object.IShareObject;
import com.wecook.common.utils.HttpUtils;
import com.wecook.common.utils.StringUtils;

/**
 * QQ好友
 *
 * @author zhoulu
 * @date 13-12-13
 */
public class QQFriends extends QQConnect {

    private int mExtraFlag;

	private QQShare mQQShare;

    public QQFriends(Context context) {
        super(context);
    }

	@Override
	public boolean onCreate() {
		super.onCreate();
		mQQShare = new QQShare(getContext(), mTencent.getQQToken());
		return true;
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
				shareType = QQShare.SHARE_TO_QQ_TYPE_IMAGE;
                params.putString(QQShare.SHARE_TO_QQ_TITLE, object.getTitle());
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, object.getRedirectUrl());
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, object.getSecondTitle());
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, object.getLocalThumbnailPaths()[0]);
                break;
            case TYPE_TEXT:
                params.putString(QQShare.SHARE_TO_QQ_TITLE, object.getTitle());
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, object.getRedirectUrl());
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, object.getSecondTitle());
                break;
            case TYPE_WEBURL:
				params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, object.getOnlineThumbnailUrl()[0]);
				params.putString(QQShare.SHARE_TO_QQ_TITLE, object.getTitle());
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, object.getRedirectUrl());
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, object.getSecondTitle());
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, object.getLocalThumbnailPaths()[0]);
                break;
            case TYPE_MUSIC:
            case TYPE_VIDEO:
                shareType = QQShare.SHARE_TO_QQ_TYPE_AUDIO;
                params.putString(QQShare.SHARE_TO_QQ_TITLE, object.getTitle());
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, object.getRedirectUrl());
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, object.getSecondTitle());
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, object.getOnlineThumbnailUrl()[0]);
                params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, object.getMediaUrl());
                break;
        }

        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, getName());
		params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, shareType);

		mExtraFlag &= (0xFFFFFFFF - QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
		mExtraFlag |= QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE;

        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, mExtraFlag);

        new Thread(){
            @Override
            public void run() {
                super.run();
				mQQShare.shareToQQ((Activity) getContext(), params, QQFriends.this);
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

}
