package cn.wecook.app.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.alarm.DialogAlarm;

import cn.wecook.app.R;

/**
 * 分享对话框
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/3/14
 */
public class ShareDialog extends DialogAlarm {

    private View mWechat;

    private View mWeblog;

    private View mWeChatFriend;

    private View mCancel;

    private TextView mTitleView;

    private View mTitleDivView;

    private CharSequence mTitle;

    private boolean mOnlyWeixin;

    private OnPositionClickListener mPositionClick;

    public ShareDialog(Context context) {
        this(context, "");
    }

    public ShareDialog(Context context, CharSequence title) {
        this(context, title, false);
    }

    public ShareDialog(Context context, CharSequence title, boolean onlyWeixin) {
        super(context, R.style.UIKit_Dialog_Fixed);
        mTitle = title;
        mOnlyWeixin = onlyWeixin;
    }

    public ShareDialog(Context context, Object noAnim) {
        this(context, "", noAnim);
    }

    public ShareDialog(Context context, CharSequence title, Object noAnim) {
        this(context, title, false, noAnim);
    }

    public ShareDialog(Context context, CharSequence title, boolean onlyWeixin, Object noAnim) {
        super(context, R.style.UIKit_Dialog_NO_ANIMATION);
        mTitle = title;
        mOnlyWeixin = onlyWeixin;
    }

    @Override
    public View getView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.dialog_share, parent, true);
    }

    @Override
    public void onViewCreated(View view) {
        mTitleView = (TextView) view.findViewById(R.id.app_share_title);
        mWechat = view.findViewById(R.id.app_share_wechat);
        mTitleDivView = view.findViewById(R.id.app_share_title_div);
        mWeChatFriend = view.findViewById(R.id.app_share_wechat_friends);
        mWeblog = view.findViewById(R.id.app_share_weblog);
        if (mOnlyWeixin) {
            mWeblog.setVisibility(View.GONE);
        }

        if (mTitle != null && !StringUtils.isEmpty(mTitle.toString())) {
            mTitleView.setText(mTitle);
            mTitleView.setVisibility(View.VISIBLE);
            mTitleDivView.setVisibility(View.VISIBLE);
        } else {
            mTitleView.setVisibility(View.GONE);
            mTitleDivView.setVisibility(View.GONE);
        }

        mWechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPositionClick != null) {
                    mPositionClick.onClick(PlatformManager.PLATFORM_WECHAT);
                }
                dismiss();
            }
        });
        mWeChatFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPositionClick != null) {
                    mPositionClick.onClick(PlatformManager.PLATFORM_WECHAT_FRIENDS);
                }
                dismiss();
            }
        });
        mWeblog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPositionClick != null) {
                    mPositionClick.onClick(PlatformManager.PLATFORM_WEBLOG);
                }
                dismiss();
            }
        });

    }

    public void setOnPositionClickListener(OnPositionClickListener listener) {
        mPositionClick = listener;
    }

    public interface OnPositionClickListener {
        public void onClick(int platformType);
    }
}
