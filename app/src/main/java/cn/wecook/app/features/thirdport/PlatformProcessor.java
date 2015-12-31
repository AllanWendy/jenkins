package cn.wecook.app.features.thirdport;

import android.content.Context;
import android.graphics.Bitmap;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.modules.thirdport.object.IShareObject;
import com.wecook.common.modules.thirdport.object.ShareWebpage;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.KitchenApi;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.api.model.FoodDetail;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.api.model.PartyDetail;
import com.wecook.sdk.api.model.ShareState;
import com.wecook.sdk.api.model.Topic;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.userinfo.UserProperties;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.features.webview.WebViewFragment;

/**
 * 第三方平台数据处理器
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/3/14
 */
class PlatformProcessor {

    /**
     * 处理菜谱详情页数据
     *
     * @param context
     */
    public static void prepareData(Context context, int platformType, FoodDetail foodDetail, OnPrepareListener listener) {
        IShareObject shareObject = null;
        if (foodDetail != null && foodDetail.getFoodRecipe() != null) {
            if (StringUtils.isEmpty(foodDetail.getUrl())) {
                foodDetail.setUrl("http://m.wecook.cn/recipe/detail/id/" + foodDetail.getFoodRecipe().getId() + ".html");
            }
            switch (platformType) {
                case PlatformManager.PLATFORM_WEBLOG: {
                    ShareWebpage webpage = new ShareWebpage(foodDetail.getUrl());
                    Bitmap data = ImageFetcher.asInstance().syncGetBitmap(foodDetail.getFoodRecipe().getImage(), 150, 150);
                    webpage.setBitmap(data);
                    webpage.setTitle(foodDetail.getFoodRecipe().getTitle());
                    webpage.setSecondTitle(foodDetail.getFoodRecipe().getDescription());
                    webpage.setMessage(context.getString(R.string.app_share_food_template, foodDetail.getFoodRecipe().getTitle()));
                    shareObject = webpage;
                    break;
                }
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS: {
                    ShareWebpage webpage = new ShareWebpage(foodDetail.getUrl());
                    byte[] data = ImageFetcher.asInstance().syncGetBitmapByte(foodDetail.getFoodRecipe().getImage(), 70, 70);
                    if (data != null) {
                        Logger.d("share", "image length : " + data.length);
                    }
                    webpage.setThumbnail(data);
                    webpage.setTitle(foodDetail.getFoodRecipe().getTitle());
                    webpage.setSecondTitle(context.getString(R.string.app_share_food_template, foodDetail.getFoodRecipe().getTitle()));
                    shareObject = webpage;
                    break;
                }
            }
        }

        listener.onPrepared(shareObject);
    }

    /**
     * 处理活动的分享数据
     *
     * @param context
     * @param platformType
     * @param partyDetail
     * @param listener
     */
    public static void prepareData(Context context, int platformType, PartyDetail partyDetail, OnPrepareListener listener) {
        IShareObject shareObject = null;
        if (partyDetail != null) {
            if (StringUtils.isEmpty(partyDetail.getUrl())) {
                partyDetail.setUrl("http://m.wecook.cn/activity/detail/id/" + partyDetail.getId() + ".html");
            }
            switch (platformType) {
                case PlatformManager.PLATFORM_WEBLOG: {
                    ShareWebpage webpage = new ShareWebpage(partyDetail.getUrl());
                    webpage.setTitle(partyDetail.getTitle());
                    Bitmap data = ImageFetcher.asInstance().syncGetBitmap(partyDetail.getImage(), 150, 150);
                    webpage.setBitmap(data);
                    webpage.setMessage("\"" + partyDetail.getTitle() + "\" " + context.getString(R.string.app_share_party_template));
                    webpage.setSecondTitle("\"" + partyDetail.getTitle() + "\" " + context.getString(R.string.app_share_party_template));
                    shareObject = webpage;
                    break;
                }
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS: {
                    ShareWebpage webpage = new ShareWebpage(partyDetail.getUrl());
                    webpage.setTitle(partyDetail.getTitle());
                    byte[] data = ImageFetcher.asInstance().syncGetBitmapByte(partyDetail.getImage(), 70, 70);
                    if (data != null) {
                        Logger.d("share", "image length : " + data.length);
                    }
                    webpage.setThumbnail(data);
                    webpage.setMessage(partyDetail.getTitle());
                    webpage.setSecondTitle(context.getString(R.string.app_share_party_template));
                    shareObject = webpage;
                    break;
                }
            }
        }

        listener.onPrepared(shareObject);
    }

