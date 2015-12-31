package cn.wecook.app.dialog;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.HttpUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Notice;
import com.wecook.uikit.alarm.DialogAlarm;

import java.util.HashSet;
import java.util.Set;

import cn.wecook.app.R;

/**
 * 公告
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/14
 */
public class NoticeListDialog extends DialogAlarm {

    public static final String IS_SHOW = "notice_id_show_state";

    private Notice mNotice;
    private ImageView mIcon;
    private ImageView mClose;
    private ImageView mImage;
    private ViewGroup mItemGroup;
    private int mIconResourceId;
    private boolean mOnlyShow;
    private View.OnClickListener mCloseListener;
    private ActionClick mActionLeft;
    private ActionClick mActionRight;
    private Button mActionLeftView;
    private Button mActionRightView;
    private View mDescLayout;
    private View mActionViewGroup;

    public static abstract class ActionClick {
        public Drawable getBackground() {
            return null;
        }

        public abstract String getTitle();

        public abstract View.OnClickListener getClick();
    }

    public NoticeListDialog(Context context, Notice notice) {
        super(context);
        mNotice = notice;
    }

    @Override
    public View getView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.dialog_notice, parent, true);
    }

    @Override
    public void onViewCreated(View view) {

        mIcon = (ImageView) view.findViewById(R.id.app_dialog_notice_icon);
        mItemGroup = (ViewGroup) view.findViewById(R.id.app_dialog_notice_item_group);
        mClose = (ImageView) view.findViewById(R.id.app_dialog_notice_close);
        mImage = (ImageView) view.findViewById(R.id.app_dialog_notice_image);
        mActionViewGroup = view.findViewById(R.id.app_dialog_notice_action_group);
        mActionLeftView = (Button) view.findViewById(R.id.app_dialog_notice_action_left);
        mActionRightView = (Button) view.findViewById(R.id.app_dialog_notice_action_right);
        mDescLayout = view.findViewById(R.id.app_dialog_notice_desc_layout);
    }

    public NoticeListDialog setDialogIcon(int resId) {
        mIconResourceId = resId;
        return this;
    }

    /**
     * 点击关闭按钮，就不再显示
     *
     * @param onlyShow
     * @return
     */
    public NoticeListDialog setDialogOnlyShow(boolean onlyShow) {
        mOnlyShow = onlyShow;
        return this;
    }

    public NoticeListDialog setActionClick(ActionClick left, ActionClick right) {
        mActionLeft = left;
        mActionRight = right;
        return this;
    }

    public NoticeListDialog setCloseClick(View.OnClickListener close) {
        mCloseListener = close;
        return this;
    }

    @Override
    public void show() {
        //没有显示才进行
        if (!isShown()) {
            super.show();

            if (mIcon != null) {
                if (mIconResourceId != 0) {
                    mIcon.setVisibility(View.VISIBLE);
                    mIcon.setImageResource(mIconResourceId);
                } else {
                    mIcon.setVisibility(View.INVISIBLE);
                }
            }

            if (mClose != null) {
                mClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCloseListener != null) {
                            mCloseListener.onClick(v);
                        }
                        setShow(!isShown());
                        dismiss();
                    }
                });
                mClose.setVisibility(mCloseListener != null ? View.VISIBLE : View.INVISIBLE);
            }

            if (mActionLeftView != null) {
                mActionLeftView.setVisibility(mActionLeft != null ? View.VISIBLE : View.GONE);
                if (mActionLeft != null) {
                    mActionViewGroup.setVisibility(View.VISIBLE);
                    mActionLeftView.setOnClickListener(mActionLeft.getClick());
                    mActionLeftView.setText(mActionLeft.getTitle());
                    if (mActionLeft.getBackground() !=  null) {
                        mActionLeftView.setBackgroundDrawable(mActionLeft.getBackground());
                    }
                }
            }

            if (mActionRightView != null) {
                mActionRightView.setVisibility(mActionRight != null ? View.VISIBLE : View.GONE);
                if (mActionRight != null) {
                    mActionViewGroup.setVisibility(View.VISIBLE);
                    mActionRightView.setOnClickListener(mActionRight.getClick());
                    mActionRightView.setText(mActionRight.getTitle());
                    if (mActionRight.getBackground() != null) {
                        mActionRightView.setBackgroundDrawable(mActionRight.getBackground());
                    }
                }
            }

            if (mNotice != null) {
                if (!StringUtils.isEmpty(mNotice.getImage())) {
                    showNoticeImage();
                } else if (mNotice.getNotes() != null && !mNotice.getNotes().isEmpty()) {
                    showNoticeList();
                } else {
                    showNoticeNormal();
                }
            }
        }
    }

    private void showNoticeNormal() {

        if (mDescLayout != null) {
            mDescLayout.setVisibility(View.VISIBLE);
            TextView title = (TextView) mDescLayout.findViewById(R.id.app_dialog_notice_note_title);
            TextView desc = (TextView) mDescLayout.findViewById(R.id.app_dialog_notice_note_desc);
            desc.setVisibility(View.VISIBLE);
            title.setText(mNotice.getTitle());
            title.setGravity(Gravity.CENTER);
            desc.setText(mNotice.getDesc());
            desc.setGravity(Gravity.CENTER);
        }

    }

    private void showNoticeImage() {

        if (mImage != null) {
            mImage.setVisibility(View.VISIBLE);
            if (StringUtils.parseInt(mNotice.getImage()) != 0) {
                mImage.setImageResource(StringUtils.parseInt(mNotice.getImage()));
            } else {
                ImageFetcher.asInstance().loadSimple(mNotice.getImage(), mImage);
            }
        }
    }

    private void showNoticeList() {
        View lastDivingView = null;
        for (Notice.Note note : mNotice.getNotes()) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_notice_note, null);
            TextView titleView = (TextView) view.findViewById(R.id.app_dialog_notice_note_title);
            ViewGroup subTitleGroup = (ViewGroup) view.findViewById(R.id.app_dialog_notice_note_item_group);
            lastDivingView = view.findViewById(R.id.app_dialog_notice_note_diving);
            mItemGroup.addView(view);
            titleView.setText(note.title);
            if (note.color != -1) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.OVAL);
                drawable.setColor(note.color);
                drawable.setSize(ScreenUtils.dip2px(8), ScreenUtils.dip2px(8));
                titleView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }

            if (note.items != null && !note.items.isEmpty()) {
                for (final Notice.NoteItem item : note.items) {
                    View itemView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_notice_note_item, null);
                    subTitleGroup.addView(itemView);
                    final TextView subTitle = (TextView) itemView.findViewById(R.id.app_dialog_notice_note_item_title);
                    subTitle.setText(item.name);
                    if (!StringUtils.isEmpty(item.icon)) {
                        if (HttpUtils.isValidHttpUri(item.icon)) {
                            ImageFetcher.asInstance().getBitmap(item.icon,
                                    ScreenUtils.dip2px(16), ScreenUtils.dip2px(16), new ImageFetcher.Callback<Bitmap>() {
                                        @Override
                                        public void callback(final Bitmap obj) {
                                            if (obj != null) {

                                                UIHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Drawable drawable = new BitmapDrawable(Resources.getSystem(), obj);
                                                        subTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                                                    }
                                                });
                                            }
                                        }
                                    });
                        } else if (StringUtils.parseInt(item.icon) != 0) {
                            subTitle.setCompoundDrawablesWithIntrinsicBounds(StringUtils.parseInt(item.icon), 0, 0, 0);
                        }

                    }
                }
            }
        }
        if (lastDivingView != null) {
            lastDivingView.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isShown() {
        if (mOnlyShow) {
            Set<String> ids = SharePreferenceProperties.get(IS_SHOW, new HashSet<String>());
            if (ids != null && ids.contains(mNotice.getId())) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    private void setShow(boolean show) {
        if (mOnlyShow) {
            Set<String> ids = SharePreferenceProperties.get(IS_SHOW, new HashSet<String>());
            if (ids == null) {
                ids = new HashSet<>();
            }
            if (show) {
                ids.add(mNotice.getId());
            } else {
                ids.remove(mNotice.getId());
            }
            SharePreferenceProperties.set(IS_SHOW, ids);
        }
    }
}
