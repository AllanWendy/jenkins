package com.wecook.common.modules.cache;

import com.android.volley.toolbox.DiskBasedCache;
import com.wecook.common.modules.network.NetworkState;

import java.io.File;

/**
 * 无网络下自动续接缓存
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/7/14
 */
public class DiskCache extends DiskBasedCache {

    public DiskCache(File rootDirectory, int maxCacheSizeInBytes) {
        super(rootDirectory, maxCacheSizeInBytes);
    }

    public DiskCache(File rootDirectory) {
        super(rootDirectory);
    }

    @Override
    public synchronized Entry get(String key) {
        Entry entry = super.get(key);
        if (entry != null
                && !NetworkState.available()) {
            if (entry.isExpired()) {
                //续借1小时
                entry.ttl = System.currentTimeMillis() + 1 * 60 * 60 * 1000;
                entry.softTtl = System.currentTimeMillis() + 1 * 60 * 60 * 1000;
            }
        }
        return entry;
    }
}
