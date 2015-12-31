package cn.wecook.app.main.kitchen.detail;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.policy.KitchenHomePolicy;
import com.wecook.uikit.view.BaseView;

import cn.wecook.app.R;

/**
 * 食材资源头
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/16/14
 */
public class KitchenResourceHeadView extends BaseView {

    private ImageView mImageView;

    private TextView mDescription;

    private TextView mButtonAdd;

    public KitchenResourceHeadView(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = (ImageView) findViewById(R.id.app_kitchen_detail_image);
        mDescription = (TextView) findViewById(R.id.app_kitchen_detail_desc);
        mButtonAdd = (TextView) findViewById(R.id.app_kitchen_detail_button);
    }

    @Override
    public void updateView(final Object obj) {
        super.updateView(obj);

        if (obj != null && obj instanceof FoodResource) {
            ImageFetcher.asInstance().load(((FoodResource) obj).getImage(), mImageView,
                    R.drawable.app_pic_default_resource);
            mDescription.setText(((FoodResource) obj).getDescription());
            String createTime = ((FoodResource) obj).getCreateTime();

            if (!StringUtils.isEmpty(createTime) && !"0".equals(createTime)) {
                mButtonAdd.setText(getResources().getString(R.string.app_kitchen_detail_added));
                mButtonAdd.setSelected(true);
                mButtonAdd.setOnClickListener(null);
            } else {
                mButtonAdd.setText(getResources().getString(R.string.app_kitchen_detail_add_to));
                mButtonAdd.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mButtonAdd.setText(getResources().getString(R.string.app_kitchen_detail_added));
                        mButtonAdd.setSelected(true);
                        mButtonAdd.setOnClickListener(null);
                        KitchenHomePolicy.getInstance().add((FoodResource) obj);
                    }
                });
            }
        }
    }

}
