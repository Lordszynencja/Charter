@echo off

FOR /f "tokens=3delims=<>" %%a in ('findstr "<version>[^<]" pom.xml') do (
	set version=%%a
	goto :EOF
)