package cn.wecook.app.adapter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Contact;
import com.wecook.sdk.api.model.PartyDetail;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.features.webview.WebViewFragment;

/**
 * 活动信息
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/20/14
 */
public class PartyInfoAdapter extends UIAdapter<PartyInfoAdapter.Info> {

    public PartyInfoAdapter(BaseFragment fragment, PartyDetail data) {
        super(fragment.getContext(), R.layout.listview_item_info, getPartyInfo(fragment, data));
    }

    @Override
    public void updateView(int position, int viewType, Info data, Bundle extra) {
        super.updateView(position, viewType, data, extra);

        TextView label = (TextView) findViewById(R.id.app_info_label);
        TextView content = (TextView) findViewById(R.id.app_info_content);
        ImageView icon = (ImageView) findViewById(R.id.app_info_icon);
        ImageView indicator = (ImageView) findViewById(R.id.app_info_indicator);

        label.setText(data.label);
        content.setText(data.content);
        icon.setImageResource(data.drawableId);
        indicator.setVisibility(data.clickCallback != null ? View.VISIBLE : View.GONE);

        getItemView().setOnClickListener(data.clickCallback);
    }

    public static class Info {

        private int drawableId;

        private String label;

        private Spannable content;

        private View.OnClickListener clickCallback;
    }

    /**
     * 获得信息列表
     *
     *
     * @param context
     * @param detail
     * @return
     */
    private static List<Info> getPartyInfo(final BaseFragment fragment, final PartyDetail detail) {
        List<Info> infoList = new ArrayList<Info>();
        if (detail != null) {
            final Contact contact = detail.getContact();
            if (contact != null && !StringUtils.isEmpty(contact.getAddress())) {
                Info address = new Info();
                address.drawableId = R.drawable.app_ic_label_city;
                address.label = "地点：";
                address.content = new SpannableString(contact.getAddress());
                address.clickCallback = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString(WebViewFragment.EXTRA_URL, WebViewActivity.ACTIVITY_MAP_URL_PRE + detail.getId());
                        Intent intent = new Intent(fragment.getContext(), WebViewActivity.class);
                        intent.putExtras(bundle);
                        fragment.startActivity(intent);
                    }
                };
                infoList.add(address);
            }

            if (!StringUtils.isEmpty(detail.getStartTime())
                    && !StringUtils.isEmpty(detail.getEndTime())) {
                Info time = new Info();
                time.drawableId = R.drawable.app_ic_label_time;
                time.label = "时间：";
                String startTime = StringUtils.formatTime(Long.parseLong(detail.getStartTime()),
                        "yyyy年M月d日 HH:mm");
                String endTime = StringUtils.formatTime(Long.parseLong(detail.getEndTime()),
                        "yyyy年M月d日 HH:mm");
                time.content = new SpannableString("从" + startTime + "\n到" + endTime);
                infoList.add(time);
            }

            if (contact != null && !StringUtils.isEmpty(contact.getTel())) {
                Info tel = new Info();
                tel.drawableId = R.drawable.app_ic_label_telephone;
                tel.label = "电话：";
                tel.content = new SpannableString(contact.getTel());
                tel.content.setSpan(new ForegroundColorSpan(fragment.getResources()
                        .getColor(R.color.uikit_font_blue)), 0, contact.getTel().length()
                        , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tel.clickCallback = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse("tel:" + contact.getTel());
                        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                        fragment.getContext().startActivity(intent);
                    }
                };
                infoList.add(tel);
            }

            if (!StringUtils.isEmpty(detail.getPrice())) {
                Info price = new Info();
                price.drawableId = R.drawable.app_ic_label_price;
                price.label = "费用：";
                price.content = new SpannableString(detail.getPrice());
                infoList.add(price);
            }

            if (!StringUtils.isEmpty(detail.getOrganizer())) {
                Info organizer = new Info();
                organizer.drawableId = R.drawable.app_ic_label_sponsor;
                organizer.label = "主办方：";
                organizer.content = new SpannableString(detail.getOrganizer());
                infoList.add(organizer);
            }

            if (contact != null && !StringUtils.isEmpty(contact.getWeixin())) {
                Info weixin = new Info();
                weixin.drawableId = R.drawable.app_ic_label_wexin;
                weixin.label = "微信：";
                weixin.content = new SpannableString(contact.getWeixin());
                infoList.add(weixin);
            }

            if (contact != null && !StringUtils.isEmpty(contact.getQq())) {
                Info qq = new Info();
                qq.drawableId = R.drawable.app_ic_label_qq;
                qq.label = "QQ：";
                qq.content = new SpannableString(contact.getQq());
                infoList.add(qq);
            }

            if (!StringUtils.isEmpty(detail.getSource())) {
                Info qq = new Info();
                qq.drawableId = R.drawable.app_ic_label_source;
                qq.label = "来源：";
                qq.content = new SpannableString(detail.getSource());
                qq.content.setSpan(new ForegroundColorSpan(fragment.getResources()
                        .getColor(R.color.uikit_font_blue)), 0, detail.getSource().length()
                        , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                qq.clickCallback = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString(WebViewFragment.EXTRA_URL, detail.getSourceUrl());
                        fragment.startActivity(new Intent(fragment.getContext(), WebViewActivity.class), bundle);
                    }
                };
                infoList.add(qq);
            }
        }
        return infoList;
    }
}
