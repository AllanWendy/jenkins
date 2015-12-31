package cn.wecook.app.main.dish.shopcart;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.wecook.common.utils.ListUtils;
import com.wecook.sdk.api.model.DeliveryDate;
import com.wecook.sdk.api.model.DeliveryRestaurant;
import com.wecook.sdk.api.model.DeliveryTime;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.DialogAlarm;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;

/**
 * 配送时间列表选择
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/25
 */
public class DishOrderDeliveryTimeDialog extends DialogAlarm {

    private OnDeliveryTimeSelectListener mSelectListener;
    private OnDoneClickListener mDoneListener;
    private DeliveryDateAdapter mDateAdapter;
    private DeliveryTimeAdapter mTimeAdapter;
    private ListView mDeliveryDateList;
    private ListView mDeliveryTimeList;

    private List<DeliveryTime> mDeliveryTimes = new ArrayList<>();

    private List<DeliveryDate> mDeliveryDates = new ArrayList<>();
    private String mDeliveryType = "-1";//配送类型
    private DeliveryTime mDeliveryTime;

    public DishOrderDeliveryTimeDialog(Context context) {
        super(context, R.style.UIKit_Dialog_Fixed);
    }

    /**
     * 解决Vivo系统兼容性问题
     *
     * @param context
     * @param noAnim
     */
    public DishOrderDeliveryTimeDialog(Context context, boolean noAnim) {
        super(context, R.style.UIKit_Dialog_NO_ANIMATION);
    }

    @Override
    public View getView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.dialog_order_delivery_time_list, parent, true);
    }

    @Override
    public void onViewCreated(View view) {
        view.findViewById(R.id.app_dialog_order_delivery_time_finish)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mDeliveryTime &&
                                (mDeliveryType.equals(DeliveryRestaurant.EXPRESS_BY_WECOOK) || mDeliveryType.equals(DeliveryRestaurant.EXPRESS_BY_RESTAURANT))) {
                            if (null != mDoneListener) {
                                mDoneListener.onClick(mDeliveryTime, mDeliveryType);
                            }
                        } else {
                            if (null != mDoneListener) {
                                Exception exception = new IllegalArgumentException("未选择时间");
                                mDoneListener.onError(exception);
                            }
                        }
                        dismiss();
                    }
                });

        mDeliveryDateList = (ListView) view.findViewById(R.id.app_dialog_order_delivery_data_list);
        mDeliveryTimeList = (ListView) view.findViewById(R.id.app_dialog_order_delivery_time_list);
        mDateAdapter = new DeliveryDateAdapter(getContext(), mDeliveryDates);
        mTimeAdapter = new DeliveryTimeAdapter(getContext(), mDeliveryTimes);

        mDeliveryDateList.setAdapter(mDateAdapter);
        mDeliveryTimeList.setAdapter(mTimeAdapter);
        selectDeliveryDate(0);
    }

    /**
     * 选择配送日期
     *
     * @param position
     */
    private void selectDeliveryDate(int position) {
        DeliveryDate date = ListUtils.getItem(mDeliveryDates, position);
        if (date != null) {
            date.setSelected(true);
            for (DeliveryDate item : mDeliveryDates) {
                if (item != date) {
                    item.setSelected(false);
                }
            }

            mDeliveryTimes.clear();
            mDeliveryTimes.addAll(date.getDeliveryTimes());
            if (date.getDeliveryTimes().size() > 0) {
                DeliveryTime time = date.getDeliveryTimes().get(0);
                time.setSelected(true);
                mDeliveryTime = time;
                if (mSelectListener != null) {
                    mSelectListener.onSelect(time, mDeliveryType);
                }
            }
            if (mTimeAdapter != null) {
                mTimeAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 选择配送时间区间
     *
     * @param position
     */
    private void selectDeliveryTime(int position) {
        if (mDeliveryTimes != null) {
            DeliveryTime time = ListUtils.getItem(mDeliveryTimes, position);
            if (time != null) {
                time.setSelected(true);
                for (DeliveryTime item : mDeliveryTimes) {
                    if (item != time) {
                        item.setSelected(false);
                    }
                }

                for (DeliveryDate item : mDeliveryDates) {
                    if (!item.isSelected()) {
                        for (DeliveryTime otherTime : item.getDeliveryTimes()) {
                            otherTime.setSelected(false);
                        }
                    }
                }

                mDeliveryTime = time;
                if (mSelectListener != null) {
                    mSelectListener.onSelect(time, mDeliveryType);
                }
            }
        }
    }

    public DishOrderDeliveryTimeDialog setDeliveryTimes(List<DeliveryDate> deliveryDates) {
        mDeliveryDates.clear();
        mDeliveryDates.addAll(deliveryDates);
        return this;
    }

    public DishOrderDeliveryTimeDialog setOnItemClick(OnDeliveryTimeSelectListener clickListener) {
        mSelectListener = clickListener;
        return this;
    }

    public DishOrderDeliveryTimeDialog setOnDoneClick(OnDoneClickListener clickListener) {
        mDoneListener = clickListener;
        return this;
    }

    public interface OnDeliveryTimeSelectListener {
        void onSelect(DeliveryTime time, String deliveryType);
    }

    public interface OnDoneClickListener {
        void onClick(DeliveryTime time, String deliveryType);

        void onError(Exception e);
    }

    private class DeliveryDateAdapter extends UIAdapter<DeliveryDate> {

        public DeliveryDateAdapter(Context context, List<DeliveryDate> data) {
            super(context, R.layout.view_delivery_data, data);
        }

        @Override
        public void updateView(final int position, int viewType, final DeliveryDate data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            if (data != null) {
                TextView textView = (TextView) findViewById(R.id.app_delivery_date_name);
                textView.setText(data.getData());
                textView.setSelected(data.isSelected());
                getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectDeliveryDate(position);
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }

    private class DeliveryTimeAdapter extends UIAdapter<DeliveryTime> {

        public DeliveryTimeAdapter(Context context, List<DeliveryTime> data) {
            super(context, R.layout.view_delivery_time, data);
        }

        @Override
        public void updateView(final int position, int viewType, final DeliveryTime data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            if (data != null) {
                TextView textView = (TextView) findViewById(R.id.app_delivery_time_name);
                textView.setText(data.getTime());
                textView.setSelected(data.isSelected());
                getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectDeliveryTime(position);
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }

    public String getDeliveryType() {
        return mDeliveryType;
    }

    public DishOrderDeliveryTimeDialog setDeliveryType(String mDeliveryType) {
        this.mDeliveryType = mDeliveryType;
        return this;
    }
}
