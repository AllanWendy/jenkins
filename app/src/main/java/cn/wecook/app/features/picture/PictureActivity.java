package cn.wecook.app.features.picture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.activity.BaseActivity;
import com.wecook.uikit.widget.photoview.PhotoView;
import com.wecook.uikit.widget.photoview.PhotoViewAttacher;

import cn.wecook.app.R;
import cn.wecook.app.main.MainActivity;

/**
 * 图片详情
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/3/26
 */
public class PictureActivity extends BaseActivity {

    public static final int ANIMATOR_STYLE_SIMPLE = 1;
    public static final int ANIMATOR_STYLE_UP_TO_CENTER = 2;

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_ANIMATOR_STYLE = "extra_animator_style";
    public static final String EXTRA_ORIGINAL_URL = "extra_original_url";

    private PhotoView mPhotoView;
    private ImageView mAnimatorImage;
    private String mUrl;
    private String mOriginalUrl;

    private int animatorStyle = ANIMATOR_STYLE_SIMPLE;

    private Bitmap mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_photo);
        holdIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        holdIntent(intent);
    }

    private void updateViews() {
        mPhotoView = (PhotoView) findViewById(R.id.app_photo_preview);
        mAnimatorImage = (ImageView) findViewById(R.id.app_photo_anim_image);
        int imageHeight = 0;
        if (mImage != null) {
            imageHeight = mImage.getHeight();
            mAnimatorImage.setImageBitmap(mImage);
            mAnimatorImage.setVisibility(View.VISIBLE);
        }
        mPhotoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                exit();
            }
        });

        mPhotoView.setBackgroundColor(getResources().getColor(R.color.uikit_black));
        ViewCompat.setAlpha(mPhotoView, 0f);
        AnimatorSet set = new AnimatorSet();

        if (animatorStyle == ANIMATOR_STYLE_UP_TO_CENTER) {
            PropertyValuesHolder alphaToV = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
            PropertyValuesHolder move = PropertyValuesHolder.ofFloat("Y", 0,
                    (ScreenUtils.getScreenHeightInt() - imageHeight) / 2f);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofPropertyValuesHolder(mPhotoView, alphaToV);
            ObjectAnimator moveAnimator = ObjectAnimator.ofPropertyValuesHolder(mAnimatorImage, move);
            moveAnimator.setInterpolator(new AnticipateInterpolator(5f));
            set.playTogether(alphaAnimator, moveAnimator);
            set.setDuration(700);
        } else if (animatorStyle == ANIMATOR_STYLE_SIMPLE) {
            PropertyValuesHolder alphaToV = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
            PropertyValuesHolder scaleXTo = PropertyValuesHolder.ofFloat("scaleX", 0f, 1f);
            PropertyValuesHolder scaleYTo = PropertyValuesHolder.ofFloat("scaleY", 0f, 1f);
            PropertyValuesHolder moveTo = PropertyValuesHolder.ofFloat("Y", 0,
                    (ScreenUtils.getScreenHeightInt() - imageHeight) / 2f);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofPropertyValuesHolder(mPhotoView, alphaToV);
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofPropertyValuesHolder(mAnimatorImage, scaleXTo);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofPropertyValuesHolder(mAnimatorImage, scaleYTo);
            ObjectAnimator moveAnimator = ObjectAnimator.ofPropertyValuesHolder(mAnimatorImage, moveTo);
            set.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator, moveAnimator);
            set.setDuration(400);
        }

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPhotoView.setImageBitmap(mImage);
                mAnimatorImage.setVisibility(View.GONE);
                loadFullImage();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }

    private void loadFullImage() {

        if (!StringUtils.isEmpty(mOriginalUrl)) {
            ImageFetcher.asInstance().load(mOriginalUrl, new RequestListener<String, GlideBitmapDrawable>() {
                @Override
                public boolean onException(Exception e, String model,
                                           Target<GlideBitmapDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideBitmapDrawable resource, String model,
                                               Target<GlideBitmapDrawable> target, boolean isFromMemoryCache,
                                               boolean isFirstResource) {
                    if (resource != null) {
                        mPhotoView.setImageDrawable(resource);
                    }
                    return false;
                }
            });
        }
    }

    private void holdIntent(Intent intent) {
        if (intent != null) {
            animatorStyle = intent.getIntExtra(EXTRA_ANIMATOR_STYLE, ANIMATOR_STYLE_SIMPLE);
            mUrl = intent.getStringExtra(EXTRA_URL);
            mOriginalUrl = intent.getStringExtra(EXTRA_ORIGINAL_URL);
            if (!StringUtils.isEmpty(mUrl)) {
                ImageFetcher.asInstance().load(mUrl, new RequestListener<String, GlideBitmapDrawable>() {

                    @Override
                    public boolean onException(Exception e, String model, Target<GlideBitmapDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideBitmapDrawable resource, String model, Target<GlideBitmapDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mImage = resource.getBitmap();
                        updateViews();
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        Intent intent = new Intent(this, MainActivity.class);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(0, R.anim.abc_fade_out);
    }
}
