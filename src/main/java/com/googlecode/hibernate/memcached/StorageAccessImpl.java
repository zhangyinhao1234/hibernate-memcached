package com.googlecode.hibernate.memcached;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
/**
 * 
 * @author zhang
 *
 */
public class StorageAccessImpl implements DomainDataStorageAccess {
	private Log logger = LogFactory.getLog(StorageAccessImpl.class);
	private final MemcachedCache cache;

	public StorageAccessImpl(MemcachedCache cache) {
		this.cache = cache;
	}

	public MemcachedCache getCache() {
		return cache;
	}

	@Override
	public Object getFromCache(Object key, SharedSessionContractImplementor session) {
		try {
			final Item item = (Item) this.cache.get(key);
			if (item == null) {
				return null;
			}
			return item.getValue();
		} catch (Exception e) {
			logger.error("获取缓存异常", e);
			return null;
		}
	}

	@Override
	public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {
		try {
			cache.put(key, new Item(value, cache.nextTimestamp()));
		} catch (Exception e) {
			logger.error("保存缓存异常", e);
		}
	}

	@Override
	public boolean contains(Object key) {
		return cache.get(key) != null;
	}

	@Override
	public void evictData() {
		try {
			cache.clear();
		} catch (Exception e) {
			logger.error("清空缓存异常", e);
		}

	}

	@Override
	public void evictData(Object key) {
		try {
			cache.remove(key);
		} catch (Exception e) {
			logger.error("清除缓存异常", e);
		}
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
	}

	/**
	 * Wrapper type representing unlocked items.
	 */
	protected final static class Item implements Serializable, Cloneable {

		private static final long serialVersionUID = 1L;
		private final Object value;
		private final Object version;
		private final long timestamp;

		Item(Object value, long timestamp) {
			this.value = value;
			this.version = 1L;
			this.timestamp = timestamp;
		}

		Item(Object value, Object version, long timestamp) {
			this.value = value;
			this.version = version;
			this.timestamp = timestamp;
		}

		public Object getValue() {
			return value;
		}
	}

}
