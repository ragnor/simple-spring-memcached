To build project and execute tests two memcached instances are required (on localhost, ports 11211 and 11212):
 memcached -d -m 256 -l 127.0.0.1 -p 11211
 memcached -d -m 256 -l 127.0.0.1 -p 11212

Currently project can use one of two available providers:
 for xmemcached use: 
   mvn clean package -Pxmemcached -Dspring.profiles.active=xmemcached
 for spymemcached use:
   mvn clean package -Pspymemcached -Dspring.profiles.active=spymemcached
