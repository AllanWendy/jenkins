package cn.wecook.app.main.home.user;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.utils.BitmapUtils;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.datetimepicker.date.DatePickerDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.wecook.app.R;
import cn.wecook.app.features.picture.PictureActivity;

/**
 * 用户信息界面
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public class UserProfileFragment extends BaseTitleFragment {

    public static final String INTENT_UPD_USERINFO = "cn.wecook.app.intent_upd_user_info";
    private static final int REQUEST_CODE_PICK_PHOTO = 0x1000;
    private static final int MB = 1024 * 1024;
    private static final int GROUP_UPLOAD_AVATAR = 0;
    public static final String PICK_AVATAR_FILE_NAME = "photo.jpg";
    private boolean isInEditor;
    private TitleBar.ActionTextView mActionView;
    private ImageView mAvatar;
    private TextView mNickName;
    private EditText mNickNameEdit;
    private TextView mGenderGirl;
    private TextView mGenderBoy;
    private TextView mBirthday;
    private TextView mCity;

    private File mPhotoOutfile;
    private Bitmap mPhotoBitmap;

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_PHOTO) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
                @Override
                public void postUi() {
                    if (mPhotoBitmap != null) {
                        mAvatar.setImageBitmap(mPhotoBitmap);
                    }
                }

                @Override
                public void run() {

                    if (data != null) {
                        Uri picUri = data.getData();
                        try {
                            InputStream stream = getContext().getContentResolver().openInputStream(picUri);
                            mPhotoOutfile = FileUtils.newFileInStream(stream, PICK_AVATAR_FILE_NAME);
                            stream.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (mPhotoOutfile != null && mPhotoOutfile.exists()) {
                        BitmapUtils.BitmapInfo info = BitmapUtils.getBitmapBestFit(mPhotoOutfile.getPath(), 500, 500, 2d * MB);
                        if (info != null) {
                            mPhotoBitmap = info.src;
                        }
                    }
                }
            });

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, null);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        switch (v.getId()) {
            case R.id.app_user_profile_avatar:
                menu.add(GROUP_UPLOAD_AVATAR, Menu.FIRST + 1, 1, "图片");
                menu.add(GROUP_UPLOAD_AVATAR, Menu.FIRST + 2, 2, "拍照");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getGroupId()) {
            case GROUP_UPLOAD_AVATAR:
                performGroupUploadAvatar(item);
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void performGroupUploadAvatar(MenuItem item) {
        if (mPhotoBitmap != null && !mPhotoBitmap.isRecycled()) {
            mPhotoBitmap.recycle();
            mPhotoBitmap = null;
        }
        mPhotoOutfile = FileUtils.newFileInTemplate(PICK_AVATAR_FILE_NAME);
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                //选择图片
                Intent select = new Intent(Intent.ACTION_PICK);
                select.setType("image/*");
                startActivityForResult(select, REQUEST_CODE_PICK_PHOTO);
                break;
            case Menu.FIRST + 2:
                //选择拍照
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoOutfile));
                startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
                break;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TitleBar titleBar = getTitleBar();
        mActionView = new TitleBar.ActionTextView(getContext(),
                getContext().getString(R.string.app_title_bar_edit));
        mActionView.setTextColor(getResources().getColor(R.color.uikit_bt_orange_white_font));
        mActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInEditor) {
                    requestUpdateProfile();
                } else {
                    changeUserProfileState(true);
                }
            }
        });
        titleBar.addActionView(mActionView);
        titleBar.setTitle(getContext().getString(R.string.app_title_user_profile));
        titleBar.setBackgroundColor(getContext().getResources().getColor(R.color.uikit_white));

        mAvatar = (ImageView) view.findViewById(R.id.app_user_profile_avatar);
        mNickName = (TextView) view.findViewById(R.id.app_user_profile_nickname);
        mNickNameEdit = (EditText) view.findViewById(R.id.app_user_profile_nickname_edit);
        mGenderBoy = (TextView) view.findViewById(R.id.app_user_profile_boy);
        mGenderGirl = (TextView) view.findViewById(R.id.app_user_profile_girl);
        mBirthday = (TextView) view.findViewById(R.id.app_user_profile_birthday);
        mCity = (TextView) view.findViewById(R.id.app_user_profile_city);

        ImageFetcher.asInstance().load(UserProperties.getUserAvatar(), mAvatar, R.drawable.app_pic_default_avatar);
        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInEditor) {
                    v.performLongClick();
                } else {
                    //显示头像
                    Intent intent = new Intent(getContext(), PictureActivity.class);
                    intent.putExtra(PictureActivity.EXTRA_URL, UserProperties.getUserAvatar());
                    getActivity().startActivity(intent);
                }
            }
        });

        mNickName.setText(UserProperties.getUserName());

        mGenderBoy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInEditor) {
                    if (mGenderGirl.isSelected()) {
                        mGenderGirl.setSelected(false);
                    }
                    mGenderBoy.setSelected(!mGenderBoy.isSelected());
                }
            }
        });

        mGenderGirl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInEditor) {
                    if (mGenderBoy.isSelected()) {
                        mGenderBoy.setSelected(false);
                    }
                    mGenderGirl.setSelected(!mGenderGirl.isSelected());
                }
            }
        });

        mBirthday.setText(UserProperties.getUserBirthday());
        mBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //选择日期
                if (isInEditor) {
                    String birthday = mBirthday.getText().toString();
                    DateFormat d = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar calendar = Calendar.getInstance();
                    try {
                        Date date = d.parse(birthday);
                        calendar.setTime(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                            mBirthday.setText(year + "-" + (month + 1) + "-" + day);
                        }
                    }, year, month, day);
                    datePickerDialog.show(getFragmentManager(), "UserProfile");
                }
            }
        });

        mCity.setText(UserProperties.getUserCity());
        changeUserProfileState(false);
    }

    /**
     * 请求网络更新
     */
    private void requestUpdateProfile() {
        if (checkProfileChanged()) {
            if (mPhotoBitmap != null) {
                syncRemoteProfileAndAvatar();
            } else {
                syncRemoteProfile();
            }

        } else {
            changeUserProfileState(false);
        }
    }

    private void syncRemoteProfileAndAvatar() {
        String uid = UserProperties.getUserId();
        String nickname = mNickNameEdit.getText().toString();
        String gender = "";
        if (mGenderBoy.isSelected()) {
            gender = UserProperties.USER_GENDER_BOY_CODE;
        } else if (mGenderGirl.isSelected()) {
            gender = UserProperties.USER_GENDER_GIRL_CODE;
        } else {
            gender = UserProperties.USER_GENDER_X_CODE;
        }
        String birthday = mBirthday.getText().toString();

        String city = mCity.getText().toString();
        final User user = new User();
        user.setUid(uid);
        user.setGender(gender);
        user.setNickname(nickname);
        user.setBirthday(birthday);
        user.setCity(city);

        UserApi.updateInfo(uid, nickname, gender, birthday, city, new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (result != null) {
                    if (result.available()) {
                        UserProperties.save(user);
                        syncAvatar();
                        ToastAlarm.show(R.string.app_tip_update_success);
                        sendBroadcast();
                    } else {
                        ToastAlarm.show(result.getErrorMsg());
                    }
                } else {
                    ToastAlarm.show(R.string.app_error_no_reason);
                }
            }
        });
    }

    /**
     * 发送更新成功广播
     */
    private void sendBroadcast() {
        if (null != getContext()) {
            Intent intent = new Intent();
            intent.setAction(INTENT_UPD_USERINFO);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        }
    }

    private void syncAvatar() {
        UserApi.uploadAvatar(UserProperties.getUserId(), BitmapUtils.bitmap2Bytes(mPhotoBitmap, 100),
                new ApiCallback<User>() {
                    @Override
                    public void onResult(User result) {
                        if (result != null && result.available()) {
                            UserProperties.saveAvatar(result.getAvatar());
                            sendBroadcast();
                        }
                        changeUserProfileState(false);
                    }
                }
        );
    }

    private void syncRemoteProfile() {
        String uid = UserProperties.getUserId();
        final String nickname = mNickNameEdit.getText().toString();
        String gender = "";
        if (mGenderBoy.isSelected()) {
            gender = UserProperties.USER_GENDER_BOY_CODE;
        } else if (mGenderGirl.isSelected()) {
            gender = UserProperties.USER_GENDER_GIRL_CODE;
        } else {
            gender = UserProperties.USER_GENDER_X_CODE;
        }
        String birthday = mBirthday.getText().toString();

        String city = mCity.getText().toString();
        final User user = new User();
        user.setUid(uid);
        user.setGender(gender);
        user.setNickname(nickname);
        user.setBirthday(birthday);
        user.setCity(city);

        UserApi.updateInfo(uid, nickname, gender, birthday, city, new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (result != null) {
                    if (result.available()) {
                        UserProperties.save(user);
                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mNickName.setText(nickname);
                            }
                        });
                        ToastAlarm.show(R.string.app_tip_update_success);
                        sendBroadcast();
                    } else {
                        ToastAlarm.show(result.getErrorMsg());
                    }
                } else {
                    ToastAlarm.show(R.string.app_error_no_reason);
                }

                changeUserProfileState(false);
            }
        });
    }

    private boolean checkProfileChanged() {
        if (!NetworkState.available()) {
            ToastAlarm.show(R.string.app_error_network);
            return false;
        }

        String nickname = mNickNameEdit.getText().toString();
        if (!nickname.equals(UserProperties.getUserName())) {
            return true;
        }

        String gender = "";
        if (mGenderBoy.isSelected()) {
            gender = UserProperties.USER_GENDER_BOY;
        } else if (mGenderGirl.isSelected()) {
            gender = UserProperties.USER_GENDER_GIRL;
        } else {
            gender = UserProperties.USER_GENDER_X;
        }

        if (!gender.equals(UserProperties.getUserGender())) {
            return true;
        }

        String saveBirthday = UserProperties.getUserBirthday();
        String birthday = mBirthday.getText().toString();
        if (!birthday.equals(saveBirthday)) {
            return true;
        }

        if (mPhotoBitmap != null) {
            return true;
        }

        String oldCity = UserProperties.getUserCity();
        if (!oldCity.equals(mCity.getText().toString())) {
            return true;
        }

        return false;
    }

    /**
     * 改变视图状态
     *
     * @param edit
     */
    private void changeUserProfileState(boolean edit) {
        if (getActivity() == null) {
            return;
        }

        isInEditor = edit;
        if (isInEditor) {
            mActionView.setText(getString(R.string.app_title_bar_save));
            mNickName.setVisibility(View.GONE);
            mNickNameEdit.setVisibility(View.VISIBLE);
            mNickNameEdit.setText(mNickName.getText());
            mNickNameEdit.setSelection(mNickName.getText().length());

            mGenderBoy.setVisibility(View.VISIBLE);
            mGenderGirl.setVisibility(View.VISIBLE);

            mGenderBoy.setText(UserProperties.USER_GENDER_BOY);
            mGenderGirl.setText(UserProperties.USER_GENDER_GIRL);

            if (UserProperties.getUserGender().equals(UserProperties.USER_GENDER_BOY)) {
                mGenderBoy.setSelected(true);
                mGenderGirl.setSelected(false);
            } else if (UserProperties.getUserGender().equals(UserProperties.USER_GENDER_GIRL)) {
                mGenderBoy.setSelected(true);
                mGenderGirl.setSelected(false);
            } else {
                mGenderBoy.setSelected(false);
                mGenderGirl.setSelected(false);
            }

            mAvatar.setOnCreateContextMenuListener(this);

            if (StringUtils.isEmpty(UserProperties.getUserCity())) {
                mCity.setHint(R.string.app_tip_add_city);
                mCity.setText("");
            } else {
                mCity.setText(UserProperties.getUserCity());
            }
            mCity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //选择地区
                    mCity.setText("定位中...");
                    LocationServer.asInstance().location(new LocationServer.OnLocationResultListener() {
                        @Override
                        public void onLocationResult(boolean success, BDLocation location) {
                            if (success) {
                                mCity.setText(LocationServer.asInstance().getLocationAddress());
                            } else {
                                mCity.setText("");
                            }
                        }
                    });
                }
            });
        } else {
            mActionView.setText(getString(R.string.app_title_bar_edit));
            mNickName.setVisibility(View.VISIBLE);
            mNickNameEdit.setVisibility(View.GONE);

            if (UserProperties.getUserGender().equals(UserProperties.USER_GENDER_BOY)) {
                mGenderBoy.setText(UserProperties.getUserGender());
                mGenderBoy.setVisibility(View.VISIBLE);
                mGenderGirl.setVisibility(View.GONE);
                mGenderBoy.setSelected(true);
                mGenderGirl.setSelected(false);
            } else if (UserProperties.getUserGender().equals(UserProperties.USER_GENDER_GIRL)) {
                mGenderGirl.setText(UserProperties.getUserGender());
                mGenderBoy.setVisibility(View.GONE);
                mGenderGirl.setVisibility(View.VISIBLE);
                mGenderBoy.setSelected(false);
                mGenderGirl.setSelected(true);
            } else {
                mGenderGirl.setText(UserProperties.getUserGender());
                mGenderGirl.setVisibility(View.VISIBLE);
                mGenderBoy.setVisibility(View.GONE);
                mGenderBoy.setSelected(false);
                mGenderGirl.setSelected(false);
            }
            mAvatar.setOnCreateContextMenuListener(null);

            if (StringUtils.isEmpty(UserProperties.getUserCity())) {
                mCity.setText("");
                mCity.setHint("");
            } else {
                mCity.setText(UserProperties.getUserCity());
            }
            mCity.setOnClickListener(null);
        }
    }

}
