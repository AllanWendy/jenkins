package cn.wecook.app.main.dish;

import android.content.Context;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.sdk.api.model.Tags;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;

/**
 * 买菜帮手分类选项界面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/9
 */
public class DishCategoryView extends LinearLayout {

    private RecyclerView mCategoryListView;
    private DishOrderByView mOrderByChooseView;
    //    private ImageView mChangeIcon;
    private String mCurrentTag;
    private List<Tags> mTags;
    private OnCategoryOrderHandler mHandler;

    private DishCategoryView mRelationView;

    private CategoryAdapter mAdapter;

    public DishCategoryView(Context context) {
        super(context);
    }

    public DishCategoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DishCategoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCategoryListView = (RecyclerView) findViewById(R.id.app_view_category);
        mCategoryListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Logger.d("test", "onScrolled : x = " + dx + " dy = " + dy);
                if (mRelationView != null) {
                    mRelationView.mCategoryListView.scrollBy(dx, dy);
                }
            }
        });
        mTags = new ArrayList<>();
        mAdapter = new CategoryAdapter(mTags);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mCategoryListView.setLayoutManager(layoutManager);
        mCategoryListView.setAdapter(mAdapter);

        mOrderByChooseView = (DishOrderByView) findViewById(R.id.app_view_order);
        mOrderByChooseView.setOnItemClick(new AdapterViewCompat.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterViewCompat<?> parent, View view, int position, long id) {
                if (mHandler != null) {
                    mHandler.onOrderItemClick(position);
                }
            }
        });
//        mChangeIcon = (ImageView) findViewById(R.id.app_view_order_choose);
//        final Drawable drawable = mChangeIcon.getDrawable();
//        mChangeIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mOrderByChooseView.setVisibility(mOrderByChooseView.getVisibility() == View.VISIBLE
//                        ? View.GONE : View.VISIBLE);
//                drawable.setLevel(drawable.getLevel() == 0 ? 1 : 0);
//                if (mRelationView != null) {
//                    mRelationView.mChangeIcon.performClick();
//                }
//            }
//        });
    }

    public void updateView(List<Tags> categorys) {
        mTags.clear();
        mTags.addAll(categorys);
        mTags.get(0).getExtra().putBoolean("selected", true);
        mAdapter.notifyDataSetChanged();
        if (mRelationView != null) {
            mRelationView.updateView(categorys);
        }
    }


    public int getOrderType() {
        return mOrderByChooseView.getOrderType();
    }

    public String getOrderDirect() {
        return mOrderByChooseView.getOrderDirect();
    }


    public String getCurrentTag() {
        return mCurrentTag;
    }

    public void setCurrentTag(String tag) {
        mCurrentTag = tag;
        if (mRelationView != null) {
            mRelationView.setCurrentTag(tag);
        }
    }

    public void setHandler(OnCategoryOrderHandler mHandler) {
        this.mHandler = mHandler;
    }

    public void relationWith(DishCategoryView relationView) {
        mRelationView = relationView;
        if (relationView == null) {
            mOrderByChooseView.relationWith(null);
        } else {
            mOrderByChooseView.relationWith(relationView.mOrderByChooseView);
        }
    }

    public DishOrderByView getmOrderByChooseView() {
        return mOrderByChooseView;
    }

    public interface OnCategoryOrderHandler {
        void onOrderItemClick(int position);

        void onCategoryItemClick(Tags tag);
    }

    private class CategoryAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<Tags> mListData;

        public CategoryAdapter(List<Tags> data) {
            mListData = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_text_highlight_layout, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int position) {
            if (position == 0) {
                ScreenUtils.reMargin(viewHolder.itemView.findViewById(R.id.view_text_highlight_layout_id), ScreenUtils.dip2px(getContext(), 30), 0, ScreenUtils.dip2px(getContext(), 15), 0);
            } else if (getItemCount() > 1 && position == (getItemCount() - 1)) {
                ScreenUtils.reMargin(viewHolder.itemView.findViewById(R.id.view_text_highlight_layout_id), ScreenUtils.dip2px(getContext(), 15), 0, ScreenUtils.dip2px(getContext(), 30), 0);
            } else {
                ScreenUtils.reMargin(viewHolder.itemView.findViewById(R.id.view_text_highlight_layout_id), ScreenUtils.dip2px(getContext(), 15), 0, ScreenUtils.dip2px(getContext(), 15), 0);
            }
            final Tags tag = mListData.get(position);
            if (tag != null) {
                final TextView tagView = (TextView) viewHolder.itemView.findViewById(R.id.app_text_title);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mCurrentTag.equals(tag.getName())) {
                            mCurrentTag = tag.getName();

                            int p = viewHolder.getLayoutPosition();
                            for (int i = 0; i < mListData.size(); i++) {
                                Tags itemTag = mListData.get(i);
                                if (i == p) {
                                    itemTag.getExtra().putBoolean("selected", true);
                                } else {
                                    itemTag.getExtra().putBoolean("selected", false);
                                }
                            }

                            if (mHandler != null) {
                                mHandler.onCategoryItemClick(tag);
                            }

                            if (mRelationView != null) {

                                RecyclerView.ViewHolder vh = mRelationView.mCategoryListView
                                        .findViewHolderForLayoutPosition(p);
                                if (vh != null) {
                                    vh.itemView.performClick();
                                }
                            }
                            notifyDataSetChanged();
                        }
                    }
                });

                tagView.setText(tag.getName());
                viewHolder.itemView.setSelected(tag.getExtra().getBoolean("selected"));
            }

        }

        @Override
        public int getItemCount() {
            return mListData.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            layout = (LinearLayout) itemView.findViewById(R.id.view_text_highlight_layout_id);
        }
    }

}
