package com.wecook.uikit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.wecook.uikit.R;

import java.util.List;

import static com.wecook.uikit.adapter.ViewHolder.ViewObject;

/**
 * 使用ViewHolder完成的Adapter
 * demo:
 * <br/>
 * <code>
 *     public class DemoAdapter extends
 *     BaseViewHolderAdapter<DemoAdapter.DemoData,DemoAdapter.DemoViewHolder> {
 *
 *         public class DemoData {
 *
 *         }
 *
 *         public class DemoViewHolder extends ViewHolder<DemoData> {
 *
 *         }
 *     }
 *
 * </code>
 *
 * @author kevin
 * @since 2014.1.6
 */
public abstract class BaseUIAdapter<T, V extends ViewHolder<T>>
		extends BaseAdapter implements IViewHolderAdapter<T, V>{

	private Context mContext;
	private int mItemLayoutResId;
	protected ViewHolderController<T> mViewHolderController;

	/**
	 * @param context
	 * @param itemLayoutResId
	 * @param data
	 */
	public BaseUIAdapter(Context context, int itemLayoutResId, List<T> data) {
		mContext = context;
		mItemLayoutResId = itemLayoutResId;
		mViewHolderController = new ViewHolderController<T>(data);
		registerDataSetObserver(mViewHolderController.getDataSetObserver());
	}

	/**
	 * @param context
	 * @param data
	 */
	public BaseUIAdapter(Context context, List<T> data) {
		mContext = context;
		mViewHolderController = new ViewHolderController<T>(data);
		registerDataSetObserver(mViewHolderController.getDataSetObserver());
	}

	@Override
	public int getCount() {
		return mViewHolderController.getCount();
	}

	@Override
	public T getItem(int position) {
		return (position < mViewHolderController.getCount()) ? mViewHolderController.getItem(position):null;
	}

	public ViewObject<T> getViewObject(int position) {
		return mViewHolderController.getViewObject(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    /**
     * 根据类型创建视图
     * @param viewType
     * @return
     */
	protected View newView(int viewType) {
		if (viewType == 0) {
			return LayoutInflater.from(mContext).inflate(mItemLayoutResId, null);
		}
		return null;
	}

    /**
     * 根据类型创建视图
     * @param viewType
     * @return
     */
	protected View newView(int viewType, int position) {
        return newView(viewType);
    }

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		try {
			V holder;
			int type = getItemViewType(position);
			if (convertView == null) {
                convertView = newView(type, position);
                holder = newViewHolder(position);
                holder.mViewType = type;
                convertView.setTag(R.id.view_holder, holder);
                holder.create(position, type, convertView, parent);
			} else {
                holder = (V) convertView.getTag(R.id.view_holder);
                //如果复用的view是其他类型的，则重新创建
                if (holder.mViewType != type) {
                    convertView = newView(type, position);
                    holder = newViewHolder(position);
                    holder.mViewType = type;
                    convertView.setTag(R.id.view_holder, holder);
                    holder.create(position, type, convertView, parent);
                }
            }

            holder.updateInternal(position, type, convertView);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return convertView;
	}

	public Context getContext() {
		return mContext;
	}

    public void release() {
        unregisterDataSetObserver(mViewHolderController.getDataSetObserver());
        mViewHolderController.release();
    }

    public void clear() {
        mViewHolderController.release();
        notifyDataSetChanged();
    }

	/**
	 * 移除
	 *
	 * @param entrys
	 */
	public void removeEntrys(List<T> entrys) {
		if(entrys == null){
			return;
		}
		
		if (mViewHolderController.removeEntrys(entrys)) {
			notifyDataSetChanged();
		}
	}

	/**
	 * 添加多项
	 *
	 * @param entrys
	 */
	public void addEntrys(List<T> entrys) {
		if(entrys == null){
			return;
		}
		
		if (mViewHolderController.addEntrys(entrys)) {
			notifyDataSetChanged();
		}

	}

	/**
	 * 移除一项
	 *
	 * @param entry
	 */
	public void removeEntry(T entry) {
		if(entry == null){
			return;
		}
		
		if (mViewHolderController.removeEntry(entry)) {
			notifyDataSetChanged();
		}
	}

	/**
	 * 添加一项
	 *
	 * @param entry
	 */
	public void addEntry(T entry) {
		if(entry == null){
			return;
		}
		
		if (mViewHolderController.addEntry(entry)) {
			notifyDataSetChanged();
		}
	}

	/**
	 * 添加一项
	 *
	 * @param entry
	 */
	public void addEntry(int index, T entry) {
		if(entry == null){
			return;
		}

		if (mViewHolderController.addEntry(index, entry)) {
			notifyDataSetChanged();
		}
	}

}
