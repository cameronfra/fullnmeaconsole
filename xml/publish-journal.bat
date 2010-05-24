@echo off
@setlocal
@echo ----------------------------
@echo Navigation Journal
@echo and pdf generation
@echo ----------------------------
::
cd %~dp0
::
set OLIVSOFT_HOME=..\..
set CP=%OLIVSOFT_HOME%\all-libs\nauticalalmanac.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-libs\geomutil.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\xmlparserv2.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\orai18n-collation.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\orai18n-mapping.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\fnd2.zip
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\xdo-0301.jar
::
@echo Processing PDF file
:: TODO Get option(s) here
@echo Publishing
set XSL_STYLESHEET=./journal.xsl
@java -Xms256m -Xmx1024m -classpath %CP% oracle.apps.xdo.template.FOProcessor -xml .\journal.xml -xsl %XSL_STYLESHEET% -pdf journal.pdf
call journal.pdf
:end
::pause
@endlocal
