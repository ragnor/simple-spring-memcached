SIMPLE SPRING MEMCACHED 3.2.0 (2013)
------------------------------------
http://code.google.com/p/simple-spring-memcached/


To build project and execute (integration) tests two memcached instances are required (on localhost, ports 11211 and 11212). By default two embedded 
memcached (jmemcached) instances are used. Those instances are started on ports 11211 and 11212 at the beginning of integration tests and stopped after 
by maven plugin (jmemcached-maven-plugin). No need to install external memcached to build project and run integration tests. 
To use external memcached set maven property: -Djmemcached.disable=true.
 memcached -d -m 256 -l 127.0.0.1 -p 11211
 memcached -d -m 256 -l 127.0.0.1 -p 11212

Currently project can use one of two available providers:
 for xmemcached use: 
   mvn clean package -Pxmemcached
 for spymemcached use:
   mvn clean package -Pspymemcached
Above maven and spring profile settings only define what provider will be used in integration tests. 
In both cases created artifacts support spymemcached and xmemcached.
   
   
Because of using the lombok library and bug in older JVM versions (http://bugs.sun.com/view_bug.do?bug_id=6512707) you may get 
'incompatible types' errors in compilation. To prevent such errors use Java in version at least 1.6.30.


Core modules of SSM: simple-spring-memcached, spymemcached-provider and xmemcached-provider require Spring 3.0.7.RELEASE.
The spring-cache module which provides integration with Spring Cache abstraction requires Spring 3.1.2.RELEASE (Spring Cache was introduced in 3.1).
