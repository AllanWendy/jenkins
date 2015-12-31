package cn.wecook.app.main.dish.order;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.uper.UpperManager;
import com.wecook.common.utils.MediaStorePicker;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.model.Image;
import com.wecook.sdk.api.model.Order;
import com.wecook.sdk.api.model.ShopCartDish;
import com.wecook.sdk.api.model.ShopCartRestaurant;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.features.message.MessageEventReceiver;
import cn.wecook.app.features.pick.PickActivity;
import cn.wecook.app.features.picture.MultiPictureActivity;

/**
 * 订单评论提交
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/8/4
 */
public class DishOrderEvaluateFragment extends BaseTitleFragment {

    public static final String EXTRA_ORDER = "extra_order";
    private Order mOrder;
    private MergeAdapter mergeAdapter;
    private ListView mListView;
    private ApiModelList<ShopCartDish> dishList;
    private boolean mHasRegisterReceiver = false;
    private List<Image> currentData;
    private DishOrderEvaluateAdapter dishEvaluateAdapter;

    private List<Image> updateData = new ArrayList<>();

    private ArrayList<Image> updateQuese = new ArrayList<>();

    private HashMap<String, String> cacheMap = new HashMap<>();
    private RatingBar mDistributionStar;
    private EditText mDistributionEdit;
    private TextView mAnonymousView;
    private LoadingDialog loadingDialog;
    private boolean isCanCommit;
    private UpperManager.SimpleUpperListener listener = new UpperManager.SimpleUpperListener() {

        @Override
        public void onStart(String path) {
            super.onStart(path);
            Logger.i("Simon", "开始上传：" + path);
            //检查网络
            if (checkNetwork()) {
                return;
            }
        }

        @Override
        public void onEnd(String path, String id, String url, boolean success) {
            if (!success) {
                //上传失败
                Logger.i("Simon", "上传失败：" + path);
                //检查网络
                if (checkNetwork()) {
                    return;
                }
            } else {
                Logger.i("Simon", "上传成功：" + path);
                //上传成功
                cacheMap.put(path, url + ";" + id);
            }

        }

        @Override
        public void onFinish(int success, int total) {
            super.onFinish(success, total);
            UpperManager.asInstance().removeUpperListener(this);
            Logger.i("Simon", "上传完成：success=" + success + "    total=" + total);
            //检查网络
            if (checkNetwork()) {
                return;
            }
            // 提交评价
            if (isCanCommit) {
                if (success == total) {
                    checkUpdateData();
                } else {
                    ToastAlarm.show("上传图片失败");
                    loadingDialog.dismiss();
                    isCanCommit = false;
                    checkUpdateData();
                }
            } else {
                checkUpdateData();
            }
        }

        @Override
        public void onCancel(String path) {
            Logger.i("Simon", "取消上传：" + path);
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MessageEventReceiver.ACTION_PICK_MULTI_PIC.equals(intent.getAction())) {
                ArrayList<MediaStorePicker.MediaImage> parcelableExtra = intent.getParcelableArrayListExtra(MessageEventReceiver.PARAM_LIST_BITMAP);
                if (parcelableExtra != null) {
                    List<Image> resultImages = new ArrayList<>();
                    for (int i = 0; i < parcelableExtra.size(); i++) {
                        String path = parcelableExtra.get(i).path;
                        Image image = new Image();
                        image.setUrl(path);
                        resultImages.add(image);
                    }
                    //更新上传数据
                    notifyUpdateDataChange(currentData, resultImages);
                    if (currentData != null) {
                        currentData.clear();
                        currentData.addAll(resultImages);
                    }
                    dishEvaluateAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOrder = (Order) bundle.getSerializable(EXTRA_ORDER);
        }
        registerReceiver(getContext());
        setTitle("评价");
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dishList = new ApiModelList<ShopCartDish>(new ShopCartDish());
        loadingDialog = new LoadingDialog(getContext());
        loadingDialog.cancelable(false);
        return inflater.inflate(R.layout.fragment_dish_order_evaluate, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PullToRefreshListView pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.uikit_listview);
        mListView = pullToRefreshListView.getRefreshableView();
        getTitleBar().setBackgroundColor(getResources().getColor(R.color.uikit_white));
        initBottomView(view);
        initData();
        updateView();
    }

    /**
     * 提交订单评价
     */
    private void commitOrderEvaluate() {
        //检查网络
        if (checkNetwork()) {
            return;
        }
        //提交
        String idAndScores = "";//菜品id及评分
        List<String> commentList = new ArrayList<String>();//评论内容详情
        List<String> dishMediaIdsList = new ArrayList<String>();//图片media_ids集合
        String delivery_score = "";//物流评分
        String delivery_content = "";//评论物流内容
        String anonymous = "1";//是否匿名；1，匿名  2不匿名（默认匿名）


        if (mAnonymousView != null) {
            anonymous = mAnonymousView.isSelected() ? "1" : "2";
        }
        if (mDistributionStar != null) {
            delivery_score = mDistributionStar.getRating() + "";
        }
        if (mDistributionEdit != null) {
            delivery_content = mDistributionEdit.getText().toString();
        }

        for (ShopCartDish dish : dishList.getList()) {
            idAndScores += dish.getDishId() + "," + dish.getGrade() + ";";
            if (StringUtils.isEmpty(dish.getComment())) {
                if (dish.getGrade() >= 4) {
                    dish.setComment("强烈推荐！");
                } else if (2 <= dish.getGrade() && dish.getGrade() < 4) {
                    dish.setComment("基本满意。");
                } else {
                    dish.setComment("差评！");
                }
            }
            commentList.add(dish.getComment());
            List<Image> images = dish.getImages();
            if (images != null && images.size() > 0) {
                StringBuffer dish_media_ids = new StringBuffer();
                for (int i = 0; i < images.size(); i++) {
                    dish_media_ids.append(images.get(i).getId() + ",");
                }
                dishMediaIdsList.add(dish_media_ids.toString());
            }
        }
        if (!StringUtils.isEmpty(idAndScores)) {
            idAndScores = idAndScores.substring(0, idAndScores.length() - 1);
        }

        if (!StringUtils.isEmpty(idAndScores)) {

            CommentApi.createDishComment(mOrder.getOrderId(), idAndScores, commentList, dishMediaIdsList, delivery_score, delivery_content, anonymous,
                    new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            loadingDialog.dismiss();
                            if (result.available()) {
                                ToastAlarm.show("评论成功");
                                //返回并刷新界面
                                back();
                            }
                        }
                    });
        }
    }

    /**
     * 初始化底部试图
     *
     * @param view
     */
    private void initBottomView(View view) {
        TextView mCommitView = (TextView) view.findViewById(R.id.app_dish_order_evaluate_commit);
        mAnonymousView = (TextView) view.findViewById(R.id.app_dish_order_evaluate_anonymous);
        mAnonymousView.setSelected(true);
        mCommitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 提交评论
                if (checkNetwork()) {
                    return;
                }
                loadingDialog.show();
                isCanCommit = true;
                if (!UpperManager.asInstance().isWorking()) {
                    checkUpdateData();
                }
            }
        });
        mAnonymousView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击匿名
                if (v.isSelected()) {
                    //非匿名
                    v.setSelected(false);
                } else {
                    //匿名
                    v.setSelected(true);

                }
            }
        });

    }


    /**
     * 初始化数据
     */
    private void initData() {
        if (mOrder != null) {
            ApiModelList<ShopCartRestaurant> restaurantList = mOrder.getRestaurantList();
            if (restaurantList != null) {
                for (int i = 0; i < restaurantList.getCountOfList(); i++) {
                    ShopCartRestaurant restaurant = restaurantList.getItem(i);
                    dishList.addAll(restaurant.getShopCartDishes());
                }
            }
            if (dishList != null && dishList.getList().size() > 0) {
                for (ShopCartDish dish : dishList.getList()) {
                    dish.setGrade("5");
                }
            }
        }
    }

    /**
     * 更新或加载view
     */
    private void updateView() {
        if (dishList != null) {
            mergeAdapter = null;
            mergeAdapter = new MergeAdapter();
            //添加餐厅评价模块
            if (dishList.getList() != null && dishList.getList().size() > 0) {
                addEvaluateAdapter(dishList.getList());
            }

            //添加配送服务模块
            addDistributionView();
            mListView.setAdapter(mergeAdapter);
            mergeAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 添加配送服务模块
     */
    private void addDistributionView() {
        View mDistributionView = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_order_distribution, null);
        if (mergeAdapter != null) {
            //添加顶部间隙
            View spaceView = new View(getContext());
            ListView.LayoutParams layoutParams = new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(10));
            spaceView.setLayoutParams(layoutParams);
            mergeAdapter.addView(spaceView);

            mDistributionEdit = (EditText) mDistributionView.findViewById(R.id.app_order_evaluate_input);
            mDistributionStar = (RatingBar) mDistributionView.findViewById(R.id.app_order_evaluate_star);
            mDistributionStar.setRating(5);
            mDistributionStar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (rating <= 3) {
                        mDistributionEdit.setVisibility(View.VISIBLE);
                    } else {
                        mDistributionEdit.setVisibility(View.GONE);
                    }
                }
            });
            mDistributionEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    double textLength = StringUtils.chineseLength(s.toString());
                    if (textLength > 60) {
                        ToastAlarm.show("最多填写60字");
                        String comment = StringUtils.getChineseStringByMaxLength(s.toString(), 60);
                        mDistributionEdit.setText(comment);
                        mDistributionEdit.setSelection(comment.length());
                    }
                }
            });
            mergeAdapter.addView(mDistributionView);
            //添加底部间隙
            View spaceBottomView = new View(getContext());
            ListView.LayoutParams layoutBottomParams = new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(30));
            spaceBottomView.setLayoutParams(layoutBottomParams);
            mergeAdapter.addView(spaceBottomView);

        }
    }

    /**
     * 添加adapter
     *
     * @param list
     */
    private void addEvaluateAdapter(List<ShopCartDish> list) {
        if (dishEvaluateAdapter == null && mergeAdapter != null) {
            dishEvaluateAdapter = new DishOrderEvaluateAdapter(getContext(), list);
            mergeAdapter.addAdapter(dishEvaluateAdapter);
        }
    }

    /**
     * 查看数据是否有需要上传的数据 有即上传
     */
    private void checkUpdateData() {
        reset();
        if (dishList != null && dishList.getList() != null && dishList.getList().size() > 0) {
            List<ShopCartDish> list = dishList.getList();
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    List<Image> images = list.get(i).getImages();
                    if (images != null && images.size() > 0) {
                        updateData.addAll(images);
                    }
                }
            }
        }

        //上传图片
        doUpdate();

    }

    private void doUpdate() {
        //获得缓存
        if (updateData != null && updateData.size() > 0) {
            for (int i = 0; i < updateData.size(); i++) {
                if (cacheMap.containsKey(updateData.get(i).getUrl())) {
                    //获得缓存
                    String result = cacheMap.get(updateData.get(i).getUrl());
                    String[] split = result.split(";");
                    updateData.get(i).setUpdateUrl(split[0]);
                    updateData.get(i).setId(split[1]);
                } else {
                    //加入上传队列
                    updateQuese.add(updateData.get(i));
                }
            }
        }
        //执行上传任务
        doUpdateQuese();
    }

    private void doUpdateQuese() {
        if (updateQuese != null && updateQuese.size() > 0) {
            final String[] updateResult = new String[updateQuese.size()];
            for (int i = 0; i < updateQuese.size(); i++) {
                updateResult[i] = updateQuese.get(i).getUrl();
            }
            if (updateResult.length > 0) {
                AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
                    @Override
                    public void run() {
                        updateImages(updateResult);
                    }
                });
            }
        } else if (isCanCommit) {
            commitOrderEvaluate();
        }
    }

    //重置数据
    public void reset() {
        UpperManager.asInstance().release();
        updateQuese.clear();
        updateData.clear();
    }

    private void registerReceiver(Context context) {
        mHasRegisterReceiver = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageEventReceiver.ACTION_PICK_MULTI_PIC);
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, filter);
    }


    /**
     * 检查上传数据是否发生改变，相应添加或取消上传数据
     *
     * @param oldImages
     * @param newImages
     */
    private void notifyUpdateDataChange(List<Image> oldImages, List<Image> newImages) {
        if (oldImages == null || newImages == null) {
            return;
        }
        ArrayList<String> oldCheckList = new ArrayList<>();
        for (int i = 0; i < oldImages.size(); i++) {
            oldCheckList.add(oldImages.get(i).getUrl());
        }

        ArrayList<String> newCheckList = new ArrayList<>();
        for (int i = 0; i < newImages.size(); i++) {
            newCheckList.add(newImages.get(i).getUrl());
        }
        for (int i = 0; i < newImages.size(); i++) {
            if (!oldCheckList.contains(newImages.get(i).getUrl())) {
                //加入上传队列
                Logger.i("Simon", "加入上传队列path=" + newImages.get(i).getUrl());
                updateImages(new String[]{newImages.get(i).getUrl()});
            }
        }
        for (int i = 0; i < oldImages.size(); i++) {
            if (!newCheckList.contains(oldImages.get(i).getUrl())) {
                //取消上传队列
                Logger.i("Simon", "取消上传队列path=" + oldImages.get(i).getUrl());
                UpperManager.asInstance().cancelImage(oldImages.get(i).getUrl());
            }
        }
    }


    private void updateImages(String... imagePathes) {

        UpperManager.asInstance().setUserId(UserProperties.getUserId());
        UpperManager.asInstance().addUpperListener(listener);
        UpperManager.asInstance().upImages(imagePathes);
    }

    private boolean checkNetwork() {
        if (!NetworkState.available() && isCanCommit) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            UpperManager.asInstance().release();
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    ToastAlarm.show("网络无效");
                }
            });
            return true;
        }
        return false;
    }

    private void unregisterReceiver() {
        if (mHasRegisterReceiver) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterReceiver();
    }

    @Override
    public void onCardOut() {
        super.onCardOut();
        reset();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    class DishOrderEvaluateAdapter extends UIAdapter<ShopCartDish> {


        public DishOrderEvaluateAdapter(Context context, List<ShopCartDish> data) {
            super(context, R.layout.listview_item_order_evaluate, data);
        }

        @Override
        public void updateView(int position, int viewType, final ShopCartDish data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            TextView dishName = (TextView) getItemView().findViewById(R.id.app_order_evaluate_dish_name);
            ImageView dishImage = (ImageView) getItemView().findViewById(R.id.app_order_evaluate_dish_image);
            PictureHelpView dishShowImages = (PictureHelpView) getItemView().findViewById(R.id.app_order_evaluate_dish_show_images);
            final EditText dishEvaluateInput = (EditText) getItemView().findViewById(R.id.app_order_evaluate_dish_content_input);
            RatingBar dishStarBar = (RatingBar) getItemView().findViewById(R.id.app_order_evaluate_star);
            HorizontalScrollView pictureLayout = (HorizontalScrollView) getItemView().findViewById(R.id.app_order_evaluate_dish_show_scrollview);

            //设置图片
            pictureLayout.setVisibility(View.VISIBLE);
            if (data.getImages() == null) {
                List<Image> images = new ArrayList<>();
                data.setImages(images);
            }

            dishShowImages.setDataAndIsUpdatePicture(data.getImages(), true);
            dishShowImages.setPictureHelpCallBack(new PictureHelpView.PictureHelpCallBack() {
                @Override
                public void callback(int position, View parent, List<Image> data, boolean isUpdatePicture) {
                    currentData = data;
                    if (isUpdatePicture) {
                        //上传图片——选择图片
                        ArrayList<String> selectedList = new ArrayList<>();
                        for (Image image : data) {
                            selectedList.add(image.getUrl());
                        }
                        Intent intent = new Intent(getContext(), PickActivity.class);
                        intent.putExtra(PickActivity.EXTRA_PICK_TYPE, PickActivity.PICK_MULTI);
                        intent.putExtra(PickActivity.EXTRA_SELECTED_COUNT_MAX, 5);
                        intent.putExtra(PickActivity.EXTRA_SELECTED, selectedList);
                        startActivity(intent);

                    } else {
                        // 跳转查看图片详情
                        Intent intent = new Intent(getContext(), MultiPictureActivity.class);
                        intent.putExtra(MultiPictureActivity.EXTRA_IMAGES, (Serializable) data);
                        intent.putExtra(MultiPictureActivity.EXTRA_FIRST_POS, position);
                        intent.putExtra(MultiPictureActivity.EXTRA_IS_SHOW_TITLE, true);
                        startActivity(intent);
                    }
                }
            });
            if (data != null) {
                dishName.setText(data.getTitle());
                ImageFetcher.asInstance().load(data.getImage(), dishImage);
                dishStarBar.setRating(data.getGrade());
                dishStarBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        if (fromUser) {
                            data.setGrade(rating + "");
                        }
                    }
                });

                dishEvaluateInput.setText(data.getComment());
                dishEvaluateInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        double textLength = StringUtils.chineseLength(s.toString());
                        if (textLength > 0) {
                            String comment = s.toString();
                            if (textLength > 60) {
                                ToastAlarm.show("最多填写60字");
                                comment = StringUtils.getChineseStringByMaxLength(s.toString(), 60);
                                dishEvaluateInput.setText(comment);
                                dishEvaluateInput.setSelection(comment.length());
                            }
                            data.setComment(comment);
                        }
                    }
                });
            }
        }
    }
}