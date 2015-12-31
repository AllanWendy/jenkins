package cn.wecook.app.main.kitchen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.legacy.FoodApi;
import com.wecook.sdk.api.legacy.KitchenApi;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.api.model.FoodResourceExtends;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.loader.LoadMoreImpl;
import com.wecook.uikit.loader.UILoader;
import com.wecook.uikit.widget.DescriptionView;
import com.wecook.uikit.widget.LabelView;
import com.wecook.uikit.widget.LineView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.adapter.CommentListAdapter;
import cn.wecook.app.adapter.FoodResourceElementsAdapter;
import cn.wecook.app.dialog.InputCommentDialog;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.kitchen.detail.KitchenResourceFoodGroupView;
import cn.wecook.app.main.kitchen.detail.KitchenResourceHeadView;
import cn.wecook.app.main.recommend.list.food.FoodListResourceFragment;

/**
 * 详情页面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/15/14
 */
public class KitchenResourceDetailFragment extends BaseTitleFragment {

    public static final String EXTRA_DATA = "extra_data";

    public static final String EXTRA_JUMP_TO_COMMENT = "extra_jump_to_comment";

    private FoodResource mFoodResource;

    private MergeAdapter mAdapter;

    private ListView mListView;

    private PullToRefreshListView mPullToRefreshListView;

    private KitchenResourceHeadView mHeadView;

    private FoodResourceElementsAdapter mElementsAdapter;

