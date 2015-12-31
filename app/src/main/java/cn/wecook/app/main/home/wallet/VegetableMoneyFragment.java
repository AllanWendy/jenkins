package cn.wecook.app.main.home.wallet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;

/**
 * 菜金
 * Created by LK on 2015/10/15.
 */
public class VegetableMoneyFragment extends BaseTitleFragment {
    public static final String PARAMTS_MONEY = "money";
    private String money = "0.00";

    private View mView;
    private TextView mTextViewMoney;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments().containsKey(PARAMTS_MONEY)) {
            money = getArguments().getString(PARAMTS_MONEY);
        }

        mView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_vegetable_money, null);
        mTextViewMoney = (TextView) mView.findViewById(R.id.app_vegetable_money_count);


        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //设置TitleBar
        TitleBar titleBar = getTitleBar();
        titleBar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        titleBar.setBackgroundColor(getResources().getColor(R.color.uikit_white));
        titleBar.getTitleView().setText("菜金");

        //设置金额
        mTextViewMoney.setText(StringUtils.getPriceText(Float.parseFloat(money)));
        //收支明细
        mView.findViewById(R.id.app_vegetable_money_payment_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next(PaymentHistoryFragment.class);
            }
        });
    }
}
