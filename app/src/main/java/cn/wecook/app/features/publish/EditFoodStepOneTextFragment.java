package cn.wecook.app.features.publish;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodStep;
import com.wecook.uikit.fragment.BaseFragment;

import cn.wecook.app.R;

/**
 * 单一步骤
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/21/15
 */
public class EditFoodStepOneTextFragment extends BaseFragment{

    private static final int MAX_LIMIT_COUNT_TIP = 5;
    private static final int MAX_LIMIT_COUNT = 100;
    public static final String EXTRA_FOOD_STEP = "extra_food_step";
    private EditText mStepDesc;
    private TextView mStepDescLimit;
    private boolean mValidateDescLimit;

    private FoodStep mFoodStep;

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
        final View view = inflater.inflate(R.layout.fragment_edit_food_step_one_text, null);
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

        String desc = mFoodStep.getText();
        if (!StringUtils.isEmpty(desc)) {
            mStepDesc.setText(desc);
        }

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
