package cn.wecook.app.main.recommend.detail.party;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.sdk.api.model.Location;
import com.wecook.sdk.api.model.PartyDetail;
import com.wecook.uikit.view.BaseView;

import cn.wecook.app.R;

/**
 * 小地图界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/20/14
 */
public class PartyMiniMapView extends BaseView {

    private ImageView mapView;

    public PartyMiniMapView(Context context) {
        super(context);
    }

    public PartyMiniMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PartyMiniMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mapView = (ImageView) findViewById(R.id.app_party_detail_mini_map);
    }

    @Override
    public void updateView(final Object obj) {
        super.updateView(obj);
        if (obj != null && obj instanceof PartyDetail) {
            Location location = ((PartyDetail) obj).getLocation();
            if (location != null) {
                String url = "http://api.map.baidu.com/staticimage?center="
                        + location.getLatitude() + "," + location.getLongitude()
                        + "&width=480&height=320&zoom=15";
                Logger.d("map url = " + url);
                ImageFetcher.asInstance().load(url, mapView, -1);
            }
        }
    }
}