    /**
     * 处理文字专题的分享数据
     *
     * @param context
     * @param platformType
     * @param topic
     * @param listener
     */
    public static void prepareData(Context context, int platformType, Topic topic, OnPrepareListener listener) {
        IShareObject shareObject = null;
        if (topic != null) {
            if (StringUtils.isEmpty(topic.getUrl())) {
                topic.setUrl("http://m.wecook.cn/topic/detail/id/" + topic.getId() + ".html");
            }
            switch (platformType) {
                case PlatformManager.PLATFORM_WEBLOG: {
                    ShareWebpage webpage = new ShareWebpage(topic.getUrl());
                    webpage.setTitle(topic.getTitle());
                    Bitmap data = ImageFetcher.asInstance().syncGetBitmap(topic.getImage(), 150, 150);
                    webpage.setBitmap(data);
                    webpage.setMessage("\"" + topic.getTitle() + "\" " + context.getString(R.string.app_share_topic_template));
                    webpage.setSecondTitle("\"" + topic.getTitle() + "\" " + context.getString(R.string.app_share_topic_template));
                    shareObject = webpage;
                    break;
                }
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS: {
                    ShareWebpage webpage = new ShareWebpage(topic.getUrl());
                    webpage.setTitle(topic.getTitle());
                    byte[] data = ImageFetcher.asInstance().syncGetBitmapByte(topic.getImage(), 70, 70);
                    if (data != null) {
                        Logger.d("share", "image length : " + data.length);
                    }
                    webpage.setThumbnail(data);
                    webpage.setMessage(topic.getTitle());
                    webpage.setSecondTitle(context.getString(R.string.app_share_topic_template));
                    shareObject = webpage;
                    break;
                }
            }
        }

        listener.onPrepared(shareObject);
    }

    /**
     * 处理晒厨艺的分享数据
     *
     * @param context
     * @param platformType
     * @param cookShow
     * @param listener
     */
    public static void prepareData(Context context, int platformType, CookShow cookShow, OnPrepareListener listener) {
        IShareObject shareObject = null;
        if (cookShow != null) {
            if (StringUtils.isEmpty(cookShow.getUrl())) {
                cookShow.setUrl("http://m.wecook.cn/recipe/detail/id/" + cookShow.getId() + ".html");
            }
            User user = cookShow.getUser();
            if (user != null && user.getUid().equals(UserProperties.getUser().getUid())) {
                cookShow.setDescription(context.getString(R.string.app_share_cook_show_self, cookShow.getTitle()));
            } else {
                cookShow.setDescription(context.getString(R.string.app_share_cook_show, cookShow.getTitle()));
            }
            switch (platformType) {
                case PlatformManager.PLATFORM_WEBLOG: {
                    ShareWebpage webpage = new ShareWebpage(cookShow.getUrl());
                    webpage.setTitle(cookShow.getTitle());
                    Bitmap data = ImageFetcher.asInstance().syncGetBitmap(cookShow.getImage(), 150, 150);
                    webpage.setBitmap(data);
                    webpage.setMessage(cookShow.getDescription());
                    webpage.setSecondTitle(cookShow.getDescription());
                    shareObject = webpage;
                    break;
                }
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS: {
                    ShareWebpage webpage = new ShareWebpage(cookShow.getUrl());
                    webpage.setTitle(cookShow.getTitle());
                    byte[] data = ImageFetcher.asInstance().syncGetBitmapByte(cookShow.getImage(), 70, 70);
                    if (data != null) {
                        Logger.d("share", "image length : " + data.length);
                    }
                    webpage.setThumbnail(data);
                    webpage.setMessage(cookShow.getDescription());
                    webpage.setSecondTitle(cookShow.getDescription());
                    shareObject = webpage;
                    break;
                }
            }
        }

        listener.onPrepared(shareObject);
    }

