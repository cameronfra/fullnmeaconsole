@echo off
@setlocal
java -version
::set COMMAPI_HOME=C:\_myWork\_mySoft\commapi
set COMMAPI_HOME=..\commapi
set path=%COMMAPI_HOME%;%PATH%
set classpath=%COMMAPI_HOME%\comm.jar
set classpath=%classpath%;deploy\NMEAHTTPLogger.jar
set classpath=%classpath%;C:\_myWork\_forExport\dev-corner\olivsoft\NMEAParser\deploy\nmeaparser.jar
set classpath=%classpath%;C:\_myWork\_forExport\dev-corner\olivsoft\NMEAHTTPServer\deploy\nmeaserver.jar
:: set classpath=%classpath%;%cd%\classes
set /p verbose=Verbose y^|[n] ? 
if (%verbose%) == () set verbose=n
set /p channel=Channel tcp^|serial [serial] ^> 
if (%channel%) == () set channel=serial
if (%channel%) == (serial) goto serial
set /p tcpport=TCP Port [80]               ^> 
if (%tcpport%) == () set tcpport=80
goto tcpgo
:serial    
set /p port=Port [COM1]                 ^> 
set /p   br=Baud Rate [4800]            ^> 
if (%port%) == () set port=COM1
if (%br%) == () set br=4800
java -Djava.library.path=%COMMAPI_HOME% http.main.MinimalLogger -verb %verbose% -serial %port% -br %br%
goto end
:tcpgo
java http.main.MinimalLogger -verb %verbose% -tcp %tcpport%
:end
pause
@endlocal
