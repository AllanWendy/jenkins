package cn.wecook.app.features.debug;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.userinfo.UserProperties;

import cn.wecook.app.R;

/**
 * 手机设备信息
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/12/15
 */
public class PhoneInfoDebugFragment extends BaseDebugFragment {

    private View mRootView;

    @Override
    public String getTitle() {
        return "手机设备信息";
    }

    @Override
    protected View onCreateInnerView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mRootView = inflater.inflate(R.layout.fragment_debug_phone_info, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = (TextView) view.findViewById(R.id.app_debug_info);
        textView.setText(getBuildString());
    }

    private String getBuildString() {
        String build = "";
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        build += "======用户信息======";
        build += "\n用户ID : " + UserProperties.getUserId();
        build += "\n用户名 : " + UserProperties.getUserName();
        build += "\n用户手机 : " + UserProperties.getUserPhone();
        build += "\n======屏幕信息======";
        build += "\ndm.density : " + dm.density;
        build += "\ndm.densityDpi : " + dm.densityDpi;
        build += "\ndm.xdpi : " + dm.xdpi;
        build += "\ndm.ydpi : " + dm.ydpi;
        build += "\n屏幕分辨率为:" + dm.widthPixels + " * " + dm.heightPixels;
        build += "\n======设备信息======";
        build += "\nBuild.BRAND : " + Build.BRAND;
        build += "\nBuild.CPU_ABI : " + Build.CPU_ABI;
        build += "\nBuild.CPU_ABI2 : " + Build.CPU_ABI2;
        build += "\nBuild.DEVICE : " + Build.DEVICE;
        build += "\nBuild.DISPLAY : " + Build.DISPLAY;
        build += "\nBuild.HARDWARE : " + Build.HARDWARE;
        build += "\nBuild.HOST : " + Build.HOST;
        build += "\nBuild.ID : " + Build.ID;
        build += "\nBuild.MANUFACTURER : " + Build.MANUFACTURER;
        build += "\nBuild.MODEL : " + Build.MODEL;
        build += "\nBuild.PRODUCT : " + Build.PRODUCT;
        build += "\nBuild.SERIAL : " + Build.SERIAL;
        build += "\nBuild.TIME : " + StringUtils.formatTime(Build.TIME, "yyyy.MM.dd hh:mm:ss");
        build += "\nBuild.USER : " + Build.USER;
        build += "\nBuild.VERSION.RELEASE : " + Build.VERSION.RELEASE;
        build += "\nBuild.VERSION.SDK_INT : " + Build.VERSION.SDK_INT;
        build += "\nBuild.VERSION.SDK : " + Build.VERSION.SDK;
        build += "\nBuild.VERSION.CODENAME : " + Build.VERSION.CODENAME;
        build += "\nBuild.VERSION.INCREMENTAL : " + Build.VERSION.INCREMENTAL;
        build += "\n======软件版本信息======";
        build += "\nDeviceId : " + PhoneProperties.getDeviceId();
        build += "\nDeviceOs : " + PhoneProperties.getDeviceOs();
        build += "\nDeviceOsVersion : " + PhoneProperties.getDeviceOsVersion();
        build += "\nDebugVersion : " + PhoneProperties.getDebugVersionName();
        build += "\nVersion : " + PhoneProperties.getVersionName();
        build += "\nChannel : " + PhoneProperties.getChannel();
        return build;
    }
}