    /**
     * 处理食材原料的分享数据
     *
     * @param context
     * @param platformType
     * @param resource
     * @param listener
     */
    public static void prepareData(Context context, int platformType, FoodResource resource, OnPrepareListener listener) {
        IShareObject shareObject = null;
        if (resource != null) {
            switch (platformType) {
                case PlatformManager.PLATFORM_WEBLOG: {
                    ShareWebpage webpage = new ShareWebpage(resource.getUrl());
                    webpage.setTitle(resource.getName());
                    Bitmap data = ImageFetcher.asInstance().syncGetBitmap(resource.getImage(), 150, 150);
                    webpage.setBitmap(data);
                    if (KitchenApi.TYPE_KITCHENWARE.equals(resource.getType())) {
                        webpage.setMessage(context.getString(R.string.app_share_resource_template_ware, resource.getName()));
                        webpage.setSecondTitle(context.getString(R.string.app_share_resource_template_ware, resource.getName()));
                    } else {
                        webpage.setMessage(context.getString(R.string.app_share_resource_template, resource.getName()));
                        webpage.setSecondTitle(context.getString(R.string.app_share_resource_template, resource.getName()));
                    }
                    shareObject = webpage;
                    break;
                }
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS: {
                    ShareWebpage webpage = new ShareWebpage(resource.getUrl());
                    webpage.setTitle(resource.getName());
                    byte[] data = ImageFetcher.asInstance().syncGetBitmapByte(resource.getImage(), 70, 70);
                    if (data != null) {
                        Logger.d("share", "image length : " + data.length);
                    }
                    webpage.setThumbnail(data);
                    webpage.setMessage(resource.getName());
                    if (KitchenApi.TYPE_KITCHENWARE.equals(resource.getType())) {
                        webpage.setSecondTitle(context.getString(R.string.app_share_resource_template_ware, resource.getName()));
                    } else {
                        webpage.setSecondTitle(context.getString(R.string.app_share_resource_template, resource.getName()));
                    }
                    shareObject = webpage;
                    break;
                }
            }
        }

        listener.onPrepared(shareObject);
    }

    /**
     * 处理Web端的分享数据
     *
     * @param context
     * @param platformType
     * @param resource
     * @param listener
     */
    public static void prepareData(Context context, int platformType, WebViewFragment.WebShareData resource, OnPrepareListener listener) {
        IShareObject shareObject = null;
        if (resource != null) {
            switch (platformType) {
                case PlatformManager.PLATFORM_WEBLOG: {
                    ShareWebpage webpage = new ShareWebpage(resource.getRedirectUrl());
                    webpage.setTitle(resource.getTitle());
                    Bitmap data = ImageFetcher.asInstance().syncGetBitmap(resource.getImage(), 150, 150);
                    webpage.setBitmap(data);
                    webpage.setMessage(resource.getDesc());
                    webpage.setSecondTitle(resource.getDesc());
                    shareObject = webpage;
                    break;
                }
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS: {
                    ShareWebpage webpage = new ShareWebpage(resource.getRedirectUrl());
                    webpage.setTitle(resource.getTitle());
                    byte[] data = ImageFetcher.asInstance().syncGetBitmapByte(resource.getImage(), 70, 70);
                    if (data != null) {
                        Logger.d("share", "image length : " + data.length);
                    }
                    webpage.setThumbnail(data);
                    webpage.setMessage(resource.getTitle());
                    webpage.setSecondTitle(resource.getDesc());
                    shareObject = webpage;
                    break;
                }
            }
        }

        listener.onPrepared(shareObject);
    }

    /**
     * 处理Web端的分享数据
     *
     * @param context
     * @param platformType
     * @param dish
     * @param listener
     */
    public static void prepareData(Context context, int platformType, Dish dish, OnPrepareListener listener) {
        IShareObject shareObject = null;
        if (dish != null) {
            switch (platformType) {
                case PlatformManager.PLATFORM_WEBLOG: {
                    ShareWebpage webpage = new ShareWebpage(dish.getUrl());
                    webpage.setTitle("上味库买“" + dish.getTitle() + "”,3分钟变大厨!");
                    Bitmap data = ImageFetcher.asInstance().syncGetBitmap(dish.getImage(), 150, 150);
                    webpage.setBitmap(data);
                    webpage.setMessage("味库－生鲜半成品净菜平台，您手机上的菜市场，千道菜品，100%新鲜，冷链到家。");
                    webpage.setSecondTitle("味库－生鲜半成品净菜平台，您手机上的菜市场，千道菜品，100%新鲜，冷链到家。");
                    shareObject = webpage;
                    break;
                }
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS: {
                    ShareWebpage webpage = new ShareWebpage(dish.getUrl());
                    webpage.setTitle("上味库买“" + dish.getTitle() + "”,3分钟变大厨!");
                    byte[] data = ImageFetcher.asInstance().syncGetBitmapByte(dish.getImage(), 70, 70);
                    if (data != null) {
                        Logger.d("share", "image length : " + data.length);
                    }
                    webpage.setThumbnail(data);
                    webpage.setMessage("味库－生鲜半成品净菜平台，您手机上的菜市场，千道菜品，100%新鲜，冷链到家。");
                    webpage.setSecondTitle("味库－生鲜半成品净菜平台，您手机上的菜市场，千道菜品，100%新鲜，冷链到家。");
                    shareObject = webpage;
                    break;
                }
            }
        }

        listener.onPrepared(shareObject);
    }

