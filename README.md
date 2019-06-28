Simple Spring Memcached
=======================

A drop-in library to enable memcached caching in Spring beans via annotations.

**Most of documentation has been moved to github but still some docs are available only on [google code](https://code.google.com/p/simple-spring-memcached).**

## Introduction ##

Distributed caching can be a big, hairy, intricate, and complex proposition when using it extensively.

Simple Spring Memcached (SSM) attempts to simplify implementation for several basic use cases.

**(28-06-2019) New version 4.1.3 with Amazon ElastiCache, Spring 5.1/5.0/4.3 and Java based configuration support is available!
SSM can also work as a cache back-end in Spring Cache (@Cacheable). Please check [release notes](https://github.com/ragnor/simple-spring-memcached/wiki/Relase-notes).** 

This project enables caching in Spring-managed beans, by using Java 5 Annotations and Spring/AspectJ AOP on top of the [spymemcached](https://github.com/couchbase/spymemcached), [xmemcached](https://github.com/killme2008/xmemcached/) or [aws-elasticache](https://github.com/amazonwebservices/aws-elasticache-cluster-client-memcached-for-java) client. Using Simple Spring Memcached requires only a little bit of configuration and the addition of some specific annotations on the methods whose output or input is being cached. 


## Usage ##

If you are using maven, you can try it now:

    <dependencies>
       <dependency>
         <groupId>com.google.code.simple-spring-memcached</groupId>
         <artifactId>xmemcached-provider</artifactId>
         <version>4.1.3</version>
       </dependency> 
    </dependencies>

and define connection to memcached on localhost using java based configuration:
	
    @Configuration
    public class LocalSSMConfiguration extends AbstractSSMConfiguration {
	    @Bean
	    @Overwrite
	    public CacheFactory defaultMemcachedClient() {
	        final CacheConfiguration conf = new CacheConfiguration();
	        conf.setConsistentHashing(true);
	        final CacheFactory cf = new CacheFactory();
	        cf.setCacheClientFactory(new com.google.code.ssm.providers.xmemcached.MemcacheClientFactoryImpl());
	        cf.setAddressProvider(new DefaultAddressProvider("127.0.0.1:11211"));
	        cf.setConfiguration(conf);
	        return cf;
	    }
	} 

or in old fashion way using XML:

    <beans xmlns="http://www.springframework.org/schema/beans" xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans-4.3.xsd
            http://www.springframework.org/schema/aop
            http://www.springframework.org/schema/aop/spring-aop-4.3.xsd">

      <import resource="simplesm-context.xml" />
      <aop:aspectj-autoproxy />

      <bean name="defaultMemcachedClient" class="com.google.code.ssm.CacheFactory">
          <property name="cacheClientFactory">
                <bean class="com.google.code.ssm.providers.xmemcached.MemcacheClientFactoryImpl" />
          </property>
          <property name="addressProvider">
                <bean class="com.google.code.ssm.config.DefaultAddressProvider">
                     <property name="address" value="127.0.0.1:11211" />
                </bean>
          </property>
          <property name="configuration">
                <bean class="com.google.code.ssm.providers.CacheConfiguration">
                      <property name="consistentHashing" value="true" />
                </bean>
          </property>
       </bean>
    </beans>

Now you can annotate method to cache result:

    @ReadThroughSingleCache(namespace = "CplxObj", expiration = 3600)
    public ComplexObject getComplexObjectFromDB(@ParameterValueKeyProvider Long complexObjectPk) {
      // ...
      return result;
    }

If you already using Spring Cache you may use SSM as an another [back-end](https://github.com/ragnor/simple-spring-memcached/wiki/Getting-Started#spring-31-cache-integration).

Need more? Please read [getting started guide](https://github.com/ragnor/simple-spring-memcached/wiki/Getting-Started).

## Documentation ##
Project documentation is available on [SSM wiki](https://github.com/ragnor/simple-spring-memcached/wiki).  
Javadocs of current release are hosted on [github.io](http://ragnor.github.io/simple-spring-memcached/).  
Source code from master branch is built and tested on:
* codeship: [ ![Codeship Status for ragnor/simple-spring-memcached](https://app.codeship.com/projects/ceb653a0-aee7-0136-c667-468d14f4260c/status?branch=master)](https://app.codeship.com/projects/310061)

## Contact Us ##

If you have any questions, feel free to ask them on the [Google Group](http://groups.google.com/group/simple-spring-memecached). (UPDATE: Sorry, this link was bad up until 02 Aug '09, because I fat-fingered when creating the Google Group. I incorrectly misspelled it as 'simple-spring-memEcached'. So sorry about that!)

Also, let us know if you are using SSM in your project, and we will list it in on the Wiki.
