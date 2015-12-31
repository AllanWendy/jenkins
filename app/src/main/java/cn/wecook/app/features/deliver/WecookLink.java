package cn.wecook.app.features.deliver;

import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.app.AppLink;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.AndroidUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.api.model.Message;
import com.wecook.sdk.api.model.Party;
import com.wecook.sdk.api.model.Topic;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.policy.MessageQueuePolicy;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.features.comment.CommentListFragment;
import cn.wecook.app.features.pick.PickActivity;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.features.webview.WebViewFragment;
import cn.wecook.app.launch.LaunchActivity;
import cn.wecook.app.main.dish.DishActivity;
import cn.wecook.app.main.dish.DishDetailFragment;
import cn.wecook.app.main.dish.list.DishGroupListFragment;
import cn.wecook.app.main.dish.list.DishListFragment;
import cn.wecook.app.main.dish.list.DishSpecialListFragment;
import cn.wecook.app.main.dish.order.DishOrderDetailFragment;
import cn.wecook.app.main.dish.restaurant.DishRestaurantDetailFragment;
import cn.wecook.app.main.dish.restaurant.DishRestaurantFragment;
import cn.wecook.app.main.home.message.MessageFragment;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.home.wallet.WalletFragment;
import cn.wecook.app.main.kitchen.KitchenResourceDetailFragment;
import cn.wecook.app.main.recommend.detail.cookshow.CookShowDetailFragment;
import cn.wecook.app.main.recommend.detail.food.FoodDetailFragment;
import cn.wecook.app.main.recommend.detail.party.PartyDetailFragment;
import cn.wecook.app.main.recommend.detail.topic.TopicDetailFragment;
import cn.wecook.app.main.recommend.list.cookshow.CookShowPageActivity;
import cn.wecook.app.main.recommend.list.food.CategoryListFragment;
import cn.wecook.app.main.recommend.list.food.FoodListFragment;
import cn.wecook.app.main.recommend.list.food.FoodListSearchFragment;
import cn.wecook.app.main.recommend.list.party.PartyListFragment;
import cn.wecook.app.main.recommend.list.topic.TopicListFragment;

/**
 * 味库快捷递送
 *
 * @author kevin
 * @version v1.0
 * @since 2015-3/13/15
 */
public class WecookLink extends AppLink {

    private static WecookLink sInstance;
    private Context mContext;

    private WecookLink() {
        init();
    }

    public static WecookLink getInstance() {
        if (sInstance == null) {
            sInstance = new WecookLink();
        }
        return sInstance;
    }

