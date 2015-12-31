package cn.wecook.app.features.publish;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.MediaStorePicker;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.sdk.policy.FoodDetailEditorPolicy;
import com.wecook.uikit.fragment.BaseFragment;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ListActionDialog;
import cn.wecook.app.features.message.MessageEventReceiver;
import cn.wecook.app.features.pick.PickActivity;

/**
 * 编辑封面和描述
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/21/15
 */
public class EditFoodStepTitleFragment extends BaseFragment {

    private static final int MAX_LIMIT_COUNT = 120;
    private static final int MAX_LIMIT_COUNT_TIP = 5;

    private ImageView mFoodCover;
    private TextView mFoodTags;
    private TextView mFoodName;
    private TextView mFoodNameEditor;
    private TextView mFoodDifficulty;
    private TextView mFoodTime;
    private EditText mFoodDesc;
    private TextView mFoodDescCount;
    private EditText mFoodTips;
    private TextView mFoodTipsCount;

    private boolean mInEditorTitle;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MessageEventReceiver.ACTION_PICK_ONE_PIC.equals(intent.getAction())) {
                Logger.i("message", "EditFoodStepTitleFragment .. onReceive.");
                MediaStorePicker.MediaImage image = intent.getParcelableExtra(MessageEventReceiver.PARAM_ONE_BITMAP);
                if (image != null && mFoodCover != null) {
                    ImageFetcher.asInstance().load(image.path, mFoodCover);
                    FoodDetailEditorPolicy.get().cancelUploadImage(FoodDetailEditorPolicy.get().getLocalImage());
                    FoodDetailEditorPolicy.get().setLocalImage(image.path);
                    FoodDetailEditorPolicy.get().uploadImage(image.path,
                            FoodDetailEditorPolicy.get().getFoodRecipe());

                }

                unregisterReceiver();
            }

        }
    };
    private boolean mHasRegisterReceiver;
    private boolean mValidateDescLimit;
    private boolean mValidateTipsLimit;


    private void registerReceiver(Context context) {
        mHasRegisterReceiver = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageEventReceiver.ACTION_PICK_ONE_PIC);
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, filter);
    }

    private void unregisterReceiver() {
        if (mHasRegisterReceiver) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_edit_food_step_title, null);
        mFoodCover = (ImageView) view.findViewById(R.id.app_food_publish_step_food_cover);
        mFoodTags = (TextView) view.findViewById(R.id.app_food_publish_step_food_tags);
        mFoodName = (TextView) view.findViewById(R.id.app_food_publish_step_food_name);
        mFoodNameEditor = (EditText) view.findViewById(R.id.app_food_publish_step_food_name_edit);
        mFoodDifficulty = (TextView) view.findViewById(R.id.app_food_publish_step_food_difficulty);
        mFoodTime = (TextView) view.findViewById(R.id.app_food_publish_step_food_time);
        mFoodDesc = (EditText) view.findViewById(R.id.app_food_publish_step_food_desc);
        mFoodDescCount = (TextView) view.findViewById(R.id.app_food_publish_step_food_desc_limit);
        mFoodTips = (EditText) view.findViewById(R.id.app_food_publish_step_food_tips);
        mFoodTipsCount = (TextView) view.findViewById(R.id.app_food_publish_step_food_tips_limit);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String onlineImage = FoodDetailEditorPolicy.get().getImage();
        String localImage = FoodDetailEditorPolicy.get().getLocalImage();

        if (FileUtils.isExist(localImage)) {
            ImageFetcher.asInstance().loadWithoutMemoryCache(localImage, mFoodCover);
        } else if (!StringUtils.isEmpty(onlineImage)) {
            ImageFetcher.asInstance().load(onlineImage, mFoodCover);
        }

        mFoodCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean resultDesc = KeyboardUtils.closeKeyboard(getContext(), mFoodDesc);
                boolean resultTips = KeyboardUtils.closeKeyboard(getContext(), mFoodTips);
                if (!resultDesc && !resultTips) {
                    registerReceiver(getContext());
                    Intent intent = new Intent(getContext(), PickActivity.class);
                    intent.putExtra(PickActivity.EXTRA_PICK_TYPE, PickActivity.PICK_ONE);
                    startActivity(intent);
                }
            }
        });

        mFoodNameEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mInEditorTitle = false;
                    updateFoodNameEditor();
                }
            }
        });

        updateNameAndTags();
        mFoodName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInEditorTitle = true;
                updateFoodNameEditor();
            }
        });

        String difficulty = FoodDetailEditorPolicy.get().getDifficulty();
        if (!StringUtils.isEmpty(difficulty)) {
            mFoodDifficulty.setText(getString(R.string.app_food_publish_add_difficulty_, difficulty));
        }

        mFoodDifficulty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ListActionDialog(getContext(), FoodRecipe.DIFFICULTY_LEVEL,
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                mFoodDifficulty.setText(getString(R.string.app_food_publish_add_difficulty_,
                                        FoodRecipe.DIFFICULTY_LEVEL[position]));
                                FoodDetailEditorPolicy.get().setDifficulty(FoodRecipe.DIFFICULTY_LEVEL[position]);
                            }
                        }
                ).show();
            }
        });

        String time = FoodDetailEditorPolicy.get().getTime();
        if (!StringUtils.isEmpty(time)) {
            mFoodTime.setText(getString(R.string.app_food_publish_add_time_, time));
        }
        mFoodTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ListActionDialog(getContext(), FoodRecipe.TIME_LEVEL,
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                mFoodTime.setText(getString(R.string.app_food_publish_add_time_,
                                        FoodRecipe.TIME_LEVEL[position]));
                                FoodDetailEditorPolicy.get().setTime(FoodRecipe.TIME_LEVEL[position]);
                            }
                        }
                ).show();
            }
        });

        mFoodDesc.setText(FoodDetailEditorPolicy.get().getDescription());
        mFoodDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double count = StringUtils.chineseLength(s.toString());
                updateDescLimitCount(MAX_LIMIT_COUNT - (int) count);
                if (mValidateDescLimit) {
                    FoodDetailEditorPolicy.get().setDescription(s.toString());
                }
            }
        });

        mFoodTips.setText(FoodDetailEditorPolicy.get().getTips());
        mFoodTips.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double count = StringUtils.chineseLength(s.toString());
                updateTipsLimitCount(MAX_LIMIT_COUNT - (int) count);

                if (mValidateTipsLimit) {
                    FoodDetailEditorPolicy.get().setTips(s.toString());
                }
            }
        });
    }

    private void updateFoodNameEditor() {
        mFoodName.setVisibility(mInEditorTitle ? View.INVISIBLE : View.VISIBLE);
        mFoodNameEditor.setVisibility(mInEditorTitle ? View.VISIBLE : View.INVISIBLE);
        if (mInEditorTitle) {
            mFoodNameEditor.setText(mFoodName.getText());
            mFoodNameEditor.requestFocus();
        } else {
            mFoodNameEditor.clearFocus();
            mFoodName.setText(mFoodNameEditor.getText());
            FoodDetailEditorPolicy.get().setTitle(mFoodNameEditor.getText().toString());
            if (!StringUtils.isEmpty(mFoodName.getText().toString())) {
                mFoodName.setHint("");
            } else {
                mFoodName.setHint(R.string.app_food_publish_title);
            }
        }
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        updateNameAndTags();
    }


    @Override
    public void onCardOut() {
        super.onCardOut();
        unregisterReceiver();
    }

    protected void updateNameAndTags() {
        String tags = FoodDetailEditorPolicy.get().getTags();
        if (!StringUtils.isEmpty(tags)) {
            if (mFoodTags != null) {
                mFoodTags.setText(tags);
            }
        }

        String title = FoodDetailEditorPolicy.get().getTitle();
        if (!StringUtils.isEmpty(title)) {
            if (mFoodName != null) {
                mFoodName.setText(title);
            }
        }
    }

    private void updateDescLimitCount(int count) {
        mValidateDescLimit = true;
        mFoodDescCount.setText("" + count);
        if (count <= MAX_LIMIT_COUNT_TIP && count > 0) {
            mFoodDescCount.setTextColor(getResources().getColor(R.color.uikit_font_dark));
        } else if (count < 0) {
            mFoodDescCount.setTextColor(getResources().getColor(R.color.uikit_font_orange));
            mValidateDescLimit = false;
        } else {
            mFoodDescCount.setText("");
        }
    }

    private void updateTipsLimitCount(int count) {
        mValidateTipsLimit = true;
        mFoodTipsCount.setText("" + count);
        if (count <= MAX_LIMIT_COUNT_TIP && count > 0) {
            mFoodTipsCount.setTextColor(getResources().getColor(R.color.uikit_font_dark));
        } else if (count < 0) {
            mFoodTipsCount.setTextColor(getResources().getColor(R.color.uikit_font_orange));
            mValidateTipsLimit = false;
        } else {
            mFoodTipsCount.setText("");
        }
    }
}
