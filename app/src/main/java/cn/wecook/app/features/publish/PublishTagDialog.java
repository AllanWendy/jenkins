package cn.wecook.app.features.publish;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.wecook.common.utils.KeyboardUtils;
import com.wecook.sdk.api.model.Tags;
import com.wecook.sdk.policy.FoodDetailEditorPolicy;
import com.wecook.sdk.policy.TagInputPolicy;
import com.wecook.uikit.alarm.DialogAlarm;
import com.wecook.uikit.widget.AutoWrapLayout;

import java.util.List;

import cn.wecook.app.R;

/**
 * 发布菜谱的标签
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/3/28
 */
public class PublishTagDialog extends DialogAlarm {

    private View.OnClickListener mPublishClick;

    private TagInputPolicy mTagInputPolicy;
    private EditText mTags;
    private ViewGroup mShowTagsGroup;
    private AutoWrapLayout mHotTagsGroup;
    private int mCurrentPage = 1;

    public PublishTagDialog(Context context) {
        super(context);
    }

    @Override
    public View getView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.dialog_publish_tag, parent, true);
    }

    @Override
    public void onViewCreated(View view) {
        mTags = (EditText) view.findViewById(R.id.app_food_publish_tags);
        mShowTagsGroup = (ViewGroup) view.findViewById(R.id.app_food_publish_input_tags);
        mHotTagsGroup = (AutoWrapLayout) view.findViewById(R.id.app_food_publish_hot_tags);

        mTagInputPolicy = new TagInputPolicy(getContext(), mTags, mHotTagsGroup, mShowTagsGroup);
        mTagInputPolicy.initTagView(R.layout.view_publish_hot_tag, R.id.app_publish_hot_tag);
        mTagInputPolicy.initInputmethodAction(getContext().getString(R.string.app_button_title_add),
                getContext().getString(R.string.app_button_title_finish));
        mTagInputPolicy.setOnHotTagLoadListener(new TagInputPolicy.OnHotTagLoadListener() {
            @Override
            public void onLoaded(final List<Tags> hotTags) {
                View changeView = LayoutInflater.from(getContext()).inflate(R.layout.view_publish_hot_tag_change, null);
                changeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCurrentPage++;
                        if (hotTags.size() < TagInputPolicy.MAX_HOT_TAG_COUNT) {
                            mCurrentPage = 1;
                        }
                        mTagInputPolicy.loadHotTags(mCurrentPage);
                    }
                });
                mHotTagsGroup.addView(changeView);
            }
        });

        view.findViewById(R.id.app_dialog_publish_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtils.closeKeyboard(getContext(), mTags);
                dismiss();
            }
        });
        view.findViewById(R.id.app_dialog_publish_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FoodDetailEditorPolicy.get().setTags(mTagInputPolicy.getInputTags());
                KeyboardUtils.closeKeyboard(getContext(), mTags);
                dismiss();
                if (mPublishClick != null) {
                    mPublishClick.onClick(v);
                }
            }
        });
    }

    public PublishTagDialog setOnPublishListener(View.OnClickListener clickListener) {
        mPublishClick = clickListener;
        return this;
    }

    @Override
    protected void onDialogShowed() {
        super.onDialogShowed();
        mTagInputPolicy.loadHotTags(mCurrentPage);
    }

    @Override
    protected void onDialogCanceled() {
        super.onDialogCanceled();
        if (mTagInputPolicy != null) {
            mTagInputPolicy.release();
            mTagInputPolicy = null;
        }
        KeyboardUtils.closeKeyboard(getContext(), mTags);
    }

    @Override
    protected void onDialogDismissed() {
        super.onDialogDismissed();
        if (mTagInputPolicy != null) {
            mTagInputPolicy.release();
            mTagInputPolicy = null;
        }
        KeyboardUtils.closeKeyboard(getContext(), mTags);
    }
}
