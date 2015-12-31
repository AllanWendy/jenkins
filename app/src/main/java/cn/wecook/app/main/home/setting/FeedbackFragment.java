package cn.wecook.app.main.home.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.database.DataLibraryManager;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.ChatMessageApi;
import com.wecook.sdk.api.model.Message;
import com.wecook.sdk.api.model.Session;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.dbprovider.AppDataLibrary;
import com.wecook.sdk.dbprovider.tables.MessageTable;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 意见反馈界面
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public class FeedbackFragment extends BaseListFragment {

    private EditText mInputText;
    private TextView mBtnSubmit;

    private PullToRefreshListView mPullListView;
    private ListView mListView;
    private FeedbackAdapter mAdapter;

    private List<Message> mChatContentList = new ArrayList<Message>();

    private LoadMore mLoadMore;
    private MessageTable mMessageTable;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        AppDataLibrary dataLibrary = DataLibraryManager.getDataLibrary(AppDataLibrary.class);
        mMessageTable = dataLibrary.getTable(MessageTable.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feedback, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TitleBar.ActionCoveredTextView report = new TitleBar.ActionCoveredTextView(getContext(), "一键上报");
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportJustClick();
            }
        });
        getTitleBar().addActionView(report);
        mPullListView = (PullToRefreshListView) view.findViewById(R.id.app_feedback_list);
        mListView = mPullListView.getRefreshableView();
        mInputText = (EditText) view.findViewById(R.id.app_feedback_input);
        mBtnSubmit = (TextView) view.findViewById(R.id.app_feedback_submit);

        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double textLength = StringUtils.chineseLength(s.toString());
                if (textLength > 140) {
                    String comment = StringUtils.getChineseStringByMaxLength(s.toString(), 140);
                    mInputText.setText(comment);
                    mInputText.setSelection(comment.length());
                }
            }
        });
        mAdapter = new FeedbackAdapter(getContext(), mChatContentList);
        setListAdapter(mListView, mAdapter);
        mLoadMore = getLoadMore();
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserProperties.isLogin()) {
                    startActivity(new Intent(getContext(), UserLoginActivity.class));
                    return;
                }

                final String submitContent = mInputText.getText().toString();
                if (!StringUtils.isEmpty(submitContent)) {
                    final Message sendMsg = new Message();
                    sendMsg.setUid(UserProperties.getUserId());
                    sendMsg.setContent(submitContent);
                    mChatContentList.add(sendMsg);
                    mAdapter.notifyDataSetChanged();

                    ChatMessageApi.sendChatMessage(submitContent, new ApiCallback<State>() {
                                @Override
                                public void onResult(State result) {
                                    mInputText.setText("");
                                    MessageTable.MessageDB messageDb = new MessageTable.MessageDB();
                                    messageDb.content = submitContent;
                                    messageDb.createTime = System.currentTimeMillis() + "";
                                    messageDb.receiverId = UserProperties.getUserId();
                                    mMessageTable.insert(messageDb);
                                }
                            }
                    );
                }
            }
        });
    }

    private void reportJustClick() {
        if (!UserProperties.isLogin()) {
            startActivity(new Intent(getContext(), UserLoginActivity.class));
            return;
        }
        showLoading();
        String reportContent = "";
        reportContent += "\nuser_id : " + UserProperties.getUserId();
        reportContent += "\nuser_tel : " + (StringUtils.isEmpty(UserProperties.getUserPhone()) ? "" :
                UserProperties.getUserPhone().substring(UserProperties.getUserPhone().length() - 4,
                        UserProperties.getUserPhone().length()));
        reportContent += "\ndevice_id : " + PhoneProperties.getDeviceId();
        reportContent += "\ndebug_version : " + PhoneProperties.getDebugVersionName();
        reportContent += "\nchannel : " + PhoneProperties.getChannel();
        ChatMessageApi.sendChatMessage(reportContent, new ApiCallback<State>() {
                    @Override
                    public void onResult(State result) {
                        if (result.available()) {
                            ToastAlarm.show("上报成功～\n谢谢你的反馈，我们会及时分析你遇到的问题，并作出改善。");
                        }
                        hideLoading();
                    }
                }
        );
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        showLoading();
        if (StringUtils.isEmpty(UserProperties.getUserChatSessionId())) {
            ChatMessageApi.createChatSession(ChatMessageApi.SYS_RECEIVER, new ApiCallback<Session>() {
                @Override
                public void onResult(Session result) {
                    if (result != null && result.available()) {
                        UserProperties.saveSessionId(result.getSessionId());
                        requestChatList();
                    } else {
                        hideLoading();
                    }
                }
            });
        } else {
            requestChatList();
        }
    }

    /**
     * 请求聊天列表
     */
    private void requestChatList() {
        String where = MessageTable.COLUMN_RECEIVER_ID
                + " in (" + ChatMessageApi.SYS_RECEIVER
                + "," + UserProperties.getUserId() + ")";

        List<MessageTable.MessageDB> list = mMessageTable.query(where, null, null);
        for (MessageTable.MessageDB db : list) {
            Message message = new Message(db);
            mChatContentList.add(message);
        }

        ChatMessageApi.fetchNewMessage(1, mLoadMore.getPageSize(), new ApiCallback<ApiModelList<Message>>() {
            @Override
            public void onResult(ApiModelList<Message> result) {
                if (result != null && result.available()) {
                    buildList(result);
                    mChatContentList.addAll(result.getList());
                }
                if (!mChatContentList.isEmpty()) {
                    Collections.sort(mChatContentList, new Comparator<Message>() {
                        @Override
                        public int compare(Message lhs, Message rhs) {
                            return lhs.getCreateTime().compareTo(rhs.getCreateTime());
                        }
                    });

                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
                hideLoading();
            }
        });
    }

    private void buildList(ApiModelList<Message> result) {
        List<MessageTable.MessageDB> dbList = new ArrayList<MessageTable.MessageDB>();
        for (Message msg : result.getList()) {
            MessageTable.MessageDB db = new MessageTable.MessageDB();
            db.messageId = msg.getId();
            db.receiverId = msg.getUid();
            db.createTime = msg.getCreateTime();
            db.content = msg.getContent();
            dbList.add(db);
        }
        if (!dbList.isEmpty()) {
            mMessageTable.batchInsert(dbList);
        }
    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {
        List list = (List) SyncHandler.sync(new SyncHandler.Sync() {
            List list = null;
            boolean waiting = true;

            @Override
            public void syncStart() {
                ChatMessageApi.fetchNewMessage(currentPage, pageSize, new ApiCallback<ApiModelList<Message>>() {
                    @Override
                    public void onResult(ApiModelList<Message> result) {
                        if (result != null && result.available()) {
                            list = result.getList();
                            buildList(result);
                            Collections.sort(list, new Comparator<Message>() {
                                @Override
                                public int compare(Message lhs, Message rhs) {
                                    return lhs.getCreateTime().compareTo(rhs.getCreateTime());
                                }
                            });
                        }
                        waiting = false;
                    }
                });
            }

            @Override
            public boolean waiting() {
                return waiting;
            }

            @Override
            public Object syncEnd() {
                return list;
            }
        });
        return list;
    }

    private class FeedbackAdapter extends UIAdapter<Message> {

        static final int TYPE_SEND = 1;
        static final int TYPE_COME = 2;

        public FeedbackAdapter(Context context, List<Message> data) {
            super(context, data);
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            Message message = getItem(position);
            if (UserProperties.getUserId().equals(message.getUid())) {
                return TYPE_SEND;
            } else {
                return TYPE_COME;
            }
        }

        @Override
        protected View newView(int viewType) {
            if (viewType == TYPE_COME) {
                return LayoutInflater.from(getContext()).inflate(R.layout.view_come_message, null);
            } else if (viewType == TYPE_SEND) {
                return LayoutInflater.from(getContext()).inflate(R.layout.view_send_message, null);
            }
            return super.newView(viewType);
        }

        @Override
        public void updateView(int position, int viewType, Message data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            if (viewType == TYPE_COME) {
                TextView content = (TextView) findViewById(R.id.app_chat_come_content);
                content.setText(data.getContent());
            } else if (viewType == TYPE_SEND) {
                TextView content = (TextView) findViewById(R.id.app_chat_send_content);
                content.setText(data.getContent());
            }
        }
    }
}
