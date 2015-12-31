package cn.wecook.app.main.dish.restaurant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.api.model.CommentCount;
import com.wecook.sdk.api.model.Image;
import com.wecook.sdk.api.model.User;
import com.wecook.uikit.fragment.ApiModelListFragment;
import com.wecook.uikit.widget.EmptyView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.features.picture.MultiPictureActivity;
import cn.wecook.app.main.dish.DishDetailFragment;
import cn.wecook.app.main.dish.order.PictureHelpView;

/**
 * 菜品评论列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/13
 */
public class DishRestaurantCommentFragment extends ApiModelListFragment<Comment> {

    public static final String EXTRA_RESTAURANT_ID = "extra_restaurant_id";
    public static final String EXTRA_DISH_ID = "extra_dish_id";

    private static final int COMMENT_TYPE_ALL = 0;
    private static final int COMMENT_TYPE_GOOD = 1;
    private static final int COMMENT_TYPE_MIDDLE = 2;
    private static final int COMMENT_TYPE_BAD = 3;

    private static final int TYPE_RESTAURANT = 0;
    private static final int TYPE_DISH = 1;

    private CommentCount mCommentCount;
    private int mCommentType = COMMENT_TYPE_ALL;

    private TextView mCommentAll;
    private TextView mCommentGood;
    private TextView mCommentMiddle;
    private TextView mCommentBad;

