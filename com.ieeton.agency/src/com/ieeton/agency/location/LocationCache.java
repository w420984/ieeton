package com.ieeton.agency.location;

import java.util.Hashtable;

public class LocationCache {
	private static LocationCache instance = null;
	private Hashtable<String, IeetonLocation> hashTableCache = new Hashtable<String, IeetonLocation>();
	private int intMaxCacheNum = LocationConstants.MAX_LOCATIONCACHE_NUM;
	private long longLastCacheTimestamp = 0l;

	private LocationCache() {
		//
	}

	/**
	 * 获取LocationCache实例
	 * 
	 * @param 无
	 * @return LocationCache
	 */
	public static synchronized LocationCache getInstance() {
		if (instance == null) {
			instance = new LocationCache();
		}
		return instance;
	}

	/**
	 * 将定位结果写入缓存
	 * 
	 * @param String
	 *            缓存的KEY，用于检索LOCATION
	 * @param IeetonLocation
	 *            缓存的LOCATION实例
	 * @return 无
	 */
	public synchronized void addCache(String stringKey, IeetonLocation location) {
		if (!isCacheItemFine(stringKey, location)) {
			return;
		}
		if (isCacheNeedReset()) {
			resetCache();
		}
		longLastCacheTimestamp = System.currentTimeMillis();
		hashTableCache.put(stringKey, location);
	}

	/**
	 * 从缓存中获取定位结果
	 * 
	 * @param String
	 *            缓存的KEY，用于检索LOCATION
	 * @return 无
	 */
	public IeetonLocation getCache(String stringKey) {
		IeetonLocation location = null;
		if (isCacheNeedReset()) {
			resetCache();
			location = null;
		} else {
			location = hashTableCache.get(stringKey);
		}
		return location;
	}

	/**
	 * 检查缓存数据是否需要被重置
	 * 
	 * @param 无
	 * @return boolean
	 */
	public boolean isCacheNeedReset() {
		return (hashTableCache.size() > this.intMaxCacheNum || (longLastCacheTimestamp > 0 && (System
				.currentTimeMillis() - longLastCacheTimestamp > LocationConstants.MAX_LOCATIONCACHE_TIME))) ? true
				: false;
	}

	/**
	 * 检查需要加入缓存的数据的有效性，只有有效数据才能加入缓存
	 * 
	 * @param String
	 *            缓存的KEY，用于检索LOCATION
	 * @param IeetonLocation
	 *            缓存的LOCATION实例
	 * @return boolean
	 */
	public boolean isCacheItemFine(String stringKey, IeetonLocation location) {
		return (stringKey != null && stringKey.indexOf("#") != -1
				&& location != null && location.isUseful()) ? true : false;
	}

	/**
	 * 重置缓存，清除缓存中的所有记录并将最后缓存时间置为0
	 * 
	 * @param 无
	 * @return 无
	 */
	public void resetCache() {
		longLastCacheTimestamp = 0l;
		hashTableCache.clear();
	}
}
