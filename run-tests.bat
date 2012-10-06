@ECHO OFF
REM build project, install artifacts and run integration tests with different settings (providers and serialization type)

set startTime=%time%

REM first build project using default setting and install all artifacts in local maven reposistory
call:execute "mvn clean install"

cd integration-test
REM execute integration-test using xmemcached and all serialization types
call:execute "mvn verify -Pxmemcached -Dssm.defaultSerializationType=PROVIDER"
call:execute "mvn verify -Pxmemcached -Dssm.defaultSerializationType=JAVA"
call:execute "mvn verify -Pxmemcached -Dssm.defaultSerializationType=JSON"

REM execute integration-test using spymemcached and all serialization types
call:execute "mvn verify -Pspymemcached -Dssm.defaultSerializationType=PROVIDER"
call:execute "mvn verify -Pspymemcached -Dssm.defaultSerializationType=JAVA"
call:execute "mvn verify -Pspymemcached -Dssm.defaultSerializationType=JSON"

cd ..
cd spring-cache-integration-test

REM execute spring-cache-integration-test using xmemcached and all serialization types
call:execute "mvn verify -Pxmemcached -Dssm.defaultSerializationType=PROVIDER"
call:execute "mvn verify -Pxmemcached -Dssm.defaultSerializationType=JAVA"
call:execute "mvn verify -Pxmemcached -Dssm.defaultSerializationType=JSON"

REM execute spring-cache-integration-test using spymemcached and all serialization types
call:execute "mvn verify -Pspymemcached -Dssm.defaultSerializationType=PROVIDER"
call:execute "mvn verify -Pspymemcached -Dssm.defaultSerializationType=JAVA"
call:execute "mvn verify -Pspymemcached -Dssm.defaultSerializationType=JSON"

cd ..

echo Start Time: %startTime%
echo Finish Time: %time%

goto:eof


:execute
 echo #########    Executing: %~1 in %CD%    #########
 call %~1
 if not "%ERRORLEVEL%" == "0" (
 	echo Error while executing  %~1 in %CD%
 	call:exit 2> NUL
 ) else echo #########    Executed: %~1 in %CD%   ######### 
 
goto:eof

:exit
 () creates an syntax error, stops immediatly
goto:eof
 