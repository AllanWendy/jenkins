package cn.wecook.app.main.home.message;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Message;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.MessageQueuePolicy;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.fragment.ApiModelListFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.features.deliver.WecookLink;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.home.user.UserPageFragment;

/**
 * 消息列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-3/9/15
 */
public class MessageFragment extends ApiModelListFragment<Message> {

    private TitleBar.ActionTextView readAll;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.app_title_my_message);

        readAll = new TitleBar.ActionTextView(getContext(), "全部已读");
        readAll.setEnabled(false);
        readAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserProperties.isLogin()) {
                    if (MessageQueuePolicy.getInstance().hasNewMessage()) {
                        MessageQueuePolicy.getInstance().readAllMessage(new ApiCallback<State>() {
                            @Override
                            public void onResult(State result) {
                                if (result.available()) {
                                    notifyDataSetChanged();
                                }
                            }
                        });
                    }
                } else {
                    startActivity(new Intent(getContext(), UserLoginActivity.class));
                }
            }
        });
        getTitleBar().addActionView(readAll);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = position - getListView().getHeaderViewsCount();
                final Message message = getItem(pos);
                if (!message.isRead()) {
                    message.setIsRead(true);
                    MessageQueuePolicy.getInstance().readMessage(message.getId(), new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            if (!result.available()) {
                                if (getAdapter() != null) {
                                    getAdapter().notifyDataSetChanged();
                                }
                            }
                        }
                    });
                    if (getAdapter() != null) {
                        getAdapter().notifyDataSetChanged();
                    }
                }

                if(Message.TYPE_SYSTEM.equals(message.getType())) {
                    String url = message.getUrl();
                    if(StringUtils.containWith(url,"?")){
                        url += "&uid=" + UserProperties.getUserId()+"&wid=" + PhoneProperties.getDeviceId();
                    }else{
                        url += "?uid=" + UserProperties.getUserId()+"&wid=" + PhoneProperties.getDeviceId();
                    }
                    WecookLink.sendLink(url);
                } else {
                    WecookLink.sendLink(message.getUrl());
                }
            }
        });
    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<Message>> callback) {
        if (isPullDownToRefreshing()) {
            MessageQueuePolicy.getInstance().clearMessageList();
        }
        MessageQueuePolicy.getInstance().getNewMessageList(page, pageSize, callback);
    }

    @Override
    protected void onUpdateList(int select) {
        super.onUpdateList(select);
        if (MessageQueuePolicy.getInstance().hasNewMessage()) {
            readAll.setEnabled(true);
            readAll.setTextColor(getResources().getColor(R.color.uikit_font_orange));
        } else {
            readAll.setEnabled(false);
            readAll.setTextColor(getResources().getColor(R.color.uikit_font_grep_light));
        }

        readAll.setVisibility(getListData().size() > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void updateItemView(View view, int position, int viewType, final Message data, Bundle extra) {

        ImageView avatar = (ImageView) view.findViewById(R.id.app_message_avatar);
        ImageView image = (ImageView) view.findViewById(R.id.app_message_image);
        TextView senderName = (TextView) view.findViewById(R.id.app_message_name);
        TextView senderInfo = (TextView) view.findViewById(R.id.app_message_info);
        TextView content = (TextView) view.findViewById(R.id.app_message_content);
        TextView time = (TextView) view.findViewById(R.id.app_message_time);

        ImageFetcher.asInstance().load(data.getImage(), image);

        if (data.getSender() != null) {
            User sender = data.getSender();
            ImageFetcher.asInstance().load(sender.getAvatar(), avatar);
            senderName.setText(sender.getNickname());
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(UserPageFragment.EXTRA_USER, data.getSender());
                    next(UserPageFragment.class, bundle);
                }
            });
        } else {
            avatar.setOnClickListener(null);
        }

        senderInfo.setText(data.getInfo());
        if (StringUtils.isEmpty(data.getContent())) {
            content.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.VISIBLE);
        }
        content.setText(data.getContent());

        String formatTime = StringUtils.formatTimeWithNearBy(Long.parseLong(data.getCreateTime()), "MM-dd");
        time.setText(formatTime);

        if (data.isRead()) {
            senderName.setTextColor(Color.parseColor("#ffbcbcbc"));
            senderInfo.setTextColor(Color.parseColor("#ffbcbcbc"));
            content.setTextColor(Color.parseColor("#ffbcbcbc"));
            time.setTextColor(Color.parseColor("#ffbcbcbc"));
        } else {
            senderName.setTextColor(getResources().getColor(R.color.uikit_font_grep_dark));
            senderInfo.setTextColor(getResources().getColor(R.color.uikit_font_grep));
            content.setTextColor(getResources().getColor(R.color.uikit_font_grep_dark));
            time.setTextColor(getResources().getColor(R.color.uikit_font_grep));
        }
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_item_message;
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView view = super.getEmptyView();
        if (getActivity() != null) {
            view.setTitle(getString(R.string.app_empty_title_message));
            view.setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_message);
        }
    }
}
