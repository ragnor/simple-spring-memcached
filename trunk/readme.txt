SIMPLE SPRING MEMCACHED 3.0.0 (2012)
------------------------------------
http://code.google.com/p/simple-spring-memcached/


To build project and execute tests two memcached instances are required (on localhost, ports 11211 and 11212):
 memcached -d -m 256 -l 127.0.0.1 -p 11211
 memcached -d -m 256 -l 127.0.0.1 -p 11212

Currently project can use one of two available providers:
 for xmemcached use: 
   mvn clean package -Pxmemcached -Dspring.profiles.active=xmemcached
 for spymemcached use:
   mvn clean package -Pspymemcached -Dspring.profiles.active=spymemcached
Above maven and spring profile settings only define what provider will be used in integration tests. 
In both cases created artifacts support spymemcached and xmemcached.
   
   
Because of the lombok library and bug in older JVM versions (http://bugs.sun.com/view_bug.do?bug_id=6512707) you may get 
'incompatible types' errors in compilation. To prevent such errors use Java in version at least 1.6.30.


Core modules of SSM: simple-spring-memcached, spymemcached-provider and xmemcached-provider require Spring 3.0.7.RELEASE.
The spring-cache module which provides integration with Spring Cache abstraction requires Spring 3.1.2.RELEASE (Spring Cache was introduced in 3.1).
