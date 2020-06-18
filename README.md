# Hibernate-memcached
A library for using Memcached as a second level distributed cache in Hibernate 5.
  * 支持 spymemcached 2.12.3 client
  * 支持 com.danga client
  * 支持查询缓存和对象缓存
  * 原版本只能支持到Hibernate 3.6，Hibernate 3.x和5.x代码上有本质的改动

# Help
  * 发现问题请发送邮件到 zhangyinhao1234@gmail.com

# Versions
## master
  * 基于hibernate 5.3.1.Final 进行编译和修改，和Hibernate 5.2.x有接口上区别。hibernate5.2.x无法使用
  * hibernate.memcached.hashAlgorithm=HashAlgorithm.FNV1_64_HASH
  * hibernate.memcached.connectionFactory=KetamaConnectionFactory
  * hibernate.cache.region.factory_class=com.googlecode.hibernate.memcached.MemcachedRegionFactory(spy客户端配置)
  * hibernate.cache.region.factory_class=com.googlecode.hibernate.memcached.dangamemcached.DangaMemcacheClientFactory(com.danga客户端配置)



## 1.7-5.2.17

- 基于hibernate 5.2.17.Final 进行编译和修改，理论上兼容hibernate 5.2.7.Final~hibernate 5.2.17.Final

## 1.7-5.0.12

  * 基于hibernate 5.0.12 进行编译和修改

## 1.7-5.2.7
  * 基于hibernate 5.2.7 进行编译和修改

## 1.7-5.2.1
  * 基于hibernate 5.2.1 进行编译和修改

## 1.7-5.0.11
  * 基于hibernate 5.0.11 进行编译和修改，spring boot 1.3.4使用的 hibernate5.0.11 因此基于此版本开发
  * hibernate.cache.region.factory_class=com.googlecode.hibernate.memcached.MemcachedRegionFactory
  * hibernate.memcached.hashAlgorithm=HashAlgorithm.FNV1_64_HASH
  * hibernate.memcached.connectionFactory=KetamaConnectionFactory

