package cn.wecook.app.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.alarm.DialogAlarm;

import cn.wecook.app.R;

/**
 * 加载对话框
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/3/14
 */
public class LoadingDialog extends DialogAlarm {

    private int mDrawable;
    private String mTextMessage;
    private int mTextMessageId;

    public LoadingDialog(Context context) {
        super(context, R.style.UIKit_LoadingDialog);
    }

    public LoadingDialog setDrawable(int drawable) {
        mDrawable = drawable;
        return this;
    }

    public LoadingDialog setText(int msg) {
        mTextMessageId = msg;
        return this;
    }

    public LoadingDialog setText(String msg) {
        mTextMessage = msg;
        return this;
    }

    @Override
    public View getView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.dialog_loading_text, parent, true);
    }

    @Override
    public void onViewCreated(View view) {
        TextView textView = (TextView) view.findViewById(R.id.app_dialog_loading_text);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.app_dialog_loading_progressbar);
        if (textView != null) {
            if (mTextMessageId != 0) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(mTextMessageId);
            } else if(!StringUtils.isEmpty(mTextMessage)){
                textView.setVisibility(View.VISIBLE);
                textView.setText(mTextMessage);
            }
        }

        if (progressBar != null) {
            if (mDrawable != 0) {
                progressBar.setIndeterminateDrawable(getContext().getResources().getDrawable(mDrawable));
            }
        }
    }
}
