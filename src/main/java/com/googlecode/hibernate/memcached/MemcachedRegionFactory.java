/* Copyright 2015, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.hibernate.memcached;

import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.cfg.spi.DomainDataRegionBuildingContext;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.cfg.spi.EntityDataCachingConfig;
import org.hibernate.cache.spi.*;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.cache.spi.support.RegionFactoryTemplate;
import org.hibernate.cache.spi.support.RegionNameQualifier;
import org.hibernate.cache.spi.support.StorageAccess;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MemcachedRegionFactory extends RegionFactoryTemplate {

	private final Logger log = LoggerFactory.getLogger(MemcachedRegionFactory.class);

	private final ConcurrentMap<String, MemcachedCache> caches = new ConcurrentHashMap<String, MemcachedCache>();

	private Properties properties;
	private Memcache client;

	public MemcachedRegionFactory(Properties properties) {
		this.properties = properties;
	}

	public MemcachedRegionFactory() {
	}

	/**
	 * start
	 */
	@Override
	protected void prepareForUse(SessionFactoryOptions settings, Map configValues) {
		this.properties = toProperties(configValues);
		log.info("Starting MemcachedClient...");
		try {
			client = getMemcachedClientFactory(new Config(new PropertiesHelper(properties))).createMemcacheClient();
		} catch (Exception e) {
			throw new CacheException("Unable to initialize MemcachedClient", e);
		}

	}

	private Properties toProperties(Map configValues) {
		final Properties properties = new Properties();
		properties.putAll(configValues);
		return properties;
	}

	/**
	 * stop
	 */
	@Override
	protected void releaseFromUse() {
		if (client != null) {
			log.debug("Shutting down Memcache client");
			client.shutdown();
		}
		client = null;
	}

	public boolean isMinimalPutsEnabledByDefault() {
		return true;
	}

	public AccessType getDefaultAccessType() {
		return AccessType.READ_WRITE;
	}

	public long nextTimestamp() {
		return System.currentTimeMillis() / 100;
	}

	protected MemcacheClientFactory getMemcachedClientFactory(Config config) {
		String factoryClassName = config.getMemcachedClientFactoryName();

		Constructor<?> constructor;
		try {
			constructor = Class.forName(factoryClassName).getConstructor(PropertiesHelper.class);
		} catch (ClassNotFoundException e) {
			throw new CacheException("Unable to find factory class [" + factoryClassName + "]", e);
		} catch (NoSuchMethodException e) {
			throw new CacheException(
					"Unable to find PropertiesHelper constructor for factory class [" + factoryClassName + "]", e);
		}

		MemcacheClientFactory clientFactory;
		try {
			clientFactory = (MemcacheClientFactory) constructor.newInstance(config.getPropertiesHelper());
		} catch (Exception e) {
			throw new CacheException("Unable to instantiate factory class [" + factoryClassName + "]", e);
		}

		return clientFactory;
	}

	private MemcachedCache getCache(String regionName) {
		return caches.get(regionName) == null
				? new MemcachedCache(regionName, client, new Config(new PropertiesHelper(properties)))
				: caches.get(regionName);
	}

	protected DomainDataStorageAccess createDomainDataStorageAccess(DomainDataRegionConfig regionConfig,
			DomainDataRegionBuildingContext buildingContext) {
		return new StorageAccessImpl(
				getOrCreateCache(regionConfig.getRegionName(), buildingContext.getSessionFactory()));
	}

	@Override
	protected StorageAccess createQueryResultsRegionStorageAccess(String regionName,
			SessionFactoryImplementor sessionFactory) {
		return new StorageAccessImpl(getCache(regionName));
	}

	@Override
	protected StorageAccess createTimestampsRegionStorageAccess(String regionName,
			SessionFactoryImplementor sessionFactory) {
		return new StorageAccessImpl(getCache(regionName));
	}

	protected MemcachedCache getOrCreateCache(String unqualifiedRegionName, SessionFactoryImplementor sessionFactory) {
		verifyStarted();
		final MemcachedCache cache = caches.get(unqualifiedRegionName);
		if (cache == null) {
			return getCache(unqualifiedRegionName);
		}
		return cache;
	}

}