    private LoadingDialog mLoadingDialog;
    private TextView mCommentAvg;
    private LinearLayout mCommentAvgLayout;
    private TextView mCommentDelivery;
    private LinearLayout mCommentDeliveryLayout;
    private TextView mCommentDishes;
    private LinearLayout mCommentDishesLayout;
    private View mCommentDeliveryLine;
    private View mCommentDishesLine;
    private int type;
    private String mId;
    private String mTitle;
    private View mCoreLayout;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String mRestaurantId = bundle.getString(EXTRA_RESTAURANT_ID);
            if (mRestaurantId != null && !StringUtils.isEmpty(mRestaurantId)) {
                type = TYPE_RESTAURANT;
                mId = mRestaurantId;
            }
            String mDishId = bundle.getString(EXTRA_DISH_ID);
            if (mDishId != null && !StringUtils.isEmpty(mDishId)) {
                type = TYPE_DISH;
                mId = mDishId;
            }
            mTitle = bundle.getString(EXTRA_DISH_ID);
        }
        if (mTitle == null || StringUtils.isEmpty(mTitle)) {
            setTitle("餐厅评价");
        } else {
            setTitle("菜品评价");
        }
        mLoadingDialog = new LoadingDialog(getContext());
        mLoadingDialog.cancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.fragment_restaurant_comment_list, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCommentAll = (TextView) view.findViewById(R.id.app_restaurant_comment_all);
        mCommentGood = (TextView) view.findViewById(R.id.app_restaurant_comment_good);
        mCommentMiddle = (TextView) view.findViewById(R.id.app_restaurant_comment_middle);
        mCommentBad = (TextView) view.findViewById(R.id.app_restaurant_comment_bad);
        //评分总布局
        mCoreLayout = view.findViewById(R.id.app_dish_comment_core_layout);
        mCoreLayout.setVisibility(View.GONE);
        //综合评分
        mCommentAvg = (TextView) view.findViewById(R.id.app_restaurant_comment_type_avg);
        mCommentAvgLayout = (LinearLayout) view.findViewById(R.id.app_restaurant_comment_type_avg_layout);

        //物流评分
        mCommentDelivery = (TextView) view.findViewById(R.id.app_restaurant_comment_type_delivery);
        mCommentDeliveryLayout = (LinearLayout) view.findViewById(R.id.app_restaurant_comment_type_delivery_layout);
        mCommentDeliveryLine = view.findViewById(R.id.app_restaurant_comment_type_delivery_line);

        //菜品评分
        mCommentDishes = (TextView) view.findViewById(R.id.app_restaurant_comment_type_dishes);
        mCommentDishesLayout = (LinearLayout) view.findViewById(R.id.app_restaurant_comment_type_dishes_layout);
        mCommentDishesLine = view.findViewById(R.id.app_restaurant_comment_type_dishes_line);


        setCommentClick(mCommentAll, COMMENT_TYPE_ALL);
        setCommentClick(mCommentGood, COMMENT_TYPE_GOOD);
        setCommentClick(mCommentMiddle, COMMENT_TYPE_MIDDLE);
        setCommentClick(mCommentBad, COMMENT_TYPE_BAD);

        updateCommentType();
    }

    private void updateCommentType() {
        mCommentAll.setSelected(mCommentType == COMMENT_TYPE_ALL);
        mCommentGood.setSelected(mCommentType == COMMENT_TYPE_GOOD);
        mCommentMiddle.setSelected(mCommentType == COMMENT_TYPE_MIDDLE);
        mCommentBad.setSelected(mCommentType == COMMENT_TYPE_BAD);
    }

    private void setCommentClick(View view, final int type) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCommentType != type) {
                    mCommentType = type;
                    showLoading();
                    finishAllLoaded(false);
                    onStartUILoad();
                }
            }
        });
    }

    @Override
    public void showLoading() {
        super.showLoading();
        mLoadingDialog.show();
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        mLoadingDialog.dismiss();
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (type == TYPE_RESTAURANT) {
            CommentApi.getCommentRestaurantOverview(mId, new ApiCallback<CommentCount>() {
                @Override
                public void onResult(CommentCount result) {
                    if (result.available()) {
                        mCommentCount = result;
                        updateCommentHead();
                    }
                }
            });
        } else if (type == TYPE_DISH) {
            CommentApi.getCommentDishOverview(mId,
                    new ApiCallback<CommentCount>() {
                        @Override
                        public void onResult(CommentCount result) {
                            if (result.available()) {
                                mCommentCount = result;
                                updateCommentHead();
                            }
                        }
                    });
        }

    }

    private void updateCommentHead() {

        updateCommentType();
        if (type == TYPE_DISH) {
            mCoreLayout.setVisibility(View.GONE);
        } else {
            mCoreLayout.setVisibility(View.VISIBLE);
        }
        //综合评分
        if (!StringUtils.isEmpty(mCommentCount.getAverage())) {
            mCommentAvgLayout.setVisibility(View.VISIBLE);
            mCommentAvg.setText(mCommentCount.getAverage());
        } else {
            mCommentAvgLayout.setVisibility(View.GONE);
        }
        //物流评分
        if (!StringUtils.isEmpty(mCommentCount.getType_delivery())) {
            mCommentDeliveryLayout.setVisibility(View.VISIBLE);
            mCommentDeliveryLine.setVisibility(View.VISIBLE);
            mCommentDelivery.setText(mCommentCount.getType_delivery());
        } else {
            mCommentDeliveryLayout.setVisibility(View.GONE);
            mCommentDeliveryLine.setVisibility(View.GONE);

        }

        //菜品评论
        if (!StringUtils.isEmpty(mCommentCount.getType_dishes())) {
            mCommentDishesLayout.setVisibility(View.VISIBLE);
            mCommentDishesLine.setVisibility(View.VISIBLE);
            mCommentDishes.setText(mCommentCount.getType_dishes());

        } else {
            mCommentDishesLayout.setVisibility(View.GONE);
            mCommentDishesLine.setVisibility(View.GONE);
        }


        if (!StringUtils.isEmpty(mCommentCount.getAll())
                && !"0".equals(mCommentCount.getAll())) {
            mCommentAll.setText("全部 " + mCommentCount.getAll() + "");
        } else {
            mCommentAll.setText("全部");
        }
        if (!StringUtils.isEmpty(mCommentCount.getGood())
                && !"0".equals(mCommentCount.getGood())) {
            mCommentGood.setText("好评 " + mCommentCount.getGood() + "");
        } else {
            mCommentGood.setText("好评");
        }
        if (!StringUtils.isEmpty(mCommentCount.getMiddle())
                && !"0".equals(mCommentCount.getMiddle())) {
            mCommentMiddle.setText("中评 " + mCommentCount.getMiddle() + "");
        } else {
            mCommentMiddle.setText("中评");
        }
        if (!StringUtils.isEmpty(mCommentCount.getBad())
                && !"0".equals(mCommentCount.getBad())) {
            mCommentBad.setText("差评 " + mCommentCount.getBad() + "");
        } else {
            mCommentBad.setText("差评");
        }
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView emptyView = getEmptyView();
        if (emptyView != null) {
            emptyView.setTitle("暂无评论");
            emptyView.setSecondTitle("");
            emptyView.setImageViewResourceIdOfTop(R.drawable.app_pic_empty_comment);
        }
    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<Comment>> callback) {
        if (type == TYPE_RESTAURANT) {
            CommentApi.getCommentListOfRestaurant(mId, mCommentType, page, pageSize, callback);
        } else if (type == TYPE_DISH) {
            CommentApi.getCommentListOfDish(mId, mCommentType, page, pageSize, callback);
        }
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_item_comment_dish;
    }

    @Override
    protected void updateItemView(View view, int position, int viewType, final Comment data, Bundle extra) {
        super.updateItemView(view, position, viewType, data, extra);
        ImageView userAvatar = (ImageView) view.findViewById(R.id.app_comment_avatar);
        TextView userName = (TextView) view.findViewById(R.id.app_comment_name);
        TextView content = (TextView) view.findViewById(R.id.app_comment_content);
        TextView time = (TextView) view.findViewById(R.id.app_comment_time);
        RatingBar star = (RatingBar) view.findViewById(R.id.app_comment_star);
        final TextView dish = (TextView) view.findViewById(R.id.app_comment_dish);
        LinearLayout replyLayout = (LinearLayout) view.findViewById(R.id.app_comment_reply_layout);
        HorizontalScrollView pictrueLayout = (HorizontalScrollView) view.findViewById(R.id.app_comment_picture_layout);
        PictureHelpView pictrueView = (PictureHelpView) view.findViewById(R.id.app_comment_pictureview);

        //评论人信息
        if (data.getAuthor() != null) {
            final User author = data.getAuthor();
            userAvatar.setVisibility(View.VISIBLE);
            ImageFetcher.asInstance().load(author.getAvatar(), userAvatar, R.drawable.app_pic_default_avatar);
            userName.setText(author.getNickname());
//            //跳转个人信息
//            userAvatar.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Bundle bundle = new Bundle();
//                    bundle.putSerializable(UserPageFragment.EXTRA_USER, author);
//                    next(UserPageFragment.class, bundle);
//                }
//            });
        } else {
            userAvatar.setVisibility(View.GONE);
        }
        //评论内容
        content.setText(data.getContent());
        time.setText(data.getCreateTime());
        star.setRating(StringUtils.parseFloat(data.getScore()));
        //评论回复
        replyLayout.removeAllViews();
        if (data.getReplyList() != null) {
            replyLayout.setVisibility(View.VISIBLE);
            List<Comment> replyList = data.getReplyList().getList();
            if (replyList != null) {
                for (int i = 0; i < replyList.size(); i++) {
                    View replyView = addCommentReplyView(replyList.get(i));
                    if (replyView != null) {
                        replyLayout.addView(replyView);
                    }
                }
            }

        } else {
            replyLayout.setVisibility(View.GONE);
        }

        //晒图
        if (data.getImageData() == null) {
            ArrayList<Image> images = new ArrayList<>();
            if (data.getImages() != null && data.getImages_origin() != null && data.getImages().size() > 0 && data.getImages_origin().size() > 0) {
                for (int i = 0; i < data.getImages().size() && i < data.getImages_origin().size(); i++) {
                    Image image = new Image();
                    image.setImage(data.getImages().get(i));
                    image.setImage_origin(data.getImages_origin().get(i));
                    images.add(image);
                }

            }
            data.setImageData(images);
        }


        if (data.getImageData() != null && data.getImageData().size() > 0) {
            pictrueLayout.setVisibility(View.VISIBLE);
            pictrueView.setDataAndIsUpdatePicture(data.getImageData(), false);
            pictrueView.setPictureHelpCallBack(new PictureHelpView.PictureHelpCallBack() {
                @Override
                public void callback(int position, View parent, List<Image> data, boolean isUpdatePicture) {
                    if (!isUpdatePicture) {
                        // 跳转查看图片详情
                        Intent intent = new Intent(getContext(), MultiPictureActivity.class);
                        intent.putExtra(MultiPictureActivity.EXTRA_IMAGES, (Serializable) data);
                        intent.putExtra(MultiPictureActivity.EXTRA_FIRST_POS, position);
                        intent.putExtra(MultiPictureActivity.EXTRA_IS_SHOW_TITLE, false);
                        startActivity(intent);
                    }
                }
            });
        } else {
            pictrueLayout.setVisibility(View.GONE);
        }

        //评论菜品
        if (type == TYPE_RESTAURANT) {
            if (data.getDish() != null) {
                dish.setVisibility(View.VISIBLE);
                dish.setText("菜品：" + data.getDish().getTitle());
                dish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //菜品详情
                        Bundle bundle = new Bundle();
                        bundle.putString(DishDetailFragment.EXTRA_DISH_ID, data.getDish().getDishId());
                        bundle.putString(DishDetailFragment.EXTRA_TITLE, data.getDish().getTitle());
                        next(DishDetailFragment.class, bundle);
                    }
                });
            } else {
                dish.setVisibility(View.GONE);
            }
        }

    }

    /**
     * 创建单个回复
     *
     * @param comment
     */
    private View addCommentReplyView(Comment comment) {
        if (comment == null) {
            return null;
        }
        View commentReplyView = LayoutInflater.from(getContext()).inflate(R.layout.view_comment_reply, null);
        TextView name = (TextView) commentReplyView.findViewById(R.id.app_comment_reply_name);
        TextView content = (TextView) commentReplyView.findViewById(R.id.app_comment_reply_content);
        if (comment.getAuthor().getNickname() != null) {
            name.setText(comment.getAuthor().getNickname() + " 回复:");
        }
        if (comment.getContent() != null) {
            content.setText(comment.getContent());
        }
        return commentReplyView;
    }
}
