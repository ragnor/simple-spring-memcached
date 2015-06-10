Simple Spring Memcached
=======================

A drop-in library to enable memcached caching in Spring beans via annotations.

**Source code and issue tracking are available on [github](https://github.com/ragnor/simple-spring-memcached),** **documentation is on [google code](https://code.google.com/p/simple-spring-memcached) but soon it will be migrated here.**

## Introduction ##

Distributed caching can be a big, hairy, intricate, and complex proposition when using it extensively.

Simple Spring Memcached (SSM) attempts to simplify implementation for several basic use cases.

**(09-06-2015) New version 3.6.0 with Amazon ElastiCache support is available! Since version 3.0.0 it can work as a cache back-end in Spring Cache (@Cacheable). Please check [release notes](https://code.google.com/p/simple-spring-memcached/wiki/ReleaseNotes).**

This project enables caching in Spring-managed beans, by using Java 5 Annotations and Spring/AspectJ AOP on top of the [spymemcached](http://code.google.com/p/spymemcached/), [xmemcached](http://code.google.com/p/xmemcached/) or [aws-elasticache](https://github.com/amazonwebservices/aws-elasticache-cluster-client-memcached-for-java) client. Using Simple Spring Memcached requires only a little bit of configuration and the addition of some specific annotations on the methods whose output or input is being cached. 


## Usage ##

If you are using maven, you can try it now:

    <dependencies>
       <dependency>
         <groupId>com.google.code.simple-spring-memcached</groupId>
         <artifactId>xmemcached-provider</artifactId>
         <version>3.6.0</version>
       </dependency> 
    </dependencies>

and define connection to memcached on localhost:

    <beans xmlns="http://www.springframework.org/schema/beans" 
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
            http://www.springframework.org/schema/aop
            http://www.springframework.org/schema/aop/spring-aop-3.0.xsd">

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

If you already using Spring Cache you may use SSM as an another [back-end](https://code.google.com/p/simple-spring-memcached/wiki/Getting_Started#Spring_3.1_Cache_Integration).

Need more? Please read [getting started guide](https://code.google.com/p/simple-spring-memcached/wiki/Getting_Started).

## Contact Us ##

If you have any questions, feel free to ask them on the [Google Group](http://groups.google.com/group/simple-spring-memecached). (UPDATE: Sorry, this link was bad up until 02 Aug '09, because I fat-fingered when creating the Google Group. I incorrectly misspelled it as 'simple-spring-memEcached'. So sorry about that!)

Also, let us know if you are using SSM in your project, and we will list it in on the Wiki. 
