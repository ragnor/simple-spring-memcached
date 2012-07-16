@ECHO OFF
REM build project, install artifacts and run integration tests with different settings (providers and serialization type)

REM first build project using default setting and install all artifacts in local maven reposistory
call:execute "mvn clean install -Dspring.profiles.active=xmemcached"

cd integration-test
REM execute integration-test using xmemcached and all serialization types
call:execute "mvn clean test -Pxmemcached -Dssm.defaultSerializationType=PROVIDER"
call:execute "mvn clean test -Pxmemcached -Dssm.defaultSerializationType=JAVA"
call:execute "mvn clean test -Pxmemcached -Dssm.defaultSerializationType=JSON"

REM execute integration-test using spymemcached and all serialization types
call:execute "mvn clean test -Pspymemcached -Dssm.defaultSerializationType=PROVIDER"
call:execute "mvn clean test -Pspymemcached -Dssm.defaultSerializationType=JAVA"
call:execute "mvn clean test -Pspymemcached -Dssm.defaultSerializationType=JSON"

cd ..
cd spring-cache-integration-test

REM execute spring-cache-integration-test using xmemcached and all serialization types
call:execute "mvn clean test -Pxmemcached -Dssm.defaultSerializationType=PROVIDER -Dspring.profiles.active=xmemcached"
call:execute "mvn clean test -Pxmemcached -Dssm.defaultSerializationType=JAVA -Dspring.profiles.active=xmemcached"
call:execute "mvn clean test -Pxmemcached -Dssm.defaultSerializationType=JSON -Dspring.profiles.active=xmemcached"

REM execute spring-cache-integration-test using spymemcached and all serialization types
call:execute "mvn clean test -Pspymemcached -Dssm.defaultSerializationType=PROVIDER -Dspring.profiles.active=spymemcached"
call:execute "mvn clean test -Pspymemcached -Dssm.defaultSerializationType=JAVA -Dspring.profiles.active=spymemcached"
call:execute "mvn clean test -Pspymemcached -Dssm.defaultSerializationType=JSON -Dspring.profiles.active=spymemcached"

cd ..
goto:eof


:execute
 echo Executing %~1 in %CD%
 call %~1
 if not "%ERRORLEVEL%" == "0" (
 	echo Error while executing  %~1 in %CD%
 	call:exit 2> null
 )
goto:eof

:exit
 () creates an syntax error, stops immediatly
goto:eof
 