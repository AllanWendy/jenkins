package cn.wecook.app.main.recommend.card;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.sdk.api.model.Category;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.view.BaseView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;

/**
 * Created by LK on 2015/8/29.
 */
public class ClassifyView extends BaseView<List<Category>> {
    /**
     * 是否显示图片
     */
    private boolean flagShowImgs = true;
    /**
     * 是否显示文字
     */
    private boolean flagShowTexts = true;
    /**
     * 是否显示下划线
     */
    private boolean flagShowLines = false;
    /**
     * 是否显示底部下划线
     */
    private boolean flagShowBottomLine = true;
    /**
     * 是否显示点击效果
     */
    private boolean flagShowSelector = true;
    private int firstVisityItem = -1;


    //    private BaseFragment mFragment;
    private List<Category> mCategories;
    private List<LinearLayout> mLayouts = new ArrayList<>();
    private List<ImageView> mImageViews = new ArrayList<>();
    private List<TextView> mTextViews = new ArrayList<>();
    private List<View> lines = new ArrayList<>();
    private LinearLayout mLayoutLines;
    private View line;
    private int[] default_img = new int[]{R.drawable.app_default_food_img_11, R.drawable.app_default_food_img_22, R.drawable.app_default_food_img_33, R.drawable.app_default_food_img_44, R.drawable.app_default_food_img_55};

    private OnLayoutItemClick mOnLayoutItemClick = null;


    public ClassifyView(BaseFragment fragment) {
        super(fragment);
    }

