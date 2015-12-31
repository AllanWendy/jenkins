package cn.wecook.app.launch;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.ImageView;

import com.wecook.common.app.AppLink;
import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiConfiger;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.ChannelConfig;
import com.wecook.sdk.api.model.SplashScreen;
import com.wecook.uikit.activity.BaseFragmentActivity;
import com.wecook.uikit.widget.NoScrollViewPager;
import com.wecook.uikit.widget.indicator.CirclePageIndicator;

import cn.wecook.app.R;
import cn.wecook.app.WecookApp;
import cn.wecook.app.WecookConfig;
import cn.wecook.app.main.MainActivity;
import cn.wecook.app.server.LocalService;

/**
 * 启动画面
 */
public class LaunchActivity extends BaseFragmentActivity {

    private static final String KEY_FIRST_LAUNCH = "first_launch_2.5.0";

    /**
     * 启动图（供运营使用）
     */
    private ImageView mLaunchImage;
    /**
     * 渠道合作logo
     */
    private ImageView mChannelImage;

    private ImageView mADSkipButton;
    private NoScrollViewPager mGuideViewPager;
    private CirclePageIndicator mIndicator;
    private View mGuideGroup;

    private SplashScreen mSplashImage = new SplashScreen();
    private ChannelConfig config = new ChannelConfig(this);

    private Runnable mADSleepCountDown = new Runnable() {
        @Override
        public void run() {
            gotoMainActivity();
        }
    };

    private Runnable mSplashSleepCountDown = new Runnable() {
        @Override
        public void run() {
            if (mSplashImage.checkValidate()) {
                mChannelImage.setVisibility(View.GONE);
                BitmapFactory.Options options = new BitmapFactory.Options();
                mLaunchImage.setImageBitmap(BitmapFactory.decodeFile(mSplashImage.getFilePath(), options));
                mLaunchImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mADSkipButton.setVisibility(View.VISIBLE);
                UIHandler.postDelayed(mADSleepCountDown, 2000);
            } else {
                gotoMainActivity();
            }
        }
    };

    @Override
    public boolean openDeliver() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //启动本地服务
        LocalService.start(this);

        //判断是否开启位置请求功能
        LocationServer.asInstance().setOnLocationChangedListener(new LocationServer.OnLocationChangedListener() {
            @Override
            public void onLocationChanged(double lon, double lat) {
                if (lon != 0 && lat != 0) {
                    initApi();
                }
            }
        });
        //请求定位
        LocalService.startLocation(this);

        //判断是否正在退出软件，如果是则重新进入
        if (WecookApp.isPendingKillApp()) {
            WecookApp.resumeApp();
            gotoMainActivity();
            return;
        }

        setContentView(R.layout.activity_launch);
        mGuideViewPager = (NoScrollViewPager) findViewById(R.id.app_launch_guide);
        mGuideViewPager.setOffscreenPageLimit(4);
        mLaunchImage = (ImageView) findViewById(R.id.app_launch_image);
        mChannelImage = (ImageView) findViewById(R.id.app_launch_channel_image);
        mGuideGroup = findViewById(R.id.app_launch_guide_group);
        mADSkipButton = (ImageView) findViewById(R.id.app_launch_ad_bt_skip);
        mIndicator = (CirclePageIndicator) findViewById(R.id.app_launch_guide_indicator);

        mADSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMainActivity(true);
            }
        });

        mLaunchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMainActivity();
            }
        });

        //判断并显示渠道图片
        config.load();
        if (config.validate()) {
            mChannelImage.setVisibility(View.VISIBLE);
            ImageFetcher.asInstance().loadSimple(config.getLogo(), mChannelImage);
        } else {
            mChannelImage.setVisibility(View.GONE);
        }

        //判断并显示启动运营图片
        boolean isFirstLaunch = (Boolean) SharePreferenceProperties.get(KEY_FIRST_LAUNCH, true);
        if (isFirstLaunch) {
            mLaunchImage.setVisibility(View.GONE);
            showGuide();
        } else {
            mGuideGroup.setVisibility(View.GONE);
            showSplash();
        }
    }

    private void initApi() {
        Api.init(getContext(), WecookConfig.getInstance(), new ApiCallback<ApiConfiger>() {
            @Override
            public void onResult(ApiConfiger result) {
                if (result instanceof WecookConfig) {
                    mSplashImage.updateRemoteImage(((WecookConfig) result).getSplashScreen());
                }
            }
        });
    }

    /**
     * 进入主页面
     */
    private void gotoMainActivity() {
        gotoMainActivity(false);
    }

    /**
     * 进入主页面
     *
     * @param skip 跳过广告
     */
    private void gotoMainActivity(boolean skip) {
        if (!isFinishing()) {
            UIHandler.stopPost(mADSleepCountDown);
            UIHandler.stopPost(mSplashSleepCountDown);
            Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!skip && mSplashImage != null && !StringUtils.isEmpty(mSplashImage.getAdUrl())) {
                //增加到页面跳转队列中
                AppLink.pendingLink(mSplashImage.getAdUrl());
            }
            startActivity(intent);
            finish();
        }
    }

    /**
     * 显示味库Logo图片
     */
    private void showSplash() {
        UIHandler.postDelayed(mSplashSleepCountDown, 2000);
    }

    /**
     * 显示引导页面
     */
    private void showGuide() {
        final ImageAdapter adapter = new ImageAdapter();
        mGuideViewPager.setAdapter(adapter);
        mGuideViewPager.setScrollListener(new NoScrollViewPager.ScrollListener() {
            @Override
            public void scroll(final int inPosition, int outPosition, int type) {

                switch (type) {
                    case NoScrollViewPager.ScrollListener.TYPE_LEFT_SCROLL:
                        setGuideAnimation(inPosition, outPosition, NoScrollViewPager.ScrollListener.TYPE_LEFT_SCROLL);
                        break;
                    case NoScrollViewPager.ScrollListener.TYPE_RIGHT_SCROLL:
                        setGuideAnimation(inPosition, outPosition, NoScrollViewPager.ScrollListener.TYPE_RIGHT_SCROLL);
                        break;
                }
            }

            /**
             * 设置引导动画
             *
             * @param inPosition
             * @param outPosition
             * @param typeLeftScroll
             */
            private void setGuideAnimation(final int inPosition, int outPosition, int typeLeftScroll) {
                final GuideBaseFragment inFragment = GuideFragmentManager.asinstall().getFragmentByPosition(inPosition);
                GuideBaseFragment outFragment = GuideFragmentManager.asinstall().getFragmentByPosition(outPosition);
                switch (typeLeftScroll) {
                    case NoScrollViewPager.ScrollListener.TYPE_LEFT_SCROLL:
                        if (outFragment != null) {
                            outFragment.outAnimationLeftToRight();
                            UIHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mGuideViewPager.setCurrentItem(inPosition);
                                    if (inFragment != null) {
                                        inFragment.inAnimationLeftToRight();
                                    }
                                }
                            }, outFragment.getOutAnimationTime());
                        }
                        break;
                    case NoScrollViewPager.ScrollListener.TYPE_RIGHT_SCROLL:
                        if (outFragment != null) {
                            outFragment.outAnimationRightToLeft();
                            UIHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mGuideViewPager.setCurrentItem(inPosition);
                                    if (inFragment != null) {
                                        inFragment.inAnimationRightToLeft();
                                    }
                                }
                            }, outFragment.getOutAnimationTime());
                        }
                        break;
                }

            }

        });
        mIndicator.setViewPager(mGuideViewPager);
        mIndicator.setPageCount(adapter.getCount());
    }

    /**
     * 引导图片
     */
    private class ImageAdapter extends FragmentPagerAdapter {

        private ImageAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 3) {
                GuideFourFragment fragment = (GuideFourFragment) GuideFragmentManager.asinstall().getFragmentByPosition(position);
                fragment.onBtnClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharePreferenceProperties.set(KEY_FIRST_LAUNCH, false);
                        SharePreferenceProperties.set(LocalService.KEY_IS_IN_BEIJING, true);
                        gotoMainActivity();
                    }
                });
            }
            return GuideFragmentManager.asinstall().getFragmentByPosition(position);
        }
    }
}

