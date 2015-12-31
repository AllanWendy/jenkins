package cn.wecook.app.main.home.setting;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.uikit.fragment.BaseTitleFragment;

import cn.wecook.app.R;

/**
 * 商务合作
 * Created by LK on 2015/9/21.
 */
public class BusinessCooperationFragment extends BaseTitleFragment {
    private ImageView businessImg;
    private TextView tvEmail;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_business_cooperation, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getTitleBar().setTitle("商务合作");

        businessImg = (ImageView) view.findViewById(R.id.app_business_cooperation_img);
        tvEmail = (TextView) view.findViewById(R.id.app_business_cooperation_email);
        ScreenUtils.resizeView(businessImg, ScreenUtils.getScreenWidthInt(), 225 / 375f);

        String img_url = "http://u1.wecook.cn/images/20150923/56024f3481a19.jpg";
        ImageFetcher.asInstance().load(img_url, businessImg);
        tvEmail.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
    }
}