    private void init() {
        //空匹配
        sCallbackMap.put(UriMatcher.NO_MATCH, new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                if (isHttpUri(uri)) {
                    BaseFragment fragment = getForegroundFragment();
                    if (fragment != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString(WebViewFragment.EXTRA_URL, uri.toString());
                        fragment.startActivity(new Intent(fragment.getContext(), WebViewActivity.class), bundle);
                    }
                }
            }
        });

        processDishDeliver();
        processCookShowDeliver();
        processRecipeDeliver();
        processTopicDeliver();
        processPartyDeliver();
        processResourceDeliver();
        processOrderDeliver();

        //系统公告
        AppLink.registerLink(Message.TYPE_SYSTEM, "", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                String systemNotifyId = params.getString("id");
                //TODO 弹出系统公告栏
            }
        });

        //消息列表
        AppLink.registerLink(Message.TYPE_NOTIFY, "index", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                if (UserProperties.isLogin()) {
                    readMessage(params.getString("msgId"));
                    nextCard(MessageFragment.class);
                } else {
                    mContext.startActivity(new Intent(getContext(), UserLoginActivity.class));
                }
            }
        });
    }

    private void processOrderDeliver() {
        //跳转到购物车
        AppLink.registerLink(Message.TYPE_ORDER, "shopcart", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                Intent intent = new Intent(mContext, DishActivity.class);
                intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_SHOP_CART);
                mContext.startActivity(intent);
            }
        });

        //跳转到订单列表
        AppLink.registerLink(Message.TYPE_ORDER, "list", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                if (UserProperties.isLogin()) {
                    Intent intent = new Intent(mContext, DishActivity.class);
                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_ORDER_LIST);
                    mContext.startActivity(intent);
                } else {
                    mContext.startActivity(new Intent(getContext(), UserLoginActivity.class));
                }
            }
        });

        //跳转到我的钱包
        AppLink.registerLink(Message.TYPE_ORDER, "wallet", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                if (UserProperties.isLogin()) {
                    nextCard(WalletFragment.class);
                } else {
                    mContext.startActivity(new Intent(getContext(), UserLoginActivity.class));
                }
            }
        });

        //跳转到订单详情
        AppLink.registerLink(Message.TYPE_ORDER, "detail", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                if (UserProperties.isLogin()) {
                    String orderId = params.getString("id");
                    if (!StringUtils.isEmpty(orderId)) {
                        Intent intent = new Intent(mContext, DishActivity.class);
                        intent.putExtra(DishOrderDetailFragment.EXTRA_ORDER_ID, orderId);
                        intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_ORDER_DETAIL);
                        mContext.startActivity(intent);
                    }
                } else {
                    mContext.startActivity(new Intent(getContext(), UserLoginActivity.class));
                }
            }
        });
    }

    /**
     * 买菜帮手
     */
    private void processDishDeliver() {
        //菜品列表
        AppLink.registerLink(Message.TYPE_DISHES, "dish/list", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                String type = params.getString("type");
                String title = params.getString("p_title");
                String keywords = params.getString("keywords");
                boolean jumpToFragment = "true".equals(params.getString("jump_to_fragment"));
                if ("1".equals(type)) {
                    //菜品列表
                    Bundle bundle = new Bundle();
                    bundle.putString(DishListFragment.EXTRA_TITLE, title);
                    bundle.putString(DishListFragment.EXTRA_KEY_WORDS, keywords);
                    nextCard(DishListFragment.class, bundle);
                } else if ("2".equals(type)) {
                    //热门套餐
                    if (jumpToFragment) {
                        if (mContext instanceof BaseSwipeActivity) {
                            Bundle bundle = new Bundle();
                            bundle.putString(DishGroupListFragment.EXTRA_TITLE, title);
                            ((BaseSwipeActivity) mContext).next(DishGroupListFragment.class, bundle);
                        }
                    } else {
                        Intent intent = new Intent(mContext, DishActivity.class);
                        intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_GROUP);
                        intent.putExtra(DishGroupListFragment.EXTRA_TITLE, title);
                        mContext.startActivity(intent);
                    }
                } else if ("4".equals(type)) {
                    //今日特价
                    if (jumpToFragment) {
                        if (mContext instanceof BaseSwipeActivity) {
                            ((BaseSwipeActivity) mContext).next(DishSpecialListFragment.class);
                        }
                    } else {
                        Intent intent = new Intent(mContext, DishActivity.class);
                        intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_SPECIAL_DISH);
                        mContext.startActivity(intent);
                    }
                }
            }
        });

        //菜品详情页面
        AppLink.registerLink(Message.TYPE_DISHES, "dish/detail", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                String dishId = params.getString("id");
                if (!StringUtils.isEmpty(dishId)) {
                    Intent intent = new Intent(mContext, DishActivity.class);
                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_DISH_DETAIL);
                    intent.putExtra(DishDetailFragment.EXTRA_DISH_ID, dishId);
                    mContext.startActivity(intent);
                }
            }
        });

        //餐厅列表
        AppLink.registerLink(Message.TYPE_DISHES, "restaurant/list", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                boolean jumpToFragment = "true".equals(params.getString("jump_to_fragment"));
                String title = params.getString("p_title");
                if (jumpToFragment) {
                    if (mContext instanceof BaseSwipeActivity) {
                        Bundle bundle = new Bundle();
                        bundle.putString(DishRestaurantFragment.EXTRA_TITLE, title);
                        ((BaseSwipeActivity) mContext).next(DishRestaurantFragment.class, bundle);
                    }
                } else {
                    Intent intent = new Intent(mContext, DishActivity.class);
                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_RESTAURANT);
                    intent.putExtra(DishRestaurantFragment.EXTRA_TITLE, title);
                    mContext.startActivity(intent);
                }
            }
        });

        //餐厅详情页面
        AppLink.registerLink(Message.TYPE_DISHES, "restaurant/detail", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                String restaurantId = params.getString("id");
                if (!StringUtils.isEmpty(restaurantId)) {
                    Intent intent = new Intent(mContext, DishActivity.class);
                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_RESTAURANT_DETAIL);
                    intent.putExtra(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, restaurantId);
                    mContext.startActivity(intent);
                }
            }
        });
    }

    /**
     * 食材或资源
     */
    private void processResourceDeliver() {
        List<String> listType = new ArrayList<String>();
        listType.add(Message.TYPE_INGREDIENT);
        listType.add(Message.TYPE_CONDIMENT);
        listType.add(Message.TYPE_KITCHENWARE);
        listType.add(Message.TYPE_BARCODE);

        AppLink.registerLink(listType, "detail", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                String type = uri.getAuthority();
                FoodResource resource = new FoodResource();
                resource.setType(type);
                resource.setId(params.getString("id"));
                Bundle bundle = new Bundle();
                bundle.putSerializable(KitchenResourceDetailFragment.EXTRA_DATA, resource);
                bundle.putBoolean(KitchenResourceDetailFragment.EXTRA_JUMP_TO_COMMENT, false);
                nextCard(KitchenResourceDetailFragment.class, bundle);
            }
        });

        AppLink.registerLink(listType, "comment", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                String type = uri.getAuthority();
                FoodResource resource = new FoodResource();
                resource.setType(type);
                resource.setId(params.getString("id"));
                Bundle bundle = new Bundle();
                bundle.putSerializable(KitchenResourceDetailFragment.EXTRA_DATA, resource);
                bundle.putBoolean(KitchenResourceDetailFragment.EXTRA_JUMP_TO_COMMENT, true);
                nextCard(KitchenResourceDetailFragment.class, bundle);
            }
        });

    }

    /**
     * 活动
     */
    private void processPartyDeliver() {
        //活动详情
        AppLink.registerLink(Message.TYPE_EVENTS, "detail", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                Bundle bundle = new Bundle();
                Party party = new Party();
                party.setId(params.getString("id"));
                bundle.putSerializable(PartyDetailFragment.EXTRA_PARTY, party);
                bundle.putBoolean(PartyDetailFragment.EXTRA_JUMP_TO_COMMENT, false);
                nextCard(PartyDetailFragment.class, bundle);
            }
        });

        //活动列表
        AppLink.registerLink(Message.TYPE_EVENTS, "index", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                nextCard(PartyListFragment.class);
            }
        });

        //活动评论
        AppLink.registerLink(Message.TYPE_EVENTS, "comment", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                Bundle bundle = new Bundle();
                Party party = new Party();
                party.setId(params.getString("id"));
                bundle.putSerializable(PartyDetailFragment.EXTRA_PARTY, party);
                bundle.putBoolean(PartyDetailFragment.EXTRA_JUMP_TO_COMMENT, true);
                nextCard(PartyDetailFragment.class, bundle);
            }
        });
    }

    /**
     * 菜谱
     */
    private void processRecipeDeliver() {
        //菜谱详情
        AppLink.registerLink(Message.TYPE_RECIPE, "detail", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                Bundle bundle = new Bundle();
                Food food = new Food();
                food.setId(params.getString("id"));
                bundle.putSerializable(FoodDetailFragment.EXTRA_FOOD, food);
                bundle.putBoolean(FoodDetailFragment.EXTRA_JUMP_TO_COMMENT, false);
                nextCard(FoodDetailFragment.class, bundle);
            }
        });

        //菜谱列表
        AppLink.registerLink(Message.TYPE_RECIPE, "index", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                nextCard(FoodListFragment.class);
            }
        });

        //菜谱分类列表
        AppLink.registerLink(Message.TYPE_RECIPE, "category", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                LogGather.onEventTagMore();
                nextCard(CategoryListFragment.class);
            }
        });

        //菜谱分类详情列表
        AppLink.registerLink(Message.TYPE_RECIPE, "search", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                String keywords = params.getString("keywords");
                LogGather.onEventTagRecommend(keywords);
                Bundle bundle = new Bundle();
                bundle.putString(FoodListSearchFragment.EXTRA_KEYWORD, keywords);
                nextCard(FoodListSearchFragment.class, bundle);
            }
        });

        //菜谱评论
        AppLink.registerLink(Message.TYPE_RECIPE, "comment", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                Bundle bundle = new Bundle();
                Food food = new Food();
                food.setId(params.getString("id"));
                bundle.putSerializable(FoodDetailFragment.EXTRA_FOOD, food);
                bundle.putBoolean(FoodDetailFragment.EXTRA_JUMP_TO_COMMENT, true);
                nextCard(FoodDetailFragment.class, bundle);
            }
        });
    }

    /**
     * 专题
     */
    private void processTopicDeliver() {
        //专题详情
        AppLink.registerLink(Message.TYPE_TOPIC, "detail", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                Bundle bundle = new Bundle();
                Topic data = new Topic();
                data.setId(params.getString("id"));
                bundle.putString(TopicDetailFragment.EXTRA_URL,
                        "http://m.wecook.cn/topic/detail?inwecook=true&id=" + data.getId());
                bundle.putString(TopicDetailFragment.EXTRA_TITLE, data.getTitle());
                data.setUrl("http://m.wecook.cn/topic/detail?inwecook=true&id=" + data.getId());
                bundle.putSerializable(TopicDetailFragment.EXTRA_DATA, data);
                bundle.putInt(WebViewActivity.EXTRA_PAGE, WebViewActivity.PAGE_TOPIC_DETAIL);
                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });

        //专题列表
        AppLink.registerLink(Message.TYPE_TOPIC, "index", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                nextCard(TopicListFragment.class);
            }
        });

        //专题评论
        AppLink.registerLink(Message.TYPE_TOPIC, "comment", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                Bundle bundle = new Bundle();
                bundle.putString(CommentListFragment.EXTRA_TITLE, "全部评论");
                bundle.putString(CommentListFragment.EXTRA_REQUEST_ID, params.getString("id"));
                bundle.putString(CommentListFragment.EXTRA_TYPE, Message.TYPE_TOPIC);
                nextCard(CommentListFragment.class, bundle);
            }
        });
    }

    /**
     * 晒厨艺
     */
    private void processCookShowDeliver() {
        //厨艺详情
        AppLink.registerLink(Message.TYPE_COOKING, "detail", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                Bundle bundle = new Bundle();
                bundle.putString(CookShowDetailFragment.EXTRA_COOK_SHOW_ID, params.getString("id"));
                bundle.putBoolean(CookShowDetailFragment.EXTRA_JUMP_TO_COMMENT, false);
                nextCard(CookShowDetailFragment.class, bundle);
            }
        });

        //厨艺列表
        AppLink.registerLink(Message.TYPE_COOKING, "index", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                Intent actionIntent = new Intent(getContext(), CookShowPageActivity.class);
                mContext.startActivity(actionIntent);
            }
        });

        //晒厨艺动作
        AppLink.registerLink(Message.TYPE_COOKING, "create", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                Intent intent = new Intent(getContext(), PickActivity.class);
                intent.putExtra(PickActivity.EXTRA_PICK_TYPE, PickActivity.PICK_COOK_SHOW);
                mContext.startActivity(intent);
            }
        });

        //厨艺评论
        AppLink.registerLink(Message.TYPE_COOKING, "comment", new LinkCallback() {
            @Override
            public void doLink(Uri uri, Bundle params) {
                readMessage(params.getString("msgId"));
                Bundle bundle = new Bundle();
                bundle.putString(CookShowDetailFragment.EXTRA_COOK_SHOW_ID, params.getString("id"));
                bundle.putBoolean(CookShowDetailFragment.EXTRA_JUMP_TO_COMMENT, true);
                nextCard(CookShowDetailFragment.class, bundle);
            }
        });
    }

    private void readMessage(String msgId) {
        if (!StringUtils.isEmpty(msgId)) {
            MessageQueuePolicy.getInstance().readMessage(msgId);
        }
    }

    private void nextCard(Class<? extends BaseFragment> card) {
        BaseFragment fragment = getForegroundFragment();
        if (mContext != null && mContext instanceof BaseSwipeActivity) {
            ((BaseSwipeActivity) mContext).next(fragment, BaseFragment.getInstance(card), null);
        }
    }

    private void nextCard(final Class<? extends BaseFragment> card, final Bundle bundle) {
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BaseFragment fragment = getForegroundFragment();
                if (mContext != null && mContext instanceof BaseSwipeActivity) {
                    ((BaseSwipeActivity) mContext).next(fragment,
                            BaseFragment.getInstance(card, bundle), bundle);
                }
            }
        }, 200);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void onUpdateLink(Context context) {
        if (AndroidUtils.isMainProcess(context)) {
            mContext = context;
            performDeliver();
        }
    }

    private void performDeliver() {
        if (!sMessageList.isEmpty()) {
            LinkMessage message = sMessageList.remove(0);
            Logger.d("sendLink", "performDeliver...");
            int code = sendLink(message);
            if (code == UriMatcher.NO_MATCH) {
                Logger.d("sendLink", "NO_MATCH...");
            }
        }
    }

    /**
     * 启动软件
     *
     * @param context
     */
    @Override
    public void onLaunchApp(Context context) {
        //显示主Activity的时候，直接处理deliver，否则启动软件
        MobclickAgent.onEvent(context, LogConstant.MY_PUSH_CLICK);
        if (AndroidUtils.isMainForeground(context)) {
            performDeliver();
        } else {
            if (!sMessageList.isEmpty()) {
                Intent intent = new Intent(context, LaunchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }

    /**
     * 当前页面显示的fragment
     *
     * @return
     */
    public BaseFragment getForegroundFragment() {
        if (mContext != null && mContext instanceof BaseSwipeActivity) {
            return ((BaseSwipeActivity) mContext).getCurrentFragment();
        }
        return null;
    }
}
