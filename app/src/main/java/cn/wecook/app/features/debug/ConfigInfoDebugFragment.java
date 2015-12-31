package cn.wecook.app.features.debug;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wecook.common.modules.location.LocationServer;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.WecookConfig;

/**
 * 环境配置
 *
 * @author kevin
 * @version v1.0
 * @since 2015-3/18/15
 */
public class ConfigInfoDebugFragment extends BaseDebugFragment {

    private List<ConfigInfo> configInfos = new ArrayList<ConfigInfo>();

    private View mRootView;

    @Override
    public String getTitle() {
        return "环境配置";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configInfos.add(new ConfigInfo(new Get() {
            @Override
            public Object get() {
                return "测试环境";
            }
        }, new Get() {
            @Override
            public Object get() {
                return WecookConfig.getInstance().getService();
            }
        }, new Get() {
            @Override
            public Object get() {
                return WecookConfig.getInstance().isTest();
            }
        }, new Get() {

            @Override
            public Object get() {
                return LayoutInflater.from(getContext()).inflate(R.layout.view_toggle, null);
            }
        }, new Set() {
            @Override
            public void set(Object obj) {
                if (obj instanceof Boolean) {
                    WecookConfig.getInstance().toggleTest((Boolean) obj);
                }
            }
        }));

        configInfos.add(new ConfigInfo(new Get() {
            @Override
            public Object get() {
                return "买菜功能";
            }
        }, new Get() {
            @Override
            public Object get() {
                return "LAT : [" + LocationServer.asInstance().getLat()
                        + "] & LON : [" + LocationServer.asInstance().getLon() + "]";
            }
        }, new Get() {
            @Override
            public Object get() {
                return WecookConfig.getInstance().isOpenSell();
            }
        }, new Get() {

            @Override
            public Object get() {
                return LayoutInflater.from(getContext()).inflate(R.layout.view_toggle, null);
            }
        }, new Set() {

            @Override
            public void set(Object obj) {
                if (obj instanceof Boolean) {
                    WecookConfig.getInstance().toggleOpenSell((Boolean) obj);
                }
            }
        }));

        configInfos.add(new ConfigInfo(new Get() {
            @Override
            public Object get() {
                return "设置获得优惠的地址";
            }
        }, new Get() {
            @Override
            public Object get() {
                return WecookConfig.getInstance().getCouponUrlAddress();
            }
        }, new Get() {
            @Override
            public Object get() {
                return WecookConfig.getInstance().isTestCouponUrlAddress();
            }
        }, new Get() {

            @Override
            public Object get() {
                return LayoutInflater.from(getContext()).inflate(R.layout.view_toggle, null);
            }
        }, new Set() {

            @Override
            public void set(Object obj) {
                if (obj instanceof Boolean) {
                    WecookConfig.getInstance().toggleCouponUrlAddress((Boolean) obj);
                }
            }
        }));
    }

    @Override
    public View onCreateInnerView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mRootView = inflater.inflate(R.layout.fragment_debug_config_info, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (ConfigInfo info : configInfos) {
            View config = (View) info.view.get();
            info.layout = config;
            info.update();
            ((ViewGroup) mRootView).addView(config, new LinearLayout.LayoutParams(-1, -2));
        }

    }

    private interface Get {
        public Object get();
    }

    private interface Set {
        public void set(Object obj);
    }

    private class ConfigInfo {
        private Get label;
        private Get content;
        private Get toggle;
        private Get view;
        private Set toggleChange;
        private View layout;
        private TextView contentView;
        private ImageButton toggleView;

        private ConfigInfo(Get label, Get content, Get toggle, Get view, Set toggleChange) {
            this.label = label;
            this.content = content;
            this.toggle = toggle;
            this.view = view;
            this.toggleChange = toggleChange;
        }

        public void update() {
            TextView label = (TextView) layout.findViewById(R.id.app_debug_label);
            contentView = (TextView) layout.findViewById(R.id.app_debug_content);
            toggleView = (ImageButton) layout.findViewById(R.id.app_debug_toggle);
            label.setText((String) this.label.get());
            contentView.setText((String) this.content.get());
            boolean selected = (Boolean) this.toggle.get();
            toggleView.setSelected(selected);
            toggleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isSelected = toggleView.isSelected();
                    toggleChange.set(!isSelected);
                    toggleView.setSelected(!isSelected);
                    contentView.setText((String) ConfigInfo.this.content.get());
                }
            });
        }
    }

}
