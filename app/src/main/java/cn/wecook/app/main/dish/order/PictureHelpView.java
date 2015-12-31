package cn.wecook.app.main.dish.order;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.sdk.api.model.Image;

import java.util.List;

import cn.wecook.app.R;

/**
 * 评价图片展示及上传
 * Created by simon on 15/10/16.
 */
public class PictureHelpView extends LinearLayout {

    /**
     * 数据
     */
    private List<Image> data;
    /**
     * 子View 的宽度
     */
    private int childViewWidth = ScreenUtils.dip2px(60);
    /**
     * 子View的高度
     */
    private int childViewHeight = ScreenUtils.dip2px(60);
    /**
     * 子View之间的间距
     */
    private int childeViewRightMargin = ScreenUtils.dip2px(10);
    /**
     * 是否显示上传图片setting
     */
    private boolean isUpdatePicture;
    /**
     * 最大展示图片数量
     */
    private int countMax = 5;
    /**
     * 点击回调接口
     */
    private PictureHelpCallBack pictureHelpCallBack;

    public PictureHelpView(Context context) {
        this(context, null);
    }

    public PictureHelpView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PictureHelpView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 设置最大展示图片
     *
     * @param countMax
     */
    public void setCountMax(int countMax) {
        this.countMax = countMax;
    }

    /**
     * 初始化数据
     */
    private void init() {
        setOrientation(LinearLayout.HORIZONTAL);
    }

    /**
     * 设置需要显示的图片数据
     *
     * @param data
     */
    public void setData(List<Image> data) {
        this.data = data;
        notifyChanged();
    }

    /**
     *
     */
    private void notifyChanged() {
        removeAllViews();
        addChildViews();
        addSettingView();
        requestLayout();

    }

    public void setIsUpdatePicture(boolean isUpdatePicture) {
        this.isUpdatePicture = isUpdatePicture;
        notifyChanged();
    }

    private void addSettingView() {
        if (isUpdatePicture && (data == null || data.size() < countMax)) {
            addChildView(null, true);
            if (data == null || data.size() == 0) {
                //添加文案
                TextView textView = new TextView(getContext());
                LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, childViewHeight);
                layoutParams.leftMargin = ScreenUtils.dip2px(5);
                textView.setLayoutParams(layoutParams);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setText("晒单");
                textView.setTextSize(12);
                textView.setTextColor(getResources().getColor(R.color.uikit_aaa));
                addView(textView);
            }
        }
    }


    public void setDataAndIsUpdatePicture(List<Image> data, boolean isUpdatePicture) {
        this.isUpdatePicture = isUpdatePicture;
        this.data = data;
        notifyChanged();
    }


    /**
     * 添加子Views
     */
    private void addChildViews() {
        if (data != null && data.size() > 0) {
            for (int i = 0; i < data.size() && i < countMax; i++) {
                addChildView(data.get(i));
            }
        }
    }

    /**
     * 添加展示图片ziview
     */
    private void addChildView(Image image) {
        addChildView(image, false);
    }

    /**
     * 添加子View
     *
     * @param s
     */
    private void addChildView(Image s, boolean isSettingView) {
        final ImageView imageView = new ImageView(getContext());
        LayoutParams layoutParams = new LayoutParams(childViewWidth, childViewHeight);
        layoutParams.rightMargin = childeViewRightMargin;
        imageView.setLayoutParams(layoutParams);
        if (isSettingView) {
            //设置相机图片的id资源图片
            imageView.setBackgroundResource(R.drawable.app_shape_rect_side);
            imageView.setImageResource(R.drawable.app_bt_photo);
            imageView.setPadding(ScreenUtils.dip2px(17), ScreenUtils.dip2px(17), ScreenUtils.dip2px(17), ScreenUtils.dip2px(17));
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pictureHelpCallBack != null) {
                        pictureHelpCallBack.callback(PictureHelpView.this.indexOfChild(v), PictureHelpView.this, data, true);
                    }
                }
            });
        } else {
            ImageFetcher.asInstance().load(s.getShowSmallImagePath(), imageView);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pictureHelpCallBack != null) {
                        pictureHelpCallBack.callback(PictureHelpView.this.indexOfChild(v), PictureHelpView.this, data, false);
                    }
                }
            });
        }
        addView(imageView);
    }


    @Override
    public void removeAllViews() {
        super.removeAllViews();
        reset();
    }

    /**
     * 重置数据
     */
    private void reset() {
    }

    /**
     * 设置回调接口
     *
     * @param pictureHelpCallBack
     */
    public void setPictureHelpCallBack(PictureHelpCallBack pictureHelpCallBack) {
        this.pictureHelpCallBack = pictureHelpCallBack;

    }

    /**
     * 回调接口
     */
    public interface PictureHelpCallBack {
        void callback(int position, View parent, List<Image> data, boolean isUpdatePicture);
    }
}
