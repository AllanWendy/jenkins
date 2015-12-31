package cn.wecook.app.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.alarm.DialogAlarm;

import cn.wecook.app.R;

/**
 * 确定对话框
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/31/15
 */
public class ConfirmDialog extends DialogAlarm {

    private View.OnClickListener mOkClick;
    private View.OnClickListener mCancelClick;

    private String mConfirmText;
    private String mCancelText;

    private String mTitle;
    private String mMessage;
    private String mConfirm;

    private boolean mIsShowConfirmButton = true;
    private boolean mIsShowCancelButton = true;
    private boolean mIsLeftHabit = true;

    private TextView confirm;
    private TextView cancel;

    public ConfirmDialog(Context context, int msgId) {
        super(context);
        mMessage = context.getString(msgId);
    }
    public ConfirmDialog(Context context, String message) {
        super(context);
        mMessage = message;
    }

    public ConfirmDialog setTitle(String title) {
        mTitle = title;
        return this;
    }

    public ConfirmDialog setMessage(String message) {
        mMessage = message;
        return this;
    }

    public ConfirmDialog setConfirm(String confirmText, View.OnClickListener clickListener) {
        mConfirmText = confirmText;
        mOkClick = clickListener;
        mIsShowConfirmButton = true;
        return this;
    }

    public ConfirmDialog setConfirm(View.OnClickListener clickListener) {
        mOkClick = clickListener;
        mIsShowConfirmButton = true;
        return this;
    }

    public ConfirmDialog setCancel(String cancelText, View.OnClickListener clickListener) {
        mCancelText = cancelText;
        mCancelClick = clickListener;
        mIsShowCancelButton = true;
        return this;
    }

    public ConfirmDialog setCancel(View.OnClickListener clickListener) {
        mCancelClick = clickListener;
        mIsShowCancelButton = true;
        return this;
    }

    public ConfirmDialog showCancel(boolean showCancel) {
        mIsShowCancelButton = showCancel;
        return this;
    }

    public ConfirmDialog showConfirm(boolean showConfirm) {
        mIsShowConfirmButton = showConfirm;
        return this;
    }

    public ConfirmDialog setActionHabit(boolean isLeft) {
        mIsLeftHabit = isLeft;
        return this;
    }

    public ConfirmDialog setConfirmText(String confirmText){
        mConfirmText = confirmText;
        return this;
    }

    @Override
    public View getView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm, parent, true);
    }

    @Override
    public void onViewCreated(View view) {
        TextView title = (TextView) view.findViewById(R.id.app_dialog_confirm_title);
        TextView message = (TextView) view.findViewById(R.id.app_dialog_confirm_message);
        confirm = (TextView) view.findViewById(R.id.app_dialog_confirm_ok);
        cancel = (TextView) view.findViewById(R.id.app_dialog_confirm_cancel);

        if (mIsLeftHabit) {
            cancel.setText(R.string.app_button_title_ok);
            confirm.setText(R.string.app_button_title_cancel);
        } else {
            confirm.setText(R.string.app_button_title_ok);
            cancel.setText(R.string.app_button_title_cancel);
        }

        if (StringUtils.isEmpty(mTitle)) {
            title.setVisibility(View.GONE);
        } else {
            title.setVisibility(View.VISIBLE);
            title.setText(mTitle);
        }

        message.setText(mMessage);

        if (!StringUtils.isEmpty(mConfirmText)) {
            if (mIsLeftHabit) {
                cancel.setText(mConfirmText);
            } else {
                confirm.setText(mConfirmText);
            }
        }
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mIsLeftHabit) {
                    if (mCancelClick != null) {
                        mCancelClick.onClick(v);
                    }
                } else {
                    if (mOkClick != null) {
                        mOkClick.onClick(v);
                    }
                }
            }
        });
        if (mIsLeftHabit) {
            if (mIsShowCancelButton) {
                cancel.setVisibility(View.VISIBLE);
            } else {
                cancel.setVisibility(View.GONE);
            }
        } else {
            if (mIsShowConfirmButton) {
                confirm.setVisibility(View.VISIBLE);
            } else {
                confirm.setVisibility(View.GONE);
            }
        }

        if (!StringUtils.isEmpty(mCancelText)) {
            if (mIsLeftHabit) {
                confirm.setText(mCancelText);
            } else {
                cancel.setText(mCancelText);
            }
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mIsLeftHabit) {
                    if (mOkClick != null) {
                        mOkClick.onClick(v);
                    }
                } else {
                    if (mCancelClick != null) {
                        mCancelClick.onClick(v);
                    }
                }
            }
        });
        if (mIsLeftHabit) {
            if (mIsShowConfirmButton) {
                confirm.setVisibility(View.VISIBLE);
            } else {
                confirm.setVisibility(View.GONE);
            }
        } else {
            if (mIsShowCancelButton) {
                cancel.setVisibility(View.VISIBLE);
            } else {
                cancel.setVisibility(View.GONE);
            }
        }

    }
}
