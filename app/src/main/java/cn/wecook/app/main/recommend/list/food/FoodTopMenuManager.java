package cn.wecook.app.main.recommend.list.food;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.sdk.api.model.FoodTopMenu;
import com.wecook.sdk.api.model.FoodTopMenus;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;

/**
 * 顶部Menu控件
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/9
 */
public class FoodTopMenuManager {
    private View view;
    private FoodTopMenus data;
    private List<View> views = new ArrayList<View>();
    /**
     * 设置type改变的监听
     */
    private TypeChangeListener listener;

    public FoodTopMenuManager(View view) {
        this(view, null);
    }

    public FoodTopMenuManager(View view, FoodTopMenus data) {
        if (view == null) {
            throw new IllegalStateException("菜谱顶部Menu的view不能为null");
        }
        this.view = view;
        setData(data);
        initView(view);
    }

    /**
     * 初始化view
     *
     * @param view
     */
    private void initView(View view) {
        views.clear();
        ViewGroup view01 = (ViewGroup) view.findViewById(R.id.app_food_top_menu_1);
        ViewGroup view02 = (ViewGroup) view.findViewById(R.id.app_food_top_menu_2);
        ViewGroup view03 = (ViewGroup) view.findViewById(R.id.app_food_top_menu_3);
        ViewGroup view04 = (ViewGroup) view.findViewById(R.id.app_food_top_menu_4);
        views.add(view01);
        views.add(view02);
        views.add(view03);
        views.add(view04);
    }

    /**
     * 设置数据
     *
     * @param data
     */
    public void setData(FoodTopMenus data) {
        this.data = data;
        updateView(data);
    }

    /**
     * 更新view数据
     *
     * @param data
     */
    private void updateView(FoodTopMenus data) {
        if (data != null && data.getList().getCountOfList() >= views.size()) {
            ApiModelList<FoodTopMenu> list = data.getList();
            for (int i = 0; i < list.getCountOfList(); i++) {
                setDataToView(views.get(i), list.getItem(i));
            }
        }
    }

    /**
     * 设置具体的内容
     *
     * @param view
     * @param item
     */
    private void setDataToView(View view, final FoodTopMenu item) {
        ItemViewHolder itemViewHolder = initItemView(view);
        ImageFetcher.asInstance().load(item.getIcon(), itemViewHolder.icon);
        itemViewHolder.name.setText(item.getName());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.typeChange(item);
            }
        });
    }

    public void setTypeChangeListener(TypeChangeListener listener) {
        this.listener = listener;
    }

    /**
     * 初始化itemView
     *
     * @param view
     */
    private ItemViewHolder initItemView(View view) {
        ItemViewHolder itemViewHolder = new ItemViewHolder();
        itemViewHolder.icon = (ImageView) view.findViewById(R.id.app_food_top_menu_item_icon);
        itemViewHolder.name = (TextView) view.findViewById(R.id.app_food_top_menu_item_text);
        return itemViewHolder;
    }

    interface TypeChangeListener {
        public void typeChange(FoodTopMenu result);
    }

    class ItemViewHolder {
        ImageView icon;
        TextView name;
    }

}
