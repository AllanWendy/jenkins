package cn.wecook.app.main.recommend.detail.food;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodDetail;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.view.BaseView;

import cn.wecook.app.R;
import cn.wecook.app.main.home.user.UserPageFragment;

/**
 * 菜谱描述
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodDescriptionView extends BaseView{

    private TextView mDescriptionView;
    private ImageView mSupplierAvatarView;
    private TextView mSupplierNameView;
    private TextView mLookCountView;
    private TextView mFavCountView;
    private TextView mCookCountView;

    private BaseFragment mFragment;

    public FoodDescriptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FoodDescriptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FoodDescriptionView(BaseFragment fragment) {
        super(fragment.getContext());
        mFragment = fragment;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mDescriptionView = (TextView) findViewById(R.id.app_food_detail_desc);
        mSupplierAvatarView = (ImageView) findViewById(R.id.app_food_detail_supplier_avatar);
        mSupplierNameView = (TextView) findViewById(R.id.app_food_detail_supplier_name);
        mLookCountView = (TextView) findViewById(R.id.app_food_detail_look_count);
        mFavCountView = (TextView) findViewById(R.id.app_food_detail_fav_count);
        mCookCountView = (TextView) findViewById(R.id.app_food_detail_cook_count);
    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);

        if (obj != null && obj instanceof FoodDetail) {
            final FoodDetail detail = (FoodDetail) obj;
            if (detail.getFoodRecipe() != null) {
                String desc = detail.getFoodRecipe().getDescription();
                if (StringUtils.isEmpty(desc)) {
                    mDescriptionView.setVisibility(GONE);
                } else {
                    mDescriptionView.setVisibility(VISIBLE);
                    mDescriptionView.setText(desc);
                }
                if (detail.getFoodRecipe().getAuthor() != null) {
                    mSupplierNameView.setText(detail.getFoodRecipe().getAuthor().getNickname());
                    ImageFetcher.asInstance().load(detail.getFoodRecipe().getAuthor().getAvatar(),
                            mSupplierAvatarView, R.drawable.app_pic_default_avatar);
                    mSupplierAvatarView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(UserPageFragment.EXTRA_USER, detail.getFoodRecipe().getAuthor());
                            mFragment.next(UserPageFragment.class, bundle);
                        }
                    });
                } else {
                    mSupplierAvatarView.setOnClickListener(null);
                }
            }

            mLookCountView.setText(detail.getViewCount());
            mFavCountView.setText(detail.getFavouriteCount());
            mCookCountView.setText(detail.getCookingCount());
        }
    }
}
