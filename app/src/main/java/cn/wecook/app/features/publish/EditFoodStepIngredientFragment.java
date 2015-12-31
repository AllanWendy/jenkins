package cn.wecook.app.features.publish;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.sdk.api.model.FoodIngredient;
import com.wecook.sdk.policy.FoodDetailEditorPolicy;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.widget.dragsort.DragSortController;
import com.wecook.uikit.widget.dragsort.DragSortListView;
import com.wecook.uikit.window.PopListWindow;

import java.util.List;

import cn.wecook.app.R;

/**
 * 编辑食材
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/21/15
 */
public class EditFoodStepIngredientFragment extends BaseFragment {

    private static final String KEY_STORE_QUALITY_UNITS = "key_store_quality_units";
    private static final String KEY_STORE_FULL_QUALITY_UNITS = "key_store_full_quality_units";
    private DragSortListView mListView;
    private View mBottomAdd;
    private View mFooterAdd;

    private IngredientItemAdapter mAdapter;
    private DragController mDragController;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_food_step_ingredient, null);
        View footer = inflater.inflate(R.layout.listview_footer_step_add_ingredient, null);

        mFooterAdd = footer.findViewById(R.id.app_food_publish_add_ingredient);
        mBottomAdd = view.findViewById(R.id.app_food_publish_add_ingredient);

        View.OnClickListener add = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FoodDetailEditorPolicy.get().addIngredient(new FoodIngredient());
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                    mListView.setSelection(mAdapter.getCount() - 1);
                }
            }
        };

        mBottomAdd.setOnClickListener(add);
        mFooterAdd.setOnClickListener(add);

        mListView = (DragSortListView) view.findViewById(R.id.app_food_publish_step_ingredient_list);
        mListView.addFooterView(footer);

        mDragController = new DragController(mListView);
        mDragController.setRemoveEnabled(false);
        mDragController.setDragInitMode(DragSortController.ON_LONG_PRESS);
        mDragController.setDragHandleId(R.id.app_food_publish_list_holder);
        mDragController.setBackgroundColor(getResources().getColor(R.color.uikit_grey_light));
        mListView.setOnTouchListener(mDragController);
        mListView.setDropListener(mDragController);
        mListView.setFloatViewManager(mDragController);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (scrollState != SCROLL_STATE_IDLE) {
                    if (mAdapter != null) {
                        mAdapter.hideKeyboard();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {

                final boolean canScroll = ViewCompat.canScrollVertically(mListView, -1);
                Logger.debug("scroll", "onScroll ...", firstVisibleItem,
                        visibleItemCount, totalItemCount, canScroll);

                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (visibleItemCount == totalItemCount && !canScroll) {
                            mFooterAdd.setVisibility(View.VISIBLE);
                            mBottomAdd.setVisibility(View.GONE);
                        } else {
                            mFooterAdd.setVisibility(View.INVISIBLE);
                            mBottomAdd.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new IngredientItemAdapter(getContext(), FoodDetailEditorPolicy.get().getIngredientsList());
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAdapter != null &&
                mAdapter.popupWindow != null) {
            mAdapter.popupWindow.dismiss();
        }
    }

    private class DragController extends DragSortController implements DragSortListView.DropListener {
        private DragSortListView mDslv;

        public DragController(DragSortListView dslv) {
            super(dslv);
            mDslv = dslv;
        }

        @Override
        public View onCreateFloatView(int position) {
            View view = super.onCreateFloatView(position);
            if (view != null) {
                view.setBackgroundResource(R.drawable.app_bt_background_grep_side);
            }
            return view;
        }

        @Override
        public int startDragPosition(MotionEvent ev) {
            int res = super.dragHandleHitPosition(ev);

            int width = mDslv.getWidth();

            if ((int) ev.getX() > width * 2 / 3) {
                return res;
            } else {
                return DragSortController.MISS;
            }
        }

        @Override
        public int dragHandleHitPosition(MotionEvent ev) {
            return super.dragHandleHitPosition(ev);
        }


        @Override
        public void drop(int from, int to) {
            Logger.d("from : " + from + " to : " + to);
            FoodIngredient ingredient = FoodDetailEditorPolicy.get().getIngredientsList().remove(from);
            FoodDetailEditorPolicy.get().getIngredientsList().add(to, ingredient);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }

    }

    /**
     * 食材列表项UI适配
     */
    private class IngredientItemAdapter extends UIAdapter<FoodIngredient> implements TextWatcher {

        private FoodIngredient mCurrentIngredient;
        private int mInEditType;// 1:name 2:quality

        private PopListWindow popupWindow;
        private EditText mQualityView;
        private boolean mIsShowFullQuality = true;
        private boolean mHasItemClicked;
        private View mFocusView;

        public IngredientItemAdapter(Context context, List<FoodIngredient> data) {
            super(context, R.layout.listview_item_add_ingredient, data);
        }

        @Override
        public void updateView(final int position, int viewType, final FoodIngredient data, Bundle extra) {
            super.updateView(position, viewType, data, extra);

            final View delete = findViewById(R.id.app_food_publish_list_delete);
            final View holder = findViewById(R.id.app_food_publish_list_holder);
            final EditText name = (EditText) findViewById(R.id.app_food_publish_ingredient_name);
            final EditText quality = (EditText) findViewById(R.id.app_food_publish_ingredient_quality);

            delete.setFocusable(false);
            holder.setFocusable(false);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FoodDetailEditorPolicy.get().deleteIngredient(data);
                    notifyDataSetChanged();
                }
            });

            name.setText(data.getName());
            quality.setText(data.getQuality());

            name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        name.addTextChangedListener(IngredientItemAdapter.this);
                        mCurrentIngredient = data;
                        mInEditType = 1;
                        delete.setVisibility(View.INVISIBLE);
                        holder.setVisibility(View.INVISIBLE);
                        mFocusView = name;
                    } else {
                        name.removeTextChangedListener(IngredientItemAdapter.this);
                        mCurrentIngredient = null;
                        mInEditType = 0;
                        delete.setVisibility(View.VISIBLE);
                        holder.setVisibility(View.VISIBLE);
                    }
                }
            });

            quality.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        quality.addTextChangedListener(IngredientItemAdapter.this);
                        mCurrentIngredient = data;
                        mInEditType = 2;
                        delete.setVisibility(View.INVISIBLE);
                        holder.setVisibility(View.INVISIBLE);
                        mQualityView = quality;
                        mFocusView = quality;
                        mQualityView.setText(mQualityView.getText().toString());
                    } else {
                        quality.removeTextChangedListener(IngredientItemAdapter.this);
                        mCurrentIngredient = null;
                        mInEditType = 0;
                        delete.setVisibility(View.VISIBLE);
                        holder.setVisibility(View.VISIBLE);
                    }
                }
            });

        }

        private void showQualityTips(View view, boolean number) {
            popupWindow = new PopListWindow(getContext());
            popupWindow.setItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mHasItemClicked = true;
                    popupWindow.dismiss();
                    String itemName = popupWindow.getItemName(position);
                    if (mQualityView != null) {
                        mQualityView.setText(mQualityView.getText().toString() + itemName);
                        mQualityView.setSelection(mQualityView.length());
                    }
                }
            });
            String[] qualityUnits = null;
            if (number) {
                qualityUnits = getQualityUnits();
            } else {
                qualityUnits = getFullQualityUnits();
            }
            popupWindow.addItems(qualityUnits);
            popupWindow.showAtViewSide(view, 0, 0);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mCurrentIngredient != null) {
                if (mInEditType == 1) {
                    mCurrentIngredient.setName(s.toString());
                } else if (mInEditType == 2) {
                    mCurrentIngredient.setQuantity(s.toString());
                    if (s.length() == 0) {//空的情况显示模糊质量
                        showQualityTips(mQualityView, false);
                    } else {
                        boolean isNumber = checkIsNumberQuality();
                        if (isNumber) {
                            showQualityTips(mQualityView, true);
                        }
                    }
                }
            }
        }

        /**
         * 检查是否是数字质量
         *
         * @return
         */
        private boolean checkIsNumberQuality() {
            boolean isNumber = false;
            try {
                Float.parseFloat(mQualityView.getText().toString());
                isNumber = true;
            } catch (NumberFormatException e) {
            }
            return isNumber;
        }

        public void hideKeyboard() {
            if (mFocusView != null) {
                KeyboardUtils.closeKeyboard(getContext(), mFocusView);
            }
        }

        private String[] getFullQualityUnits() {
            String units = (String) SharePreferenceProperties.get(KEY_STORE_FULL_QUALITY_UNITS,
                    "少许;若干;适量;半");
            return units.split(";");
        }

        private String[] getQualityUnits() {
            String units = (String) SharePreferenceProperties.get(KEY_STORE_QUALITY_UNITS,
                    "克;千克;滴;毫升;升;个;勺;片;条;根;块");
            return units.split(";");
        }
    }
}
