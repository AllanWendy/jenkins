package cn.wecook.app.features.city;

import android.os.Bundle;

import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

/**
 * Created by shan on 2015/8/29.
 */
public class CityActivity extends BaseSwipeActivity {


    @Override
    protected BaseFragment onCreateFragment(Bundle intentBundle) {
        CityFragment addressFragment = new CityFragment();
        return addressFragment;
    }
}
