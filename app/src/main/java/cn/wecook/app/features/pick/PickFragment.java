package cn.wecook.app.features.pick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.filemaster.FileMaster;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.MediaStorePicker;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.Image;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.features.message.MessageEventReceiver;

/**
 * 获取相册图片
 *
 * @author kevin
 * @since 2014-12/23/14
 */
public class PickFragment extends BaseTitleFragment {

    public static final String EXTRA_FOOD = "extra_food";
    private static final int REQUEST_CAMERA = 0;
    private static final int MAX_COLUMN = 3;
    ArrayList<MediaStorePicker.MediaImage> selected = new ArrayList<MediaStorePicker.MediaImage>();
    private TitleBar mTitleBar;
    private ListView mListView;
    private ImageItemAdapter mImagesAdapter;
    private AlbumItemAdapter mAlbumAdapter;
    private List<ApiModelGroup<MediaStorePicker.MediaImage>> mImages = new ArrayList<ApiModelGroup<MediaStorePicker.MediaImage>>();
    private List<MediaStorePicker.MediaAlbum> mAlbums = new ArrayList<MediaStorePicker.MediaAlbum>();
    private Intent mActivityResultData;
    private Food mFoodOfCookShow;
    private MediaStorePicker.MediaImage mPhotoItem;
    private MediaStorePicker.MediaImage mAlbumItem;
    private MediaStorePicker.MediaAlbum mDefaultAlbum;
    private MediaStorePicker.MediaAlbum mCurrentAlbum;
    private MediaStorePicker.MediaImage mCurrentFocus;
    private int mPickType = PickActivity.PICK_ONE;
    private TitleBar.ActionTextView mPickCount;
    private TitleBar.ActionTextView mMultiFinish;
    private int mSelectedCountMax;
    private ArrayList<String> mInitSelectedList;//初始化已选中的path

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mFoodOfCookShow = (Food) bundle.getSerializable(EXTRA_FOOD);
            mPickType = bundle.getInt(PickActivity.EXTRA_PICK_TYPE, PickActivity.PICK_ONE);
            mSelectedCountMax = bundle.getInt(PickActivity.EXTRA_SELECTED_COUNT_MAX, Integer.MAX_VALUE);
            mInitSelectedList = (ArrayList) bundle.getSerializable(PickActivity.EXTRA_SELECTED);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                mActivityResultData = data;
                onStartUILoad();

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cook_show_pick, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTitleBar = getTitleBar();
        mTitleBar.enableBack(false);
        TextView backTitleView = mTitleBar.getBackTitleView();
        backTitleView.setText(R.string.app_button_title_cancel);
        backTitleView.setTextColor(getResources().getColor(R.color.uikit_white));
        backTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        mTitleBar.setBackDrawable(R.drawable.uikit_bt_back);
        mTitleBar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAlbum(-1);
            }
        });

        TextView titleView = mTitleBar.getTitleView();
        titleView.setText(R.string.app_title_select_image);
        titleView.setTextColor(getResources().getColor(R.color.uikit_font_white));

        mTitleBar.setBackgroundColor(getResources().getColor(R.color.uikit_dark));
        mTitleBar.enableBottomDiv(false);

        if (mPickType == PickActivity.PICK_MULTI) {

            mPickCount = new TitleBar.ActionTextView(getContext(), "");
            mPickCount.setTextColor(getResources().getColor(R.color.uikit_font_white));
            mPickCount.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            ScreenUtils.rePadding(mPickCount, 1);
            mPickCount.setBackgroundResource(R.drawable.app_bg_bubble);
            mPickCount.setMinWidth(ScreenUtils.dip2px(getContext(), 18));
            mTitleBar.addActionView(mPickCount, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ScreenUtils.dip2px(getContext(), 18)));

            mMultiFinish = new TitleBar.ActionTextView(getContext(), getString(R.string.app_button_title_finish));
            ScreenUtils.rePadding(mMultiFinish, 10, 0, 0, 0);
            mMultiFinish.setTextColor(getResources().getColor(R.color.uikit_font_white));
            mMultiFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //pick multi

                    if (!selected.isEmpty()) {
                        Intent data = new Intent(MessageEventReceiver.ACTION_PICK_MULTI_PIC);
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList(MessageEventReceiver.PARAM_LIST_BITMAP, selected);
                        data.putExtras(bundle);
                        getContext().sendBroadcast(data, MessageEventReceiver.PERMISSION_EVENT);
                        finishAll();
                    }
                }
            });
            mTitleBar.addActionView(mMultiFinish);

            mPickCount.setVisibility(View.GONE);
            mMultiFinish.setVisibility(View.GONE);
        }

        mListView = (ListView) view.findViewById(R.id.app_pick_images_list);

        mImagesAdapter = new ImageItemAdapter(this, mImages);
        mAlbumAdapter = new AlbumItemAdapter(this, mAlbums);

        mListView.setAdapter(mImagesAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectAlbum(position);
            }
        });

    }


    /**
     * 选择相册
     *
     * @param position
     */
    private void selectAlbum(int position) {
        MediaStorePicker.MediaAlbum album = ListUtils.getItem(mAlbums, position);
        selectAlbum(album);
    }

    /**
     * 选择相册
     */
    private void selectAlbum(MediaStorePicker.MediaAlbum album) {
        if (album != null) {
            fillImageData(album);
        }

        mListView.setAdapter(mImagesAdapter);
        mImagesAdapter.notifyDataSetChanged();

        mCurrentFocus = null;
        mTitleBar.enableBack(false);
        mTitleBar.getBackTitleView().setVisibility(View.VISIBLE);
        mTitleBar.setTitle(getString(R.string.app_title_select_image));
    }

    private void fillImageData(MediaStorePicker.MediaAlbum album) {
        mImages.clear();
        if (mPhotoItem == null) {
            mPhotoItem = new MediaStorePicker.MediaImage();
            mPhotoItem.path = "1";
        }
        if (mAlbumItem == null) {
            mAlbumItem = new MediaStorePicker.MediaImage();
            mAlbumItem.path = "2";
        }
        if (!album.images.contains(mPhotoItem)) {
            album.images.add(0, mPhotoItem);
        }
        if (!album.images.contains(mAlbumItem)) {
            album.images.add(1, mAlbumItem);
        }
        for (MediaStorePicker.MediaImage image : album.images) {
            if (selected.contains(image)) {
                image.selected = 1;
            }
        }
        updateMultiChoiceTitle();
        ApiModelGroup<MediaStorePicker.MediaImage> group = new ApiModelGroup<MediaStorePicker.MediaImage>(MAX_COLUMN);
        List<ApiModelGroup<MediaStorePicker.MediaImage>> list = group.loadChildrenFromList(album.images);
        mImages.addAll(list);

        mCurrentAlbum = album;
    }

    private void fillImageData(List<MediaStorePicker.MediaImage> all) {
        mImages.clear();

        if (mPhotoItem == null) {
            mPhotoItem = new MediaStorePicker.MediaImage();
            mPhotoItem.path = "1";
        }
        if (mAlbumItem == null) {
            mAlbumItem = new MediaStorePicker.MediaImage();
            mAlbumItem.path = "2";
        }
        if (!all.contains(mPhotoItem)) {
            all.add(0, mPhotoItem);
        }
        if (!all.contains(mAlbumItem)) {
            all.add(1, mAlbumItem);
        }
        //标记已选中的item
        if (mInitSelectedList != null) {
            HashMap<Integer, MediaStorePicker.MediaImage> tempHashmap = new HashMap<>();
            for (MediaStorePicker.MediaImage album : all) {
                if (mInitSelectedList.contains(album.path)) {
                    int index = mInitSelectedList.indexOf(album.path);
                    album.selected = 1;
                    tempHashmap.put(index, album);
                }
            }
            for (int i = 0; i < tempHashmap.size(); i++) {
                if (tempHashmap.containsKey(i)) {
                    selected.add(tempHashmap.get(i));
                }
            }
            mInitSelectedList = null;
        }
        for (MediaStorePicker.MediaImage album : all) {
            if (selected.contains(album)) {
                album.selected = 1;
            }
        }
        updateMultiChoiceTitle();
        ApiModelGroup<MediaStorePicker.MediaImage> group = new ApiModelGroup<MediaStorePicker.MediaImage>(MAX_COLUMN);
        List<ApiModelGroup<MediaStorePicker.MediaImage>> list = group.loadChildrenFromList(all);
        mImages.addAll(list);

        mCurrentAlbum = new MediaStorePicker.MediaAlbum();
        mCurrentAlbum.images.addAll(all);
        mCurrentAlbum.images.remove(mPhotoItem);
        mCurrentAlbum.images.remove(mAlbumItem);
    }

    /**
     * 点击图片更新当前位置
     */
    private void focusCurrentImage(MediaStorePicker.MediaImage image) {
        if (image == mPhotoItem) {
            MobclickAgent.onEvent(getContext(), LogConstant.COOK_ACTION_PHOTO);
            Intent intent = new Intent(getContext(), CameraActivity.class);
            intent.putExtra(CameraActivity.EXTRA_ALBUM, mDefaultAlbum);
            startActivityForResult(intent, REQUEST_CAMERA);
            return;
        } else if (image == mAlbumItem) {
            //选择相册
            mTitleBar.enableBack(true);
            mTitleBar.getBackTitleView().setVisibility(View.GONE);
            mTitleBar.setTitle(getString(R.string.app_title_select_album));

            mListView.setAdapter(mAlbumAdapter);
            mAlbumAdapter.notifyDataSetChanged();
            return;
        }

        if (mCurrentFocus != image) {
            MobclickAgent.onEvent(getContext(), LogConstant.COOK_ACTION_PICK);
            mCurrentFocus = image;

            Bundle bundle = new Bundle();
            bundle.putInt(PickActivity.EXTRA_PICK_TYPE, mPickType);
            bundle.putParcelable(PickPreviewFragment.EXTRA_CURRENT_IMAGE, image);

            if (mPickType == PickActivity.PICK_MULTI) {
                bundle.putParcelableArrayList(PickPreviewFragment.EXTRA_IMAGE_LIST, mCurrentAlbum.images);
            } else if (mPickType == PickActivity.PICK_ONE ||
                    mPickType == PickActivity.PICK_COOK_SHOW) {
                if (mFoodOfCookShow != null) {
                    bundle.putSerializable(PickPreviewFragment.EXTRA_FOOD, mFoodOfCookShow);
                }
            }

            next(PickPreviewFragment.class, bundle);
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        MediaStorePicker.pick(getContext(), new MediaStorePicker.PickHandler() {
            @Override
            public void picked(final List<MediaStorePicker.MediaImage> all, final List<MediaStorePicker.MediaAlbum> albums) {
                //处理数据
                Collections.sort(albums, new Comparator<MediaStorePicker.MediaAlbum>() {
                    @Override
                    public int compare(MediaStorePicker.MediaAlbum lhs, MediaStorePicker.MediaAlbum rhs) {
                        return rhs.images.size() - lhs.images.size();
                    }
                });

                for (MediaStorePicker.MediaAlbum album : albums) {
                    if (FileMaster.getInstance().getLongImageDir().getName().equals(album.albumName)) {
                        mDefaultAlbum = album;
                        break;
                    }
                }


                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fillImageData(all);
                        mAlbums.clear();
                        mAlbums.addAll(albums);
                        mImagesAdapter.notifyDataSetChanged();
                        mAlbumAdapter.notifyDataSetChanged();

                        processActivityResult();
                    }
                });
            }
        });

    }

    /**
     * 处理ActivityResult返回的数据
     */
    private void processActivityResult() {
        if (mActivityResultData != null) {
            String selectedAlbum = mActivityResultData.getStringExtra(CameraActivity.EXTRA_ALBUM);

            if (!StringUtils.isEmpty(selectedAlbum)) {
                selectAlbum(mDefaultAlbum);
            }

            mActivityResultData = null;
        }
    }

    private void updateMultiChoiceTitle() {

        if (mMultiFinish != null && mPickCount != null) {
            if (selected.size() > 0) {
                mMultiFinish.setVisibility(View.VISIBLE);
                mPickCount.setText(selected.size() + "");
                mPickCount.setVisibility(View.VISIBLE);
            } else {
                mMultiFinish.setVisibility(View.GONE);
                mPickCount.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        mCurrentFocus = null;

        if (mPickType == PickActivity.PICK_MULTI && mCurrentAlbum != null) {
            updateMultiChoiceTitle();

            if (mImagesAdapter != null) {
                mImagesAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 相册适配器
     */
    private static class AlbumItemAdapter extends UIAdapter<MediaStorePicker.MediaAlbum> {
        private PickFragment mFragment;

        public AlbumItemAdapter(PickFragment fragment, List<MediaStorePicker.MediaAlbum> data) {
            super(fragment.getContext(), R.layout.listview_item_album, data);
            mFragment = fragment;
        }


        @Override
        public void updateView(int position, int viewType, MediaStorePicker.MediaAlbum data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            ImageView album = (ImageView) findViewById(R.id.app_pick_album);
            TextView name = (TextView) findViewById(R.id.app_pick_album_name);
            TextView count = (TextView) findViewById(R.id.app_pick_album_count);

            MediaStorePicker.MediaImage first = null;
            for (int index = 0; index < data.images.size(); index++) {
                first = ListUtils.getItem(data.images, index);
                if (first != mFragment.mAlbumItem
                        && first != mFragment.mPhotoItem
                        && first != null) {
                    break;
                }
            }

            if (first != null) {
                ImageFetcher.asInstance().load(first.path, album);
            }

            name.setText(data.albumName);
            count.setText(data.images.size() + "");
        }
    }

    /**
     * 图片适配器
     */
    private static class ImageItemAdapter extends UIAdapter<ApiModelGroup<MediaStorePicker.MediaImage>> {

        private PickFragment mFragment;
        private int width;
        private float space;

        public ImageItemAdapter(PickFragment fragment, List<ApiModelGroup<MediaStorePicker.MediaImage>> data) {
            super(fragment.getContext(), data);
            mFragment = fragment;

        }

        @Override
        protected View newView(int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_album_image, null);
            View itemView1 = view.findViewById(R.id.app_pick_image_1);
            View itemView2 = view.findViewById(R.id.app_pick_image_2);
            View itemView3 = view.findViewById(R.id.app_pick_image_3);
            space = getContext().getResources().getDimension(R.dimen.app_pick_grid_space);
            width = PhoneProperties.getScreenWidthInt() / 3;
            newItemView(itemView1);
            newItemView(itemView2);
            newItemView(itemView3);
            return view;
        }

        private void newItemView(View itemView) {
            ScreenUtils.resizeViewWithSpecial(itemView, width, width);
            ScreenUtils.rePadding(itemView, (int) space, (int) space, (int) space, (int) space, true);
        }

        @Override
        public void updateView(int position, int viewType, ApiModelGroup<MediaStorePicker.MediaImage> data, Bundle extra) {
            super.updateView(position, viewType, data, extra);

            View itemView1 = findViewById(R.id.app_pick_image_1);
            View itemView2 = findViewById(R.id.app_pick_image_2);
            View itemView3 = findViewById(R.id.app_pick_image_3);
            updateItem(itemView1, data.getItem(0));
            updateItem(itemView2, data.getItem(1));
            updateItem(itemView3, data.getItem(2));

        }

        private void updateItem(View view, final MediaStorePicker.MediaImage data) {

            if (data == null) {
                view.setVisibility(View.INVISIBLE);
                view.setEnabled(false);
                return;
            } else {
                view.setEnabled(true);
                view.setVisibility(View.VISIBLE);
            }

            final ImageView image = (ImageView) view.findViewById(R.id.app_image_view);
            final ImageView choice = (ImageView) view.findViewById(R.id.app_image_choice);

            if (data == mFragment.mPhotoItem) {
                //拍照
                image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                image.setImageResource(R.drawable.app_ic_pick_camera);
                choice.setVisibility(View.GONE);
            } else if (data == mFragment.mAlbumItem) {
                //选择相册
                image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                image.setImageResource(R.drawable.app_ic_pick_gallery);
                choice.setVisibility(View.GONE);
            } else {
                ImageFetcher.asInstance().load(data.path, image, width, width);
                if (mFragment.mPickType == PickActivity.PICK_MULTI) {
                    choice.setVisibility(View.VISIBLE);
                    choice.setSelected(data.isSelected());
                } else {
                    choice.setVisibility(View.GONE);
                }
            }
            if (mFragment.mPickType == PickActivity.PICK_MULTI) {
                if (data.selected == 1) {
                    choice.setSelected(true);
                } else {
                    choice.setSelected(false);
                }
                choice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (choice.getVisibility() != View.GONE) {
                            if (!data.isSelected()) {
                                if (mFragment.selected.size() >= mFragment.mSelectedCountMax) {
                                    ToastAlarm.show("最多选择" + mFragment.mSelectedCountMax + "张图片");
                                    return;
                                }
                            }
                            data.setSelected(!data.isSelected());
                            choice.setSelected(data.isSelected());
                            if (data.isSelected()) {
                                mFragment.selected.add(data);
                            } else {
                                mFragment.selected.remove(data);
                            }
                            mFragment.updateMultiChoiceTitle();
                        }
                    }
                });
            }

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFragment.focusCurrentImage(data);
                }
            });
        }

    }

}
