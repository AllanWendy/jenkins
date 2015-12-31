package cn.wecook.app.main.recommend;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.BitmapUtils;
import com.wecook.sdk.api.legacy.CookShowApi;
import com.wecook.sdk.api.legacy.MediaApi;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.ID;
import com.wecook.sdk.api.model.Media;
import com.wecook.sdk.api.model.Tags;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.policy.TagInputPolicy;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.AutoWrapLayout;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.features.message.MessageEventReceiver;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 晒厨艺发布界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/23/14
 */
public class CookShowPublishFragment extends BaseTitleFragment {

    public static final String EXTRA_PUBLISH_BITMAP = "extra_publish_bitmap";
    public static final String EXTRA_PUBLISH_FOOD = "extra_publish_food";
    public static final String EXTRA_PUBLISH_COOK_SHOW = "extra_publish_cook_show";

    private static final String TAG = "fragment-publish";

    private Bitmap mPublishBitmap;

    private Food mFood;

    private LoadingDialog mLoading;

    private AutoWrapLayout mAutoWrapLayout;
    private ViewGroup mInputTagsGroup;
    private EditText mTagEdit;
    private EditText mCommentEdit;
    private EditText mTitleEdit;
    private CookShow mCookShow;

    private TagInputPolicy mTagEditPolicy;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mPublishBitmap = bundle.getParcelable(EXTRA_PUBLISH_BITMAP);
            mFood = (Food) bundle.getSerializable(EXTRA_PUBLISH_FOOD);
            mCookShow = (CookShow) bundle.getSerializable(EXTRA_PUBLISH_COOK_SHOW);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cook_show_publish, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView image = (ImageView) view.findViewById(R.id.app_cook_show_publish_image);
        image.setImageBitmap(mPublishBitmap);

        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.app_title_bar_edit));
        titleBar.enableBottomDiv(false);

        TitleBar.ActionTextView publish = new TitleBar.ActionTextView(getContext(), "发布");
        publish.setTextColor(getResources().getColor(R.color.uikit_font_orange));
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPublish();
            }
        });
        titleBar.addActionView(publish);

        mCommentEdit = (EditText) view.findViewById(R.id.app_cook_show_publish_comment);

        mTitleEdit = (EditText) view.findViewById(R.id.app_cook_show_publish_title);
        if (mFood != null) {
            mTitleEdit.setText(mFood.title);
        }

        mTagEdit = (EditText) view.findViewById(R.id.app_cook_show_publish_tags);
        mAutoWrapLayout = (AutoWrapLayout) view.findViewById(R.id.app_cook_show_publish_tags_group);
        mInputTagsGroup = (ViewGroup) view.findViewById(R.id.app_cook_show_publish_input_tags);

        mTagEditPolicy = new TagInputPolicy(getContext(), mTagEdit, mAutoWrapLayout, mInputTagsGroup);
        mTagEditPolicy.setOnHotTagClickListener(new TagInputPolicy.OnHotTagClickListener() {
            @Override
            public void onClick(String tagName) {
                MobclickAgent.onEvent(getContext(), LogConstant.COOK_ACTION_TAG_SELECTED);
            }
        });
        mTagEditPolicy.initTagView(R.layout.view_cook_show_tag, R.id.app_cook_show_tag);
        mTagEditPolicy.initInputmethodAction(getString(R.string.app_button_title_add),
                getString(R.string.app_button_title_finish));

        if (mCookShow != null) {
            mCommentEdit.setText(mCookShow.getDescription());
            mTitleEdit.setText(mCookShow.getTitle());
            if (!mCookShow.getTags().isEmpty()) {
                updateTags(mCookShow);
            }
            ImageFetcher.asInstance().load(mCookShow.getImage(), image);
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();

        mTagEditPolicy.loadHotTags(1);
    }

    /**
     * 获得标签字符串
     *
     * @param cookShow
     * @return
     */
    private void updateTags(CookShow cookShow) {
        ApiModelList<Tags> tags = cookShow.getTags();
        for (int i = 0; i < (tags.getCountOfList() > 3 ? 3 : tags.getCountOfList()); i++) {
            mTagEditPolicy.addTagView(tags.getItem(i).getName());
        }
    }

    private int getTagCount() {
        return mTagEditPolicy.getTagCount();
    }

    /**
     * 上传照片并通知分享
     */
    private void requestPublish() {

        if (UserProperties.isLogin()) {

            if (!checkValidate()) {
                return;
            }
            mLoading = new LoadingDialog(getContext());
            mLoading.setText(R.string.app_tip_cook_show_loading);
            mLoading.cancelable(false);
            mLoading.show();

            // 重新编辑
            if (mCookShow != null) {
                requestUpdateCookshow(mCookShow);
            } else {
                if (!BitmapUtils.isRecycled(mPublishBitmap)) {
                    MediaApi.uploadSingleMedia(UserProperties.getUserId(), mPublishBitmap, new ApiCallback<Media>() {
                        @Override
                        public void onResult(Media result) {
                            if (result != null) {
                                if (result.available()) {
                                    requestCreateCookshow(result);
                                } else {
                                    ToastAlarm.show(result.getErrorMsg());
                                    mLoading.cancel();
                                }
                            } else {
                                ToastAlarm.show(R.string.app_error_no_reason);
                                mLoading.cancel();
                            }
                        }
                    });
                }
            }

        } else {
            startActivity(new Intent(getContext(), UserLoginActivity.class));
        }


    }

    /**
     * 检查数据是否合法
     *
     * @return
     */
    private boolean checkValidate() {

        mCommentEdit.clearFocus();
        mTitleEdit.clearFocus();
        mTagEdit.clearFocus();

        if (mTitleEdit.getText().length() == 0) {//菜名不能空
            ToastAlarm.show(R.string.app_tip_publish_title_empty);
            return false;
        }

        if (getTagCount() > 3) {//标签数量不大于3
            ToastAlarm.show(R.string.app_tip_publish_tag_count_limit);
            return false;
        }
        return true;
    }

    /**
     * 更新厨艺
     *
     * @param cookShow
     */
    private void requestUpdateCookshow(final CookShow cookShow) {
        final String desc = mCommentEdit.getText().toString();
        final String title = mTitleEdit.getText().toString();
        final String tags = getPublishTags();
        CookShowApi.updateCookShow(cookShow.getId(), title, desc, tags, new ApiCallback<ID>() {
            @Override
            public void onResult(ID result) {
                mLoading.cancel();
                LogGather.onEventCookShowPublish(result.available(), result.getErrorMsg());
                if (result.available()) {
                    cookShow.setUrl(result.url);
                    ToastAlarm.show(R.string.app_tip_cook_show_updated);
                    finishFragment();
                }
            }
        });
    }

    /**
     * 请求创建晒厨艺
     *
     * @param media
     */
    private void requestCreateCookshow(final Media media) {
        //上传图片成功,发布晒厨艺功能
        final String desc = mCommentEdit.getText().toString();
        final String title = mTitleEdit.getText().toString();
        final String tags = getPublishTags();
        CookShowApi.createCookShow(CookShowApi.TYPE_RECIPE, title, media.getId(),
                UserProperties.getUserId(), desc, tags, new ApiCallback<ID>() {
                    @Override
                    public void onResult(ID result) {
                        mLoading.cancel();
                        LogGather.onEventCookShowPublish(result.available(), result.getErrorMsg());
                        if (result.available()) {
                            //显示是否进行分享
                            CookShow cookShow = new CookShow();
                            cookShow.setId(result.id);
                            cookShow.setTitle(title);
                            cookShow.setImage(media.getUrl());
                            cookShow.setDescription(desc);
                            if (mFood != null) {
                                cookShow.setRecipeId(mFood.id);
                            }
                            cookShow.setUrl(result.url);
                            cookShow.setUser(UserProperties.getUser());
                            ThirdPortDelivery.share(getContext(), cookShow,
                                    getString(R.string.app_cook_show_publish_success));

                            Intent data = new Intent(MessageEventReceiver.ACTION_COOK_SHOW);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(CookShowApi.PARAM_COOK_SHOW, cookShow);
                            data.putExtras(bundle);
                            getContext().sendBroadcast(data, MessageEventReceiver.PERMISSION_EVENT);

                            finishAll();
                        } else {
                            ToastAlarm.show(result.getErrorMsg());
                        }
                    }
                }
        );
    }

    private String getPublishTags() {
        String publishTags = "";
        for (String tag : mTagEditPolicy.getInputTags()) {
            publishTags += tag + ",";
        }
        return publishTags;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BitmapUtils.clean(mPublishBitmap);
        if (mTagEditPolicy != null) {
            mTagEditPolicy.release();
            mTagEditPolicy = null;
        }

    }

}
