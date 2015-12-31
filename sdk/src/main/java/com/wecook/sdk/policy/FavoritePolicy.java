package com.wecook.sdk.policy;

import android.view.View;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.sdk.api.legacy.FavoriteApi;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.base.Favorable;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 收藏策略
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/29/14
 */
public class FavoritePolicy {

    /**
     * 快速设置收藏功能
     *
     * @param view
     * @param type
     * @param foreignId
     * @param favorable
     * @param showLoginDialog
     */
    public static void favoriteHelper(final View view, final String type, final int foreignId,
                                      final Favorable favorable, final Runnable showLoginDialog) {
        if (view != null && favorable != null) {
            //未登录状态不变化
            if (UserProperties.isLogin()) {
                view.setSelected(favorable.isFav());
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UserProperties.isLogin()) {
                        view.setSelected(!favorable.isFav());
                        favoriteToggle(type, foreignId,
                                favorable.isFav(), new ApiCallback<State>() {
                                    @Override
                                    public void onResult(State result) {
                                        if (result == null || !result.available()) {
                                            //添加收藏或者取消收藏失败的情况下，表示服务端已经收藏或者已经取消收藏，
                                            //则将状态直接同步成服务端
                                            favorable.setFav(!favorable.isFav());
                                        } else {
                                            favorable.setFav(!favorable.isFav());
                                        }
                                        LogGather.onEventFav(type, favorable);
                                    }
                                }
                        );
                    } else {
                        if (showLoginDialog != null) {
                            showLoginDialog.run();
                        }
                    }
                }
            });
        }

    }

    /**
     * @param views
     * @param type
     * @param foreignId
     * @param favorable
     * @param showLoginDialog
     */
    public static void favoriteHelper(final View[] views, final String type, final int foreignId,
                                      final Favorable favorable, final Runnable showLoginDialog) {
        if (views != null) {
            if (favorable != null) {
                for (View view : views) {
                    if (view != null) {
                        view.setSelected(favorable.isFav());
                    }
                }
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        if (UserProperties.isLogin()) {
                            for (View view : views) {
                                if (view != null) {
                                    view.setSelected(!favorable.isFav());
                                }
                            }

                            favoriteToggle(type, foreignId,
                                    favorable.isFav(), new ApiCallback<State>() {
                                        @Override
                                        public void onResult(State result) {
                                            if (result == null || !result.available()) {
                                                //添加收藏或者取消收藏失败的情况下，表示服务端已经收藏或者已经取消收藏，
                                                //则将状态直接同步成服务端
                                                favorable.setFav(!favorable.isFav());
                                            } else {
                                                favorable.setFav(!favorable.isFav());
                                            }
                                            LogGather.onEventFav(type, favorable);
                                        }
                                    }
                            );
                        } else {
                            if (showLoginDialog != null) {
                                showLoginDialog.run();
                            }
                        }
                    }
                };
                for (View view : views) {
                    if (view != null) {
                        view.setOnClickListener(listener);
                    }
                }

            }
        }
    }

    /**
     * 收藏触发开关
     *
     * @param type
     * @param foreignId
     * @param hasFav    是否已经收藏
     * @param callback
     */
    public static void favoriteToggle(String type, int foreignId, boolean hasFav, ApiCallback<State> callback) {
        if (hasFav) {
            //remove
            FavoriteApi.favoriteRemove(type, foreignId, callback);
        } else {
            //add
            FavoriteApi.favoriteAdd(type, foreignId, callback);
        }
    }
}
