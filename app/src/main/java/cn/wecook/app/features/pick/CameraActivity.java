package cn.wecook.app.features.pick;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.filemaster.FileMaster;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.modules.zxing.camera.CameraManager;
import com.wecook.common.utils.BitmapUtils;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.MediaStorePicker;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.activity.BaseActivity;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.widget.TitleBar;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;

/**
 * 拍照
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/23/14
 */
public class CameraActivity extends BaseActivity implements SurfaceHolder.Callback {

    private static final String TAG = "camera";
    public static final String EXTRA_ALBUM = "extra_album";
    private CameraManager mCameraManager;
    private TitleBar mTitleBar;
    private View mFocusViewGroup;
    private SurfaceView mCameraPreview;
    private View mOperatorGroup;
    private ImageView mAlbumImage;
    private View mFocusView;
    private boolean isHasSurface = false;
    private MediaStorePicker.MediaAlbum mAlbum;
    private Matrix mPreviewToCameraMatrix = new Matrix();
    private Matrix mCameraToPreviewMatrix = new Matrix();
    private Rect mPreviewRect;
    private Rect mFocusViewRect;
    private ImageView mCameraPreviewAnimator;
    private File savePicPath;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            mAlbum = intent.getParcelableExtra(EXTRA_ALBUM);
        }

        setContentView(R.layout.activity_camera);

        mTitleBar = (TitleBar) findViewById(com.wecook.uikit.R.id.uikit_title_bar);
        mCameraPreview = (SurfaceView) findViewById(R.id.app_cook_camera_preview);
        mOperatorGroup = findViewById(R.id.app_cook_camera_operator_group);
        mAlbumImage = (ImageView) findViewById(R.id.app_cook_camera_album);
        mFocusView = findViewById(R.id.app_cook_camera_focus);
        mFocusViewGroup = findViewById(R.id.app_cook_camera_focus_group);
        mCameraPreviewAnimator = (ImageView) findViewById(R.id.app_cook_camera_animator);
        int titleBarHeight = getResources().getDimensionPixelSize(R.dimen.uikit_action_bar_default_height);
        int stateBarHeight = getStatusBarHeight();
        ScreenUtils.resizeViewWithSpecial(mOperatorGroup, PhoneProperties.getScreenWidthInt(),
                PhoneProperties.getScreenHeightInt() - PhoneProperties.getScreenWidthInt() - titleBarHeight - stateBarHeight);
        ScreenUtils.resizeViewWithSpecial(mFocusViewGroup, PhoneProperties.getScreenWidthInt(),
                PhoneProperties.getScreenWidthInt());
        ScreenUtils.resizeViewWithSpecial(mCameraPreviewAnimator, PhoneProperties.getScreenWidthInt(),
                PhoneProperties.getScreenWidthInt());

        findViewById(R.id.app_cook_camera_take).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        mCameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    focusOnPoint(event.getX(), event.getY());
                    return true;
                }
                return false;
            }
        });

        mAlbumImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showAlbumIntent = new Intent();
                showAlbumIntent.putExtra(EXTRA_ALBUM, FileMaster.getInstance().getLongImageDir().getName());
                setResult(RESULT_OK, showAlbumIntent);
                finish();
            }
        });

        mTitleBar.setBackgroundColor(getResources().getColor(R.color.uikit_dark));
        mTitleBar.setBackDrawable(R.drawable.uikit_bt_back);
        mTitleBar.enableBottomDiv(false);
        mTitleBar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final TextView titleView = mTitleBar.getTitleView();
        titleView.setTextColor(getResources().getColor(R.color.uikit_font_white));
        titleView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.app_ic_camera_splash, 0, 0, 0);
        titleView.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.uikit_default_drawable_padding));
        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera.Parameters.FLASH_MODE_OFF.equals(getFlashMode())) {
                    titleView.setText("打开");
                    setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                } else if (Camera.Parameters.FLASH_MODE_TORCH.equals(getFlashMode())) {
                    titleView.setText("自动");
                    setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                } else if (Camera.Parameters.FLASH_MODE_AUTO.equals(getFlashMode())) {
                    titleView.setText("关闭");
                    setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
            }
        });

        if (mAlbum != null) {
            MediaStorePicker.MediaImage first = ListUtils.getItem(mAlbum.images, 0);
            if (first != null) {
                ImageFetcher.asInstance().load(first.path, mAlbumImage);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraManager = new CameraManager(getApplicationContext());
        if (isHasSurface) {
            initCamera(mCameraPreview.getHolder());
        } else {
            mCameraPreview.getHolder().addCallback(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraManager.stopPreview();
        mCameraManager.closeDriver();
        if (!isHasSurface) {
            mCameraPreview.getHolder().removeCallback(this);
        }
    }

    public String getFlashMode() {
        Camera.Parameters parameters = mCameraManager.getCamera().getParameters();
        return parameters.getFlashMode();
    }

    public void setFlashMode(String mode) {
        Camera.Parameters parameters = mCameraManager.getCamera().getParameters();
        parameters.setFlashMode(mode);
        mCameraManager.getCamera().setParameters(parameters);
    }

    /**
     * 拍照
     */
    public void takePhoto() {
        if (Build.VERSION.SDK_INT >= 17) {
            mCameraManager.getCamera().enableShutterSound(true);
        }
        mCameraManager.getCamera().takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                Logger.d(TAG, "jpeg length : " + data.length);

                final Rect rect = new Rect();
                mAlbumImage.getGlobalVisibleRect(rect);

                final Rect rectAnimator = new Rect();
                mCameraPreviewAnimator.getGlobalVisibleRect(rectAnimator);

                AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
                    Bitmap bitmap;
                    @Override
                    public void run() {
                        BitmapFactory.Options ops = new BitmapFactory.Options();
                        ops.inSampleSize = 2;
                        bitmap = BitmapUtils.scaleAndCrop(BitmapFactory.decodeByteArray(data, 0, data.length, ops),
                                Gravity.LEFT, PhoneProperties.getScreenWidthInt(),
                                PhoneProperties.getScreenWidthInt(), true);
                    }

                    @Override
                    public void postUi() {
                        super.postUi();
                        mCameraManager.stopPreview();
                        mCameraPreviewAnimator.setImageBitmap(bitmap);

                        ViewCompat.setPivotX(mCameraPreviewAnimator, 0);
                        ViewCompat.setPivotY(mCameraPreviewAnimator, 0);
                        ViewCompat.setAlpha(mCameraPreviewAnimator, 0.9f);
                        final float x = ViewCompat.getX(mCameraPreviewAnimator);
                        final float y = ViewCompat.getY(mCameraPreviewAnimator);
                        final float scaleX = ViewCompat.getScaleX(mCameraPreviewAnimator);
                        final float scaleY = ViewCompat.getScaleY(mCameraPreviewAnimator);
                        final float alpha = ViewCompat.getAlpha(mCameraPreviewAnimator);
                        ViewCompat.animate(mCameraPreviewAnimator)
                                .x(rect.left)
                                .y(rect.top)
                                .scaleX((float) rect.width() / rectAnimator.width())
                                .scaleY((float) rect.height() / rectAnimator.height())
                                .alpha(0)
                                .setInterpolator(new AccelerateInterpolator(2f))
                                .setDuration(600)
                                .setListener(new ViewPropertyAnimatorListener() {
                                    @Override
                                    public void onAnimationStart(View view) {

                                    }

                                    @Override
                                    public void onAnimationEnd(View view) {
                                        ViewCompat.setAlpha(mCameraPreviewAnimator, alpha);
                                        ViewCompat.setX(mCameraPreviewAnimator, x);
                                        ViewCompat.setY(mCameraPreviewAnimator, y);
                                        ViewCompat.setScaleX(mCameraPreviewAnimator, scaleX);
                                        ViewCompat.setScaleY(mCameraPreviewAnimator, scaleY);
                                        mCameraPreviewAnimator.setImageBitmap(null);
                                        ImageFetcher.asInstance().load(savePicPath.getAbsolutePath(), mAlbumImage);
                                        mCameraManager.startPreview();
                                    }

                                    @Override
                                    public void onAnimationCancel(View view) {
                                        ViewCompat.setAlpha(mCameraPreviewAnimator, alpha);
                                        ViewCompat.setX(mCameraPreviewAnimator, x);
                                        ViewCompat.setY(mCameraPreviewAnimator, y);
                                        ViewCompat.setScaleX(mCameraPreviewAnimator, scaleX);
                                        ViewCompat.setScaleY(mCameraPreviewAnimator, scaleY);
                                        mCameraPreviewAnimator.setImageBitmap(null);
                                        mCameraManager.startPreview();
                                    }
                                })
                                .start();
                    }
                });

                AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
                    String picName;

                    @Override
                    public void run() {
                        picName = "wecook-" + System.currentTimeMillis();
                        savePicPath = new File(FileMaster.getInstance().getLongImageDir(), picName + ".jpg");
                        FileUtils.newFileWithFullPath(data, savePicPath);
                        Logger.d(TAG, "jpeg savePicPath : " + savePicPath);
                    }

                    @Override
                    public void postUi() {
                        super.postUi();
                        if (savePicPath != null) {
                            MediaStorePicker.MediaImage image = MediaStorePicker.build(savePicPath.getAbsolutePath(),
                                    picName, "");
                            MediaStorePicker.insert(getContext(), image);
                        }
                    }
                });
            }
        });
    }

    /**
     * 设置对焦点
     *
     * @param x
     * @param y
     */
    private void focusOnPoint(final float x, final float y) {
        if (mPreviewRect == null) {
            mPreviewRect = new Rect();
            mFocusViewGroup.getGlobalVisibleRect(mPreviewRect);
            mPreviewRect.offsetTo(0, 0);
        }

        if (!mPreviewRect.contains((int) x, (int) y)) {
            return;
        }

        if (mFocusViewRect == null) {
            mFocusViewRect = new Rect();
            mFocusView.getGlobalVisibleRect(mFocusViewRect);
        }
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                Camera.Parameters parameters = mCameraManager.getCamera().getParameters();
                if (parameters.getMaxNumMeteringAreas() > 0) {
                    mCameraManager.getAutoFocusManager().stop();
                    List<Camera.Area> areas = getFocusAreas(x, y);
                    parameters.setMeteringAreas(areas);
                    if (parameters.getMaxNumFocusAreas() > 0) {
                        parameters.setFocusAreas(areas);
                    }
                    mCameraManager.getCamera().setParameters(parameters);
                    mCameraManager.getAutoFocusManager().restart();
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ViewCompat.animate(mFocusView)
                                    .x(x - mFocusViewRect.width() / 2)
                                    .y(y - mFocusViewRect.height() / 2)
                                    .start();
                        }
                    });
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private List<Camera.Area> getFocusAreas(float x, float y) {
        float[] coords = {x, y};
        calculatePreviewToCameraMatrix();
        mPreviewToCameraMatrix.mapPoints(coords);
        float focusX = coords[0];
        float focusY = coords[1];

        int focusSize = 50;
        Rect rect = new Rect();
        rect.left = (int) focusX - focusSize;
        rect.right = (int) focusX + focusSize;
        rect.top = (int) focusY - focusSize;
        rect.bottom = (int) focusY + focusSize;
        if (rect.left < -1000) {
            rect.left = -1000;
            rect.right = rect.left + 2 * focusSize;
        } else if (rect.right > 1000) {
            rect.right = 1000;
            rect.left = rect.right - 2 * focusSize;
        }
        if (rect.top < -1000) {
            rect.top = -1000;
            rect.bottom = rect.top + 2 * focusSize;
        } else if (rect.bottom > 1000) {
            rect.bottom = 1000;
            rect.top = rect.bottom - 2 * focusSize;
        }

        ArrayList<Camera.Area> areas = new ArrayList<Camera.Area>();
        areas.add(new Camera.Area(rect, 1000));
        return areas;
    }

    private void calculatePreviewToCameraMatrix() {
        mCameraToPreviewMatrix.reset();
        // from http://developer.android.com/reference/android/hardware/Camera.Face.html#rect
        // Need mirror for front camera
        mCameraToPreviewMatrix.setScale(1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        mCameraToPreviewMatrix.postRotate(90);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        mCameraToPreviewMatrix.postScale(mCameraPreview.getWidth() / 2000f, mCameraPreview.getHeight() / 2000f);
        mCameraToPreviewMatrix.postTranslate(mCameraPreview.getWidth() / 2f, mCameraPreview.getHeight() / 2f);
        mCameraToPreviewMatrix.invert(mPreviewToCameraMatrix);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {

        if (mCameraManager.isOpen()) {
            Logger.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);

            float cameraWidth = mCameraManager.getCameraResolution().y;
            float cameraHeight = mCameraManager.getCameraResolution().x;

            float ratio = cameraHeight / cameraWidth;
            ScreenUtils.resizeView(mCameraPreview, PhoneProperties.getScreenWidthInt(), ratio);

            Logger.d(TAG, "cameraWidth : " + cameraWidth);
            Logger.d(TAG, "cameraHeight : " + cameraHeight);
            mCameraManager.startPreview();

            updateViewAfterCameraReady();
        } catch (IOException ioe) {
            Logger.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Logger.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void updateViewAfterCameraReady() {
        TextView textView = mTitleBar.getTitleView();
        if (Camera.Parameters.FLASH_MODE_OFF.equals(getFlashMode())) {
            textView.setText("关闭");
        } else if (Camera.Parameters.FLASH_MODE_TORCH.equals(getFlashMode())) {
            textView.setText("打开");
        } else if (Camera.Parameters.FLASH_MODE_AUTO.equals(getFlashMode())) {
            textView.setText("自动");
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error
        ToastAlarm.show("相机打开出错，请稍后重试");
        finish();
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = StringUtils.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }
}