    public ClassifyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray;
        typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClassifyView);
        this.flagShowImgs = typedArray.getBoolean(R.styleable.ClassifyView_flagShowImgs, true);
        this.flagShowTexts = typedArray.getBoolean(R.styleable.ClassifyView_flagShowTexts, true);
        this.flagShowLines = typedArray.getBoolean(R.styleable.ClassifyView_flagShowLines, false);
        this.firstVisityItem = typedArray.getInt(R.styleable.ClassifyView_firstVisityItem, -1);
        typedArray.recycle();
    }

    public boolean isFlagShowImgs() {
        return flagShowImgs;
    }

    /**
     * 设置是否显示图片
     *
     * @param flagShowImgs
     */
    public void setFlagShowImgs(boolean flagShowImgs) {
        this.flagShowImgs = flagShowImgs;
    }

    public boolean isFlagShowLines() {
        return flagShowLines;
    }

    /**
     * 是否显示下划线
     *
     * @param flagShowLines
     */
    public void setFlagShowLines(boolean flagShowLines) {
        this.flagShowLines = flagShowLines;
    }

    public boolean isFlagShowTexts() {
        return flagShowTexts;
    }

    /**
     * 是否显示文本
     *
     * @param flagShowTexts
     */
    public void setFlagShowTexts(boolean flagShowTexts) {
        this.flagShowTexts = flagShowTexts;
    }

    public boolean isFlagShowBottomLine() {
        return flagShowBottomLine;
    }

    /**
     * 是否显示底部线
     *
     * @param flagShowBottomLine
     */
    public void setFlagShowBottomLine(boolean flagShowBottomLine) {
        this.flagShowBottomLine = flagShowBottomLine;
    }

    public boolean isFlagShowSelector() {
        return flagShowSelector;
    }

    /**
     * 显示5.0点击效果
     *
     * @param flagShowSelector
     */
    public void setFlagShowSelector(boolean flagShowSelector) {
        this.flagShowSelector = flagShowSelector;
    }

    @Override
    public void loadLayout(int layoutId, List<Category> categories, boolean update) {
        super.loadLayout(layoutId, data, update);
    }

    public void loadLayout(List<Category> categories) {
        this.loadLayout(categories, true);
    }

    public void loadLayout(List<Category> categories, boolean update) {
        mCategories = categories;
        super.loadLayout(R.layout.view_recommend_classify, categories, update);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewByIds();
    }


    private void findViewByIds() {
        mLayouts.add((LinearLayout) this.findViewById(R.id.app_classify_ll1));
        mLayouts.add((LinearLayout) this.findViewById(R.id.app_classify_ll2));
        mLayouts.add((LinearLayout) this.findViewById(R.id.app_classify_ll3));
        mLayouts.add((LinearLayout) this.findViewById(R.id.app_classify_ll4));
        mLayouts.add((LinearLayout) this.findViewById(R.id.app_classify_ll5));

        mImageViews.add((ImageView) this.findViewById(R.id.app_classify_iv_1));
        mImageViews.add((ImageView) this.findViewById(R.id.app_classify_iv_2));
        mImageViews.add((ImageView) this.findViewById(R.id.app_classify_iv_3));
        mImageViews.add((ImageView) this.findViewById(R.id.app_classify_iv_4));
        mImageViews.add((ImageView) this.findViewById(R.id.app_classify_iv_5));

        mTextViews.add((TextView) this.findViewById(R.id.app_classify_tv_1));
        mTextViews.add((TextView) this.findViewById(R.id.app_classify_tv_2));
        mTextViews.add((TextView) this.findViewById(R.id.app_classify_tv_3));
        mTextViews.add((TextView) this.findViewById(R.id.app_classify_tv_4));
        mTextViews.add((TextView) this.findViewById(R.id.app_classify_tv_5));

        lines.add(this.findViewById(R.id.view_recommend_classify_line1));
        lines.add(this.findViewById(R.id.view_recommend_classify_line2));
        lines.add(this.findViewById(R.id.view_recommend_classify_line3));
        lines.add(this.findViewById(R.id.view_recommend_classify_line4));
        lines.add(this.findViewById(R.id.view_recommend_classify_line5));
        mLayoutLines = (LinearLayout) this.findViewById(R.id.view_recommend_classify_line);
        line = this.findViewById(R.id.line);
    }


    /**
     * 设置数据
     */
    private void setDate() {
        if (null != mCategories) {
            for (int i = 0; i < 5; i++) {
                final int position = i;
                final Category category = mCategories.get(position);
                //设置图片
                ImageFetcher.asInstance().load(category.getImage(), mImageViews.get(position), default_img[position]);
                //设置文字
                final String titleString = category.getTitle();
                mTextViews.get(position).setText(titleString);
                //监听
                mLayouts.get(position).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mOnLayoutItemClick) {
                            changeBtnStatus(position);
                            mOnLayoutItemClick.OnClickListener(position, category);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void updateView(List<Category> categories) {
        super.updateView(categories);
        mCategories = categories;
        initViewStat();
        setDate();
    }

    /**
     * 初始化控件状态
     */
    public void initViewStat() {
        for (int i = 0; i < 5; i++) {
            mImageViews.get(i).setVisibility(flagShowImgs ? View.VISIBLE : View.GONE);
            mTextViews.get(i).setVisibility(flagShowTexts ? View.VISIBLE : View.GONE);
            if (!flagShowSelector) {
                mLayouts.get(i).setBackgroundResource(R.color.uikit_white);
            }
        }
        mLayoutLines.setVisibility(flagShowLines ? View.VISIBLE : View.GONE);
        line.setVisibility(flagShowBottomLine ? View.VISIBLE : View.GONE);
        changeBtnStatus(firstVisityItem);
    }

    public void setOnLayoutItemClick(OnLayoutItemClick l) {
        if (null != l) {
            mOnLayoutItemClick = l;
        }
    }

    /**
     * 设置默认选中项(0-4)
     *
     * @param firstVisityItem
     */
    public void setFirstVisityItem(int firstVisityItem) {
        if (firstVisityItem < 0 || firstVisityItem > 4) return;

        this.firstVisityItem = firstVisityItem;
    }

    public void changeBtnStatus(int position) {
        if (!flagShowLines || firstVisityItem < 0 || firstVisityItem > 4) return;
        for (int i = 0; i < 5; i++) {
            lines.get(i).setVisibility(View.INVISIBLE);
            mTextViews.get(i).setTextColor(getResources().getColor(R.color.uikit_777));
        }
        lines.get(position).setVisibility(View.VISIBLE);
        mTextViews.get(position).setTextColor(getResources().getColor(R.color.uikit_orange));
    }

    public interface OnLayoutItemClick {
        void OnClickListener(int position, Category category);
    }

}

