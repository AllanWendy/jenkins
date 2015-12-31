package cn.wecook.app.features.picture;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.MediaStorePicker;
import com.wecook.sdk.api.model.Image;
import com.wecook.uikit.activity.BaseActivity;
import com.wecook.uikit.widget.indicator.CirclePageIndicator;
import com.wecook.uikit.widget.photoview.PhotoView;
import com.wecook.uikit.widget.photoview.PhotoViewAttacher;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.features.message.MessageEventReceiver;
import cn.wecook.app.main.MainActivity;

/**
 * 多图展示
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/8/13
 */
public class MultiPictureActivity extends BaseActivity {

    public static final String EXTRA_IS_SHOW_TITLE = "extra_is_show_title";
    public static final String EXTRA_IMAGES = "extra_images";
    public static final String EXTRA_FIRST_POS = "extra_first_position";

    private ViewPager mViewPager;
    private PagerAdapter mImageAdapter;

    private ArrayList<Image> images;
    private int firstPos;
    private TextView titleView;
    private CirclePageIndicator mIndicator;
    private boolean isShowTitle;
    private LinearLayout titleLayout;
    private boolean isDeletePicture;
    private View mMask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_multi_banner);
        initData();
        initTitle();

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(2);
        mIndicator = (CirclePageIndicator) findViewById(R.id.view_pager_indicator);
        mMask = findViewById(R.id.app_multpicture_mask);


        if (mImageAdapter == null) {
            mImageAdapter = new ImageAdapter();
            mViewPager.setAdapter(mImageAdapter);
        }
        mImageAdapter.notifyDataSetChanged();
        mIndicator.setViewPager(mViewPager);
        mIndicator.setPageCount(((ImageAdapter) mImageAdapter).getData().size());

        if (null != images && images.size() > 0) {
            firstPos += (images.size() * 100);
        }
        mViewPager.setCurrentItem(firstPos);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                titleView.setText(position + 1 + "/" + images.size());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (isShowTitle) {
            if (titleLayout != null) {
                titleLayout.setVisibility(View.VISIBLE);
            }
            if (mIndicator != null) {
                mIndicator.setVisibility(View.GONE);
            }
        } else {
            if (titleLayout != null) {
                titleLayout.setVisibility(View.GONE);
            }
            if (mIndicator != null) {
                mIndicator.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * 初始化数据
     */
    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            images = intent.getParcelableArrayListExtra(EXTRA_IMAGES);
            firstPos = intent.getIntExtra(EXTRA_FIRST_POS, 0);
            isShowTitle = intent.getBooleanExtra(EXTRA_IS_SHOW_TITLE, false);

        }
    }

    /**
     * 初始化title
     */
    private void initTitle() {

        titleLayout = (LinearLayout) findViewById(R.id.app_multipicture_title_layout);

        ImageView backView = (ImageView) findViewById(R.id.app_multipicture_back);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroastAndExit();
            }
        });
        ImageView deleteView = (ImageView) findViewById(R.id.app_multipicture_delete);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除图片
                new ConfirmDialog(getContext(), R.string.app_alarm_delete)
                        .setConfirm(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 删除当前步骤
                                deletePicture();
                            }
                        }).show();

            }
        });

        titleView = (TextView) findViewById(R.id.app_multipicture_title);
        titleView.setText(firstPos + 1 + "/" + images.size());
    }

    private void deletePicture() {
        if (mViewPager != null) {
            int currentItem = mViewPager.getCurrentItem();
            images.remove(currentItem);
            PagerAdapter adapter = mViewPager.getAdapter();
            if (adapter != null) {
                int setCurrentItem;
                if (currentItem > 0) {
                    setCurrentItem = currentItem - 1;
                } else {
                    if (images.size() == 0) {
                        sendBroastAndExit();
                    }
                    setCurrentItem = 0;
                }
                isDeletePicture = true;
                adapter.notifyDataSetChanged();
                titleView.setText(setCurrentItem + 1 + "/" + images.size());
                mIndicator.setPageCount(images.size());
                mViewPager.setCurrentItem(setCurrentItem);
                isDeletePicture = false;
            }
        }
    }

    private void exit() {
        Intent intent = new Intent(this, MainActivity.class);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(0, R.anim.abc_fade_out);
    }

    private void sendBroastAndExit() {
        ArrayList<MediaStorePicker.MediaImage> selected = new ArrayList<MediaStorePicker.MediaImage>();

        for (Image image : images) {
            MediaStorePicker.MediaImage mediaImage = new MediaStorePicker.MediaImage();
            mediaImage.path = image.getShowBigImagePath();
            selected.add(mediaImage);
        }
        Intent data = new Intent(MessageEventReceiver.ACTION_PICK_MULTI_PIC);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MessageEventReceiver.PARAM_LIST_BITMAP, selected);
        data.putExtras(bundle);
        getContext().sendBroadcast(data, MessageEventReceiver.PERMISSION_EVENT);
        exit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            sendBroastAndExit();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 多页面视图
     */
    private class ImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            if (images != null) {
                return Integer.MAX_VALUE;
            }
            return 0;
        }

        public List<Image> getData() {
            return images;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            container.removeView(img.get(position % mImageViewList.size()));
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.view_photo, null);
            PhotoView imageView = (PhotoView) view.findViewById(R.id.app_photo_preview);
            Image image = ListUtils.getItem(images, position % images.size());
            if (image != null) {
                ImageFetcher.asInstance().loadSimple(image.getShowBigImagePath(), imageView);
                imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                    @Override
                    public void onPhotoTap(View view, float x, float y) {
                        sendBroastAndExit();
                    }
                });
            }
            container.addView(view);
            return view;
        }

        @Override
        public int getItemPosition(Object object) {

            if (isDeletePicture) {
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
