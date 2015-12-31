package cn.wecook.app.features.pick;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.BitmapUtils;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.MediaStorePicker;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;

import cn.wecook.app.R;
import cn.wecook.app.features.message.MessageEventReceiver;
import cn.wecook.app.main.recommend.CookShowPublishFragment;

/**
 * 预览
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/29/14
 */
public class PickPreviewFragment extends BaseTitleFragment {

    public static final String EXTRA_IMAGE_LIST = "extra_image_list";
    public static final String EXTRA_CURRENT_IMAGE = "extra_media_image";
    public static final String EXTRA_FOOD = "extra_food";
    private static final int MAX_WIDTH = 960;
    private static final int MAX_HEIGHT = 960;
    private ViewPager mViewPager;
    private TextView mBubble;
    private TextView mBtnNext;
    private ImageView mBtnChoice;
    private PhotoPageAdapter mAdapter;

    private MediaStorePicker.MediaImage mMediaImage;
    private Food mFoodOfCookShow;

    private int mPickType = PickActivity.PICK_ONE;

    private ArrayList<MediaStorePicker.MediaImage> mImages = new ArrayList<MediaStorePicker.MediaImage>();

    private int mCurrentPos;

    private int mCurrentSelectedCount;

    private File[] mBitmapFiles;
    private ViewPager.OnPageChangeListener mPageChangerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mCurrentPos = position;
            updatePhotoView(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mMediaImage = bundle.getParcelable(EXTRA_CURRENT_IMAGE);
            mFoodOfCookShow = (Food) bundle.getSerializable(EXTRA_FOOD);
            mPickType = bundle.getInt(PickActivity.EXTRA_PICK_TYPE);
            ArrayList<MediaStorePicker.MediaImage> list = bundle.getParcelableArrayList(EXTRA_IMAGE_LIST);
            if (list != null && !list.isEmpty()) {
                mImages.addAll(list);
            }
            if (mMediaImage != null && !mImages.contains(mMediaImage)) {
                mImages.add(mMediaImage);
            }

            mBitmapFiles = new File[mImages.size()];
            for (int i = 0; i < mBitmapFiles.length; i++) {
                mBitmapFiles[i] = new File("");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pick_preview, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBubble = (TextView) view.findViewById(R.id.app_pick_bubble);
        mBtnChoice = (ImageView) view.findViewById(R.id.app_pick_preview_choice);

        mBtnNext = (TextView) view.findViewById(R.id.app_pick_next);
        if (mPickType == PickActivity.PICK_ONE
                || mPickType == PickActivity.PICK_MULTI) {
            if (mPickType == PickActivity.PICK_MULTI) {
                mBubble.setVisibility(View.VISIBLE);
            }
            mBtnNext.setText(R.string.app_button_title_finish);
        }
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPickType == PickActivity.PICK_COOK_SHOW) {

                    LogGather.onEventCookShowNext();

                    Bitmap bitmap = getClipBitmap();
                    Bundle bundle = new Bundle();
                    if (mFoodOfCookShow != null) {
                        bundle.putSerializable(CookShowPublishFragment.EXTRA_PUBLISH_FOOD, mFoodOfCookShow);
                    }
                    bundle.putParcelable(CookShowPublishFragment.EXTRA_PUBLISH_BITMAP, bitmap);
                    next(CookShowPublishFragment.class, bundle);

                } else if (mPickType == PickActivity.PICK_ONE) {
                    //pick one
                    File file = getClipBitmapFile();

                    Intent data = new Intent(MessageEventReceiver.ACTION_PICK_ONE_PIC);
                    Bundle bundle = new Bundle();
                    if (file != null) {
                        MediaStorePicker.MediaImage image = MediaStorePicker.build(file.getAbsolutePath(), "", "");
                        bundle.putParcelable(MessageEventReceiver.PARAM_ONE_BITMAP, image);
                    }
                    data.putExtras(bundle);
                    getContext().sendBroadcast(data, MessageEventReceiver.PERMISSION_EVENT);
                    finishAll();
                } else if (mPickType == PickActivity.PICK_MULTI) {
                    //pick multi

                    ArrayList<MediaStorePicker.MediaImage> selected = new ArrayList<MediaStorePicker.MediaImage>();

                    for (MediaStorePicker.MediaImage image : mImages) {
                        if (image.isSelected()) {
                            selected.add(image);
                        }
                    }

                    if (!selected.isEmpty()) {
                        Intent data = new Intent(MessageEventReceiver.ACTION_PICK_MULTI_PIC);
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList(MessageEventReceiver.PARAM_LIST_BITMAP, selected);
                        data.putExtras(bundle);
                        getContext().sendBroadcast(data, MessageEventReceiver.PERMISSION_EVENT);
                        finishAll();
                    }
                }

            }
        });

        view.findViewById(R.id.app_pick_reset_pick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        //初始化ViewPage
        mViewPager = (ViewPager) view.findViewById(R.id.app_pick_preview_pages);
        mViewPager.setOnPageChangeListener(mPageChangerListener);
        initAdapterData();

        //初始化其他界面
        if (mPickType == PickActivity.PICK_MULTI) {
            mBtnChoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaStorePicker.MediaImage image = ListUtils.getItem(mImages, mCurrentPos);
                    image.setSelected(!image.isSelected());
                    mBtnChoice.setSelected(image.isSelected());

                    addOrRemove(image);
                    updateSelectStateChange(image);
                }
            });

            totalSelectCount();
            updateSelectStateChange(mMediaImage);

        } else {
            mBtnChoice.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewPager.removeOnPageChangeListener(mPageChangerListener);
    }

    private void updatePhotoView(int position) {
        View currentView = getCurrentView();
        if (currentView != null) {
            PhotoView photoView = (PhotoView) currentView.findViewById(R.id.app_photo_preview);
            MediaStorePicker.MediaImage item = ListUtils.getItem(mImages, position);
            updatePreview(photoView, item);
            if (mPickType == PickActivity.PICK_MULTI) {
                mBtnChoice.setSelected(item.isSelected());
            }
        }
    }

    private void initAdapterData() {
        mAdapter = new PhotoPageAdapter();

        int currentPos = 0;
        boolean find = false;

        for (MediaStorePicker.MediaImage image : mImages) {
            if (image.equals(mMediaImage)) {
                find = true;
                break;
            }
            currentPos++;
        }

        if (!find) {
            currentPos = 0;
        }

        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(currentPos);
    }

    private void totalSelectCount() {
        for (MediaStorePicker.MediaImage image : mImages) {
            if (image.isSelected()) {
                mCurrentSelectedCount++;
            }
        }
    }

    private void updateSelectStateChange(MediaStorePicker.MediaImage image) {
        if (mCurrentSelectedCount > 0) {
            mBubble.setVisibility(View.VISIBLE);
            mBubble.setText("" + mCurrentSelectedCount);
            mBtnChoice.setSelected(image.isSelected());
            mBtnNext.setVisibility(View.VISIBLE);
        } else {
            mBtnNext.setVisibility(View.GONE);
            mBubble.setVisibility(View.GONE);
        }
    }

    private void addOrRemove(MediaStorePicker.MediaImage image) {
        if (image.isSelected()) {
            mCurrentSelectedCount++;
        } else {
            mCurrentSelectedCount--;
            if (mCurrentSelectedCount < 0) {
                mCurrentSelectedCount = 0;
            }
        }
    }

    private Bitmap getClipBitmap() {
        View currentView = getCurrentView();
        if (currentView != null) {
            PhotoView photoView = (PhotoView) currentView.findViewById(R.id.app_photo_preview);
            photoView.setDrawingCacheEnabled(true);
            photoView.destroyDrawingCache();
            photoView.buildDrawingCache();
            BitmapUtils.BitmapInfo bitmapInfo = BitmapUtils.getBitmapBestFit(photoView.getVisibleRectangleBitmap(),
                    PickPreviewFragment.MAX_WIDTH, PickPreviewFragment.MAX_HEIGHT, 2d * FileUtils.ONE_MB, "");
            return bitmapInfo.src;
        }

        return null;
    }

    private File getClipBitmapFile() {
        View currentView = getCurrentView();
        if (currentView != null) {
            PhotoView photoView = (PhotoView) currentView.findViewById(R.id.app_photo_preview);
            photoView.setDrawingCacheEnabled(true);
            photoView.destroyDrawingCache();
            photoView.buildDrawingCache();
            BitmapUtils.BitmapInfo bitmapInfo = BitmapUtils.getBitmapBestFit(photoView.getVisibleRectangleBitmap(),
                    PickPreviewFragment.MAX_WIDTH, PickPreviewFragment.MAX_HEIGHT, 2d * FileUtils.ONE_MB, "pick-one-pic" + System.currentTimeMillis());
            if (bitmapInfo.srcFile == null) {
                bitmapInfo.srcFile = new File(mMediaImage.path);
            }
            return bitmapInfo.srcFile;
        }

        return null;
    }

    private View getCurrentView() {
        for (int i = 0; i < mViewPager.getChildCount(); i++) {
            View child = mViewPager.getChildAt(i);
            int pos = (Integer) child.getTag(R.id.view_position);
            if (pos == mCurrentPos) {
                return child;
            }
        }

        return null;
    }

    private void updatePreview(final PhotoView photoView, MediaStorePicker.MediaImage image) {
        if (image == null || StringUtils.isEmpty(image.path)) {
            return;
        }

        ImageFetcher.asInstance().load(image.path, photoView, new RequestListener() {
            @Override
            public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target,
                                           boolean isFromMemoryCache, boolean isFirstResource) {

                float scale = 1;

                if (resource instanceof GlideBitmapDrawable) {
                    int resourceWidth = ((GlideBitmapDrawable) resource).getIntrinsicWidth();
                    int resourceHeight = ((GlideBitmapDrawable) resource).getIntrinsicHeight();
                    float scaleX = 1;
                    float scaleY = 1;
                    if (resourceWidth != 0) {
                        scaleX = (float) ScreenUtils.getScreenWidthInt() / resourceWidth;
                    }

                    if (resourceHeight != 0) {
                        scaleY = (float) ScreenUtils.getScreenWidthInt() / resourceHeight;
                    }

                    scale = Math.max(scaleX, scaleY);
                }

                if (scale != 0) {
                    photoView.setMinimumScale(0);
                    photoView.setMaximumScale(Integer.MAX_VALUE);
                    photoView.setScale(scale, ScreenUtils.getScreenWidthInt() / 2, ScreenUtils.getScreenWidthInt() / 2, true);
                    photoView.setMediumScale(scale + 1);
                    photoView.setMinimumScale(scale);
                }

                return false;
            }
        });
    }

    private class PhotoPageAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.view_photo, null);
            PhotoView mPreview = (PhotoView) view.findViewById(R.id.app_photo_preview);
            mPreview.setOnDoubleTapListener(null);
            ScreenUtils.resizeViewOnScreen(mPreview, 1);
            updatePreview(mPreview, ListUtils.getItem(mImages, position));
            view.setTag(R.id.view_position, position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mImages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

}
