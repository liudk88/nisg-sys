package com.hcxinan.sys.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 实现缓存
 * @author liudk
 * @param <K>
 * @param <V>
 */
public abstract class AbstractLoadCache<K, V> {
	// 配置缓存条目的大小
	public int maximumSize = 10;

	protected int expireAfterAccess = 12;
	// 配置数据加载到缓存后的刷新时间
	protected int refreshAfterWrite = 12;
	// 配置数据加载到缓存后的移除时间
	protected int expireAfterWrite = 12;
	/*配置每次读取后多久刷新*/
	protected TimeUnit expireAfterAccess_timeUnit=TimeUnit.MINUTES;
	protected TimeUnit refreshAfterWrite_timeUnit=TimeUnit.HOURS;

	/**
	 * 数据缓存类的构建
	 */
	LoadingCache<K, V> cache = CacheBuilder.newBuilder()
			// 设计缓存条目
			.maximumSize(maximumSize)
			//每次读取后,间隔多久重新刷新取
			.expireAfterAccess(expireAfterAccess,expireAfterAccess_timeUnit.SECONDS)
			// 设计刷新时间
			.refreshAfterWrite(refreshAfterWrite, refreshAfterWrite_timeUnit)
			.build(new CacheLoader<K, V>() {
				@Override
				public V load(K key) throws Exception {
					// 执行缓存数据方法获取数据，cache可以根据key来判断是否需要执行getData(key)方法
					return getData(key);
				}
			});
	
	
	/**
	 * 抽象方法，执行缓存方法
	 * 
	 * @param key
	 * @return
	 */
	protected abstract V getData(K key);

	/**
	 * 清楚单个缓存
	 * 
	 * @param key
	 */
	public void reload(K key) {
		cache.invalidate(key);
	}

	/**
	 * 清楚所有缓存
	 */
	public void reload() {
		cache.invalidateAll();
	}
	
	/**根据Key获取值
	 * @param key
	 * @return
	 */
	public V get(K key){
		try {
			return cache.get(key);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}

	/**根据Key获取值
	 * @param key
	 * @return
	 */
	public V getReload(K key){
		try {
			reload(key);
			return cache.get(key);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
}
