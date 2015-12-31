package com.wecook.sdk.policy;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CookShowApi;
import com.wecook.sdk.api.model.Tags;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.widget.AutoWrapLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签输入的策略
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/19/15
 */
public class TagInputPolicy {

    public static final int MAX_HOT_TAG_COUNT = 14;

    private AutoWrapLayout mHotTagsGroup;
    private ViewGroup mShowTagsGroup;
    private EditText mTags;

    private List<Tags> mHotTags;
    private List<String> mInputTags = new ArrayList<String>();
    private Map<String, View> mTagViews = new HashMap<String, View>();

    private Context mContext;

    private String mTagsHint;

    private int mTagLayoutId;
    private int mTextResId;
    private String mInputActionAdd;
    private String mInputActionFinish;

    private OnHotTagClickListener mHotTagClickListener;
    private OnHotTagLoadListener mHotTagLoadListener;

    /**
     * 标签策略
     *
     * @param tags   标签输入
     * @param hotTagsLayout  热门标签列表
     * @param showTagsLayout 显示已加入的标签
     */
    public TagInputPolicy(Context context, EditText tags, AutoWrapLayout hotTagsLayout, ViewGroup showTagsLayout) {
        mContext = context.getApplicationContext();
        mShowTagsGroup = showTagsLayout;
        mHotTagsGroup = hotTagsLayout;
        mTags = tags;

        mTagsHint = mTags.getHint().toString();

        mTags.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addTagView(mTags.getText().toString());
                    return true;
                }
                return false;
            }
        });

        mTags.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mTags.getText().length() == 0) {
                        if (mInputTags.size() > 0) {
                            String deleteTag = mInputTags.remove(mInputTags.size() - 1);
                            deleteTagView(deleteTag);
                            checkEditHint();
                        }
                    }
                }
                return false;
            }
        });

        mTags.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pre = s.subSequence(0, start).toString();
                String end = s.subSequence(start, start + count).toString();
                //输入空格也执行添加标签功能
                if (" ".equals(end)) {
                    addTagView(pre);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 更新热门标签
     */
    private void updateHotTags() {
        if (mHotTags != null && !mHotTags.isEmpty()) {
            mHotTagsGroup.removeAllViews();
            for (final Tags tag : mHotTags) {
                View tagView = createTagView(tag.getName());
                tagView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTags.requestFocus();
                        addTagView(tag.getName());
                        if (mHotTagClickListener != null) {
                            mHotTagClickListener.onClick(tag.getName());
                        }
                    }
                });
                mHotTagsGroup.addView(tagView);
            }
        }
    }

    /**
     * 检查标签Hint
     */
    private void checkEditHint() {

        if (mInputTags.isEmpty()) {
            mTags.setHint(mTagsHint);
        } else {
            mTags.setHint("");
            if (mInputTags.size() < 3) {
                mTags.setImeActionLabel(mInputActionAdd, EditorInfo.IME_ACTION_DONE);
            } else {
                mTags.setImeActionLabel(mInputActionFinish, EditorInfo.IME_ACTION_DONE);
            }
        }
    }

    /**
     * 添加标签
     *
     * @param inputTag
     */
    public void addTagView(String inputTag) {
        if (StringUtils.isEmpty(inputTag)) {
            checkEditHint();
            return;
        }
        inputTag = inputTag.replaceAll("#| ", "");

        if (StringUtils.isEmpty(inputTag)) {
            checkEditHint();
            return;
        }

        if (StringUtils.chineseLength(inputTag) > 6) {
//            ToastAlarm.show(R.string.app_tip_publish_tag_too_long);
            checkEditHint();
            return;
        }

        if (mInputTags.contains(inputTag)) {
//            ToastAlarm.show(R.string.app_tip_publish_tag_add_duplicate);
            checkEditHint();
            return;
        }

        if (mInputTags.size() >= 3) {
//            ToastAlarm.show(R.string.app_tip_publish_tag_add_full);
            checkEditHint();
            return;
        }

        mInputTags.add(inputTag);
        final View tag = createTagView(inputTag);
        final String finalInputTag = inputTag;
        tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTagView(finalInputTag);
                checkEditHint();
                if (mHotTagClickListener != null) {
                    mHotTagClickListener.onClick(finalInputTag);
                }
            }
        });
        mShowTagsGroup.addView(tag);
        mTags.setText("");
        mTagViews.put(inputTag, tag);
        if (mInputTags.size() >= 3) {
            KeyboardUtils.closeKeyboard(mContext, mTags);
        }
        checkEditHint();
    }

    /**
     * 删除标签
     *
     * @param tag
     */
    private void deleteTagView(String tag) {
        mInputTags.remove(tag);
        View tagView = mTagViews.remove(tag);
        if (tagView != null) {
            mShowTagsGroup.removeView(tagView);
        }
    }

    /**
     * 创建标签
     *
     * @param name
     * @return
     */
    private View createTagView(String name) {
        View view = View.inflate(mContext, mTagLayoutId, null);
        TextView tag = (TextView) view.findViewById(mTextResId);
        tag.setText(name);
        return view;
    }

    /**
     * 初始化标签View
     *
     * @param layout
     * @param textResId
     */
    public void initTagView(int layout, int textResId) {
        mTagLayoutId = layout;
        mTextResId = textResId;
    }

    /**
     * 初始化输入法的动作标签
     *
     * @param add
     * @param finish
     */
    public void initInputmethodAction(String add, String finish) {
        mInputActionAdd = add;
        mInputActionFinish = finish;
    }

    /**
     * 加载更多的热门标签
     */
    public void loadHotTags(int page) {
        CookShowApi.getCookShowHotTag(page, MAX_HOT_TAG_COUNT, new ApiCallback<ApiModelList<Tags>>() {
            @Override
            public void onResult(ApiModelList<Tags> result) {
                if (result.available()) {
                    mHotTags = result.getList();
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateHotTags();
                            if (mHotTagLoadListener != null) {
                                mHotTagLoadListener.onLoaded(mHotTags);
                            }
                        }
                    });
                }
            }
        });
    }

    public int getTagCount() {
        return mInputTags.size();
    }

    public void release() {
        if (mHotTags != null) {
            mHotTags.clear();
        }
        mTagViews.clear();
        mInputTags.clear();
    }

    public List<String> getInputTags() {
        return mInputTags;
    }

    public void setOnHotTagClickListener(OnHotTagClickListener listener) {
        mHotTagClickListener = listener;
    }

    public void setOnHotTagLoadListener(OnHotTagLoadListener listener) {
        mHotTagLoadListener = listener;
    }

    public interface OnHotTagClickListener {

        void onClick(String tagName);
    }

    public interface OnHotTagLoadListener {
        void onLoaded(List<Tags> hotTags);
    }
}
