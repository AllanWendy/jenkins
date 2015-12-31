package cn.wecook.app.features.debug;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.wecook.common.utils.MorseCodeUtils;

import cn.wecook.app.R;

/**
 * 摩斯电码
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/12/15
 */
public class MorseCodeDebugFragment extends BaseDebugFragment {

    private EditText mLongTime;
    private EditText mShortTime;
    private EditText mSplitTime;
    private EditText mWaitTime;
    private EditText mText;
    private TextView mCode;
    private TextView mPlainText;
    private MorseCodeUtils.MorseCodeGenerator mGenerator;


    @Override
    public View onCreateInnerView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.fragment_debug_morse, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mText = (EditText) view.findViewById(R.id.app_debug_text);
        mLongTime = (EditText) view.findViewById(R.id.app_debug_long_time);
        mShortTime = (EditText) view.findViewById(R.id.app_debug_short_time);
        mSplitTime = (EditText) view.findViewById(R.id.app_debug_split_time);
        mWaitTime = (EditText) view.findViewById(R.id.app_debug_wait_time);
        mCode = (TextView) view.findViewById(R.id.app_debug_code);
        mPlainText = (TextView) view.findViewById(R.id.app_debug_plaintext);

        mLongTime.setText(MorseCodeUtils.LONG_TIME + "");
        mShortTime.setText(MorseCodeUtils.SHORT_TIME + "");
        mSplitTime.setText(MorseCodeUtils.SPLIT_TIME + "");
        mWaitTime.setText(MorseCodeUtils.WAIT_TIME + "");

        view.findViewById(R.id.app_debug_action_vibrate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MorseCodeUtils.setLongTime(Long.parseLong(mLongTime.getText().toString()));
                MorseCodeUtils.setShortTime(Long.parseLong(mShortTime.getText().toString()));
                MorseCodeUtils.setSplitTime(Long.parseLong(mSplitTime.getText().toString()));
                MorseCodeUtils.setWaitTime(Long.parseLong(mWaitTime.getText().toString()));
                MorseCodeUtils.vibrateMorseCode(getContext(), mText.getText().toString());
                mCode.setText(MorseCodeUtils.translateToCode(mText.getText().toString()));
            }
        });

        view.findViewById(R.id.app_debug_action_morse).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MorseCodeUtils.setLongTime(Long.parseLong(mLongTime.getText().toString()));
                MorseCodeUtils.setShortTime(Long.parseLong(mShortTime.getText().toString()));
                MorseCodeUtils.setSplitTime(Long.parseLong(mSplitTime.getText().toString()));
                MorseCodeUtils.setWaitTime(Long.parseLong(mWaitTime.getText().toString()));
                if (mGenerator == null) {
                    mGenerator = new MorseCodeUtils.MorseCodeGenerator();
                    mGenerator.start();
                }

                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mGenerator.clickDown(getContext());
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        mGenerator.clickUp(getContext());
                        break;
                }

                mPlainText.setText(mGenerator.getCode());
                return false;
            }
        });
        mPlainText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mPlainText.setText("");
                mGenerator.start();
                return true;
            }
        });
    }

    @Override
    public String getTitle() {
        return "摩斯码测试";
    }
}