    private CommentListAdapter mCommentAdapter;
    private LoadMoreImpl<List<Comment>> mLoadMore;
    private boolean mIsJumpToComment;
    private LabelView mCommentLabel;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mFoodResource = (FoodResource) bundle.getSerializable(EXTRA_DATA);
            mIsJumpToComment = bundle.getBoolean(EXTRA_JUMP_TO_COMMENT);
        }

        mAdapter = new MergeAdapter();

        String type = "";
        if (KitchenApi.TYPE_KITCHENWARE.equals(mFoodResource.getType())) {
            type = LogConstant.TYPE_WARE;
        } else if (KitchenApi.TYPE_BARCODE.equals(mFoodResource.getType())) {
            type = LogConstant.TYPE_BARCODE;
        } else if (KitchenApi.TYPE_CONDIMENT.equals(mFoodResource.getType())) {
            type = LogConstant.TYPE_CONDIANT;
        } else if (KitchenApi.TYPE_INGREDIENT.equals(mFoodResource.getType())) {
            type = LogConstant.TYPE_INGREDIENT;
        }
        Map<String, String> keys = new HashMap<String, String>();
        keys.put(LogConstant.KEY_TYPE, type);
        MobclickAgent.onEvent(getContext(), LogConstant.UBS_KITCHEN_ITEM_DETAILS_COUNT, keys);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resource_detail, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(mFoodResource.getName());

        TitleBar.ActionCoveredImageView share = new TitleBar.ActionCoveredImageView(getContext(),
                R.drawable.app_bt_share_highlight);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThirdPortDelivery.share(getContext(), mFoodResource);
            }
        });
        titleBar.addActionView(share);
        mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.app_resource_listview);
        mListView = mPullToRefreshListView.getRefreshableView();

        View commentView = view.findViewById(R.id.app_resource_comment);
        commentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserProperties.isLogin()) {
                    new InputCommentDialog(getContext(), CommentApi.TYPE_CATEGORY, mFoodResource.getId())
                            .setOnSendCommentListener(new InputCommentDialog.OnSendCommentListener() {
                                @Override
                                public void sendResult(Comment comment) {
                                    if (comment != null) {
                                        if (mCommentAdapter != null) {
                                            mCommentAdapter.addEntry(0, comment);
                                        }
                                    }
                                }
                            }).show();
                } else {
                    startActivity(new Intent(getContext(), UserLoginActivity.class));
                }
            }
        });

    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (mFoodResource != null) {
            showLoading();
            KitchenApi.getDetail(mFoodResource.getType(), mFoodResource.getId(),
                    new FoodResource(), new ApiCallback<FoodResource>() {
                        @Override
                        public void onResult(FoodResource result) {
                            if (getContext() != null) {
                                mFoodResource = result;

                                TitleBar titleBar = getTitleBar();
                                titleBar.setTitle(mFoodResource.getName());

                                //图片＋描述
                                mHeadView = new KitchenResourceHeadView(getContext());
                                mHeadView.loadLayout(R.layout.view_kitchen_detail_headview, result);
                                mAdapter.addView(mHeadView);

                                LineView lineView = new LineView(getContext());
                                lineView.initLayout();
                                mAdapter.addView(lineView);

                                //详情事项
                                if (result.getmExtends() != null) {
                                    List<FoodResourceExtends> extendList = result.getmExtends().getList();
                                    if (extendList != null && !extendList.isEmpty()) {
                                        for (FoodResourceExtends resourceExtends : extendList) {
                                            LabelView labelView = new LabelView(getContext());
                                            labelView.initWithLabelName(resourceExtends.getName());
                                            mAdapter.addView(labelView);

                                            DescriptionView descriptionView = new DescriptionView(getContext());
                                            descriptionView.initDescription(resourceExtends.getValue());
                                            mAdapter.addView(descriptionView);

                                            lineView = new LineView(getContext());
                                            lineView.initLayout();
                                            mAdapter.addView(lineView);
                                        }
                                    }
                                }

                                //FIXME 营养元素,暂时不展示
//                            if (result.getmElements() != null) {
//                                List<FoodResourceElements> elementsList = result.getmElements().getList();
//                                if (elementsList != null && !elementsList.isEmpty()) {
//                                    LabelView labelView = new LabelView(getContext());
//                                    labelView.initWithLabelName("营养成分");
//                                    mAdapter.addView(labelView);
//
//                                    mElementsAdapter = new FoodResourceElementsAdapter(getContext(), elementsList);
//                                    mAdapter.addAdapter(mElementsAdapter);
//
//                                    lineView = new LineView(getContext());
//                                    lineView.initLayout();
//                                    mAdapter.addView(lineView);
//                                }
//                            }

                                //专用菜谱
                                requestFoodList(result);
                            }
                        }
                    }
            );
        }
    }

    private void requestFoodList(final FoodResource result) {
        FoodApi.getFoodListByResource(result.getType(), result.getName(), 1, 20, new ApiCallback<ApiModelList<Food>>() {
            @Override
            public void onResult(ApiModelList<Food> food) {
                if (food != null && food.available() && getContext() != null) {
                    LabelView labelView = new LabelView(getContext());
                    labelView.initWithLabelName("该宝贝共" + food.getCountOfServer() + "道专用菜谱");
                    labelView.setOnMoreClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setClickMarker(LogConstant.SOURCE_SPECIAL_INGREDIENT);
                            Map<String, String> keys = new HashMap<String, String>();
                            keys.put(LogConstant.KEY_SOURCE, LogConstant.SOURCE_SPECIAL_INGREDIENT);
                            MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPELIST_OPEN_COUNT, keys);

                            Bundle bundle = new Bundle();
                            bundle.putString(FoodListResourceFragment.EXTRA_TITLE, result.getName() + "的专用菜谱");
                            bundle.putSerializable(FoodListResourceFragment.EXTRA_FOOD_RESOURCE, result);
                            next(FoodListResourceFragment.class, bundle);
                        }
                    });
                    mAdapter.addView(labelView);

                    KitchenResourceFoodGroupView foodGroupView = new KitchenResourceFoodGroupView(getContext());
                    foodGroupView.loadLayout(R.layout.view_kitchen_detail_foodgroup, food.getList());
                    mAdapter.addView(foodGroupView);

                    LineView lineView = new LineView(getContext());
                    lineView.initLayout();
                    mAdapter.addView(lineView);
                }
                requestCommentList(result);
            }
        });
    }

    private void requestCommentList(final FoodResource resource) {
        //评论
        CommentApi.getCommentList(CommentApi.TYPE_CATEGORY, resource.getId(),
                1, 10, new ApiCallback<ApiModelList<Comment>>() {
                    @Override
                    public void onResult(ApiModelList<Comment> result) {
                        if (result != null && result.available() && getContext() != null) {
                            mCommentLabel = new LabelView(getContext());
                            mCommentLabel.initWithLabelName("大家的讨论");
                            mAdapter.addView(mCommentLabel);
                            mCommentAdapter = new CommentListAdapter(KitchenResourceDetailFragment.this,
                                    result.getList(), CommentApi.TYPE_CATEGORY, resource.getId());
                            mAdapter.addAdapter(mCommentAdapter);

                            mLoadMore = new LoadMoreImpl<List<Comment>>();
                            mLoadMore.setMoreLoader(new UILoader<List<Comment>>(KitchenResourceDetailFragment.this) {
                                @Override
                                public List<Comment> runBackground() {
                                    return SyncHandler.sync(this);
                                }

                                @Override
                                public void sync(final Callback<List<Comment>> callback) {
                                    super.sync(callback);

                                    CommentApi.getCommentList(CommentApi.TYPE_CATEGORY, resource.getId(),
                                            mLoadMore.getCurrentPage(), mLoadMore.getPageSize(),
                                            new ApiCallback<ApiModelList<Comment>>() {
                                                @Override
                                                public void onResult(ApiModelList<Comment> result) {
                                                    if (result != null && result.available() && getContext() != null) {
                                                        callback.callback(result.getList());
                                                    } else {
                                                        callback.callback(null);
                                                    }
                                                }
                                            });
                                }
                            });
                            mLoadMore.setOnLoadMoreListener(new LoadMore.OnLoadMoreListener<List<Comment>>() {
                                @Override
                                public void onLoaded(boolean success, List<Comment> o) {
                                    if (success) {
                                        if (mCommentAdapter != null) {
                                            mCommentAdapter.addEntrys(o);
                                        }
                                    }
                                }
                            });
                            mLoadMore.setListAdapter(mCommentAdapter, mListView);
                        }

                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mListView.getAdapter() != null) {
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    mListView.setAdapter(mAdapter);
                                }
                                if (mIsJumpToComment) {
                                    for (int i = 0; i < mAdapter.getCount(); i++) {
                                        Object o = mAdapter.getItem(i);
                                        if (o == mCommentLabel) {
                                            mListView.setSelection(Math.max(i - 1, 1));
                                            break;
                                        }
                                    }
                                }
                                hideLoading();
                                finishAllLoaded(true);
                            }
                        });

                    }
                }
        );
    }
}
