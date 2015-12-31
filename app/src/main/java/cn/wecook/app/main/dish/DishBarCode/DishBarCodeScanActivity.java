package cn.wecook.app.main.dish.DishBarCode;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.zxing.Result;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.zxing.BeepManager;
import com.wecook.common.modules.zxing.CaptureActivity;
import com.wecook.common.modules.zxing.InactivityTimer;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.main.dish.DishRecentPurchaseFragment;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 扫码界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/18/14
 */
public class DishBarCodeScanActivity extends CaptureActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_capture);

        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        scanContainer = (RelativeLayout) findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
        findViewById(R.id.capture_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        scanLine.startAnimation(animation);
    }

    @Override
    public void handleDecode(Result rawResult, Bundle bundle) {
        InactivityTimer inactivityTimer = getInactivityTimer();
        if (inactivityTimer != null) {
            inactivityTimer.onActivity();
        }
        BeepManager beepManager = getBeepManager();
        if (beepManager != null) {
            beepManager.playBeepSoundAndVibrate();
        }
        if (rawResult != null) {
            String code = rawResult.getText();
            if (!StringUtils.isEmpty(code)) {
                Logger.i("barcode", "request barcode detail with code : " + code);
                Uri uri = Uri.parse(code);
                if (uri != null && "www.wecook.cn".equals(uri.getHost()) && "/download/".equals(uri.getPath())) {
                    if (!UserProperties.isLogin()) {
                        startActivity(new Intent(this, UserLoginActivity.class));
                        restartPreviewAfterDelay(1000);
                        return;
                    }
                    //
                    //wecook 有效url
                    bundle.putInt("width", getmCropRect().width());
                    bundle.putInt("height", getmCropRect().height());
                    bundle.putString(EXTRA_BAR_CODE, rawResult.getText());
                    Intent data = new Intent();
                    data.putExtras(bundle);
                    setResult(RESULT_OK, data);
                    finish();
                    return;
                }
            }
            ToastAlarm.show("请扫描菜品包装上的二维码，查看做法");
            restartPreviewAfterDelay(1000);
            return;
        }
    }
}

