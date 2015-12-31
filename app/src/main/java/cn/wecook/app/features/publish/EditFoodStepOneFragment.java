package cn.wecook.app.features.publish;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.MediaStorePicker;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodStep;
import com.wecook.sdk.policy.FoodDetailEditorPolicy;
import com.wecook.uikit.fragment.BaseFragment;

import cn.wecook.app.R;
import cn.wecook.app.features.message.MessageEventReceiver;
import cn.wecook.app.features.pick.PickActivity;

/**
 * 单一步骤
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/21/15
 */
public class EditFoodStepOneFragment extends BaseFragment{

    private static final int MAX_LIMIT_COUNT_TIP = 5;
    private static final int MAX_LIMIT_COUNT = 100;
    public static final String EXTRA_FOOD_STEP = "extra_food_step";
    private ImageView mStepImageView;
    private EditText mStepDesc;
    private TextView mStepDescLimit;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MessageEventReceiver.ACTION_PICK_ONE_PIC.equals(intent.getAction())) {
                Logger.i("message", "EditFoodStepOneFragment .. onReceive.");
                MediaStorePicker.MediaImage image = intent.getParcelableExtra(MessageEventReceiver.PARAM_ONE_BITMAP);
                if (mFoodStep != null) {
                    FoodDetailEditorPolicy.get().cancelUploadImage(mFoodStep.getLocalImage());
                    mFoodStep.setLocalImage(image.path);
                }
                if (image != null && mStepImageView != null) {
                    ImageFetcher.asInstance().loadWithoutMemoryCache(image.path, mStepImageView);
                    FoodDetailEditorPolicy.get().uploadImage(image.path, mFoodStep);
                }

                unregisterReceiver();
            }

        }
    };
    private boolean mHasRegisterReceiver;
    private boolean mValidateDescLimit;

    private FoodStep mFoodStep;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mFoodStep = (FoodStep) bundle.getSerializable(EXTRA_FOOD_STEP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_edit_food_step_one, null);
        mStepImageView = (ImageView) view.findViewById(R.id.app_food_publish_step_image);
        mStepDesc = (EditText) view.findViewById(R.id.app_food_publish_step_brief);
        mStepDescLimit = (TextView) view.findViewById(R.id.app_food_publish_step_brief_limit);

        mStepDesc.addTextChangedListener(new TextWatcher() {
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
                    mFoodStep.setText(s.toString());
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //界面在ViewPage中计算出视图的宽高之后，再加载图片，这样保证图片铺满
        //onPreDrawListener也会出现问题，只能这样处理了...
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FileUtils.isExist(mFoodStep.getLocalImage())) {
                    ImageFetcher.asInstance().loadWithoutMemoryCache(mFoodStep.getLocalImage(), mStepImageView);
                } else if(!StringUtils.isEmpty(mFoodStep.getOnlineImageUrl())){
                    ImageFetcher.asInstance().load(mFoodStep.getOnlineImageUrl(), mStepImageView);
                }
            }
        }, 500);

        mStepImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isClosed = KeyboardUtils.closeKeyboard(getContext(), mStepDesc);
                if (!isClosed) {
                    registerReceiver(getContext());
                    Intent intent = new Intent(getContext(), PickActivity.class);
                    intent.putExtra(PickActivity.EXTRA_PICK_TYPE, PickActivity.PICK_ONE);
                    startActivity(intent);
                }
            }
        });

        String desc = mFoodStep.getText();
        if (!StringUtils.isEmpty(desc)) {
            mStepDesc.setText(desc);
        }

    }

    @Override
    public void onCardOut() {
        super.onCardOut();
        unregisterReceiver();
    }

    private void updateDescLimitCount(int count) {
        mValidateDescLimit = true;
        mStepDescLimit.setText("" + count);
        if (count <= MAX_LIMIT_COUNT_TIP && count > 0) {
            mStepDescLimit.setTextColor(getResources().getColor(R.color.uikit_font_dark));
        } else if (count < 0) {
            mStepDescLimit.setTextColor(getResources().getColor(R.color.uikit_font_orange));
            mValidateDescLimit = false;
        } else {
            mStepDescLimit.setText("");
        }
    }
}