    /**
     * 处理订单红包分享数据
     *
     * @param context
     * @param platformType
     * @param shareState
     * @param listener
     */
    public static void prepareData(Context context, int platformType, ShareState shareState, OnPrepareListener listener) {
        IShareObject shareObject = null;
        if (shareState != null) {
            switch (platformType) {
                case PlatformManager.PLATFORM_WEBLOG: {
                    ShareWebpage webpage = new ShareWebpage(shareState.getLink());
                    webpage.setTitle(shareState.getTitle());
                    Bitmap data = ImageFetcher.asInstance().syncGetBitmap(shareState.getIcon(), 150, 150);
                    webpage.setBitmap(data);
                    webpage.setMessage(shareState.getContent());
                    webpage.setSecondTitle(shareState.getContent());
                    shareObject = webpage;
                    break;
                }
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS: {
                    ShareWebpage webpage = new ShareWebpage(shareState.getLink());
                    webpage.setTitle(shareState.getTitle());
                    byte[] data = ImageFetcher.asInstance().syncGetBitmapByte(shareState.getIcon(), 70, 70);
                    if (data != null) {
                        Logger.d("share", "image length : " + data.length);
                    }
                    webpage.setThumbnail(data);
                    webpage.setMessage(shareState.getContent());
                    webpage.setSecondTitle(shareState.getContent());
                    shareObject = webpage;
                    break;
                }
            }
        }

        listener.onPrepared(shareObject);
    }

    /**
     * 分发处理分享数据
     *
     * @param context
     * @param platformType
     * @param extra
     * @param listener
     */
    public static void dispatchPrepareData(final Context context, final int platformType, final Object extra, final OnPrepareListener listener) {
        if (extra == null) {
            return;
        }

        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            @Override
            public void run() {
                LogGather.setLogMarker(LogGather.MARK.SHARE_DATA, extra);

                if (extra instanceof List) {
                    for (Object data : (List) extra) {
                        if (data instanceof WebViewFragment.WebShareData) {
                            String shareTo = ((WebViewFragment.WebShareData) data).getShareTo();
                            switch (platformType) {
                                case PlatformManager.PLATFORM_WECHAT:
                                    if (WebViewFragment.WebShareData.SHARE_TO_WEIXIN.equals(shareTo)) {
                                        performData(data, context, platformType, listener);
                                        return;
                                    }
                                    break;
                                case PlatformManager.PLATFORM_WECHAT_FRIENDS:
                                    if (WebViewFragment.WebShareData.SHARE_TO_WEIXIN_FRIENDS.equals(shareTo)) {
                                        performData(data, context, platformType, listener);
                                        return;
                                    }
                                    break;
                                case PlatformManager.PLATFORM_WEBLOG:
                                    if (WebViewFragment.WebShareData.SHARE_TO_WEIBO.equals(shareTo)) {
                                        performData(data, context, platformType, listener);
                                        return;
                                    }
                                    break;
                            }
                        }
                    }
                } else {
                    performData(extra, context, platformType, listener);
                }

            }
        });

    }

    private static void performData(Object extra, Context context, int platformType, OnPrepareListener listener) {
        if (extra instanceof FoodDetail) {
            prepareData(context, platformType, (FoodDetail) extra, listener);
        } else if (extra instanceof PartyDetail) {
            prepareData(context, platformType, (PartyDetail) extra, listener);
        } else if (extra instanceof Topic) {
            prepareData(context, platformType, (Topic) extra, listener);
        } else if (extra instanceof CookShow) {
            prepareData(context, platformType, (CookShow) extra, listener);
        } else if (extra instanceof FoodResource) {
            prepareData(context, platformType, (FoodResource) extra, listener);
        } else if (extra instanceof Dish) {
            prepareData(context, platformType, (Dish) extra, listener);
        } else if (extra instanceof WebViewFragment.WebShareData) {
            prepareData(context, platformType, (WebViewFragment.WebShareData) extra, listener);
        } else if (extra instanceof ShareState) {
            prepareData(context, platformType, (ShareState) extra, listener);
        }
    }

    public interface OnPrepareListener {

        public void onPrepared(IShareObject object);
    }
}
