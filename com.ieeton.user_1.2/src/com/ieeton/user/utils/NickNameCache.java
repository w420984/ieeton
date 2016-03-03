package com.ieeton.user.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;


/**
 * 昵称缓存，采用软引用和lru算法
 *
 */
public class NickNameCache {

	private NickNameCache() {
		
	}

	private ReferenceQueue<String> queue = new ReferenceQueue<String>();

	private long maxCacheSize = 5 * 1024 * 1024;//最大值

	private static NickNameCache mInstance = null;
	private ConcurrentLRUHashMap<String, CacheEntry> mNcikCacheMap = new ConcurrentLRUHashMap<String, NickNameCache.CacheEntry>();

	private boolean mActive = true;

	public static NickNameCache getInstance() {
		if (mInstance == null) {
		    synchronized(NickNameCache.class) {
		        if (mInstance == null) {
		            mInstance = new NickNameCache();
		        }
		    }
		}
		return mInstance;
	}

	/**
	 * 保存到cache
	 * @param passport
	 * @param bm
	 */
	public void save(String passport, String nick) {
		if (mActive == false) {
			return;
		}

		CacheEntry e;
		while ((e = (CacheEntry) queue.poll()) != null) {
			mNcikCacheMap.remove(e.passport);
		}

		if (nick == null || "".equals(nick) || passport == null
				|| "".equals(passport.trim())) {
			return;
		}
		e = new CacheEntry(nick, queue, passport);
		mNcikCacheMap.put(passport, e);
	}
	
	/**
	 * 从缓存中获取图片，缓存没有指定图片返回空
	 * @param passport
	 * @return
	 */
	public String get(String passport) {
		if (mActive == false) {
			return null;
		}
		if (passport != null && !passport.trim().equals("")) {
			CacheEntry cacheEntry = mNcikCacheMap.get(passport);
			if (cacheEntry != null) {
				String nick = cacheEntry.get();
				if (nick == null || "".equals(nick)) {
					mNcikCacheMap.remove(passport);
				}
				return nick;
			}
		}
		return null;
	}


	/**
	 * cache实体类。
	 * @author nieyu2
	 *
	 */
	private static class CacheEntry extends SoftReference<String> {

		private String passport;

		private CacheEntry(String nick, ReferenceQueue<String> queue, String passport) {
			super(nick, queue);
			this.passport = passport;
		}
	}

	/**
	 * 清空缓存
	 */
	public void clear() {
		clear(mNcikCacheMap);
	}

	private void clear(Map<String, CacheEntry> map) {
		map.clear();
	}
}

