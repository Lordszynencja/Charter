CD target
XCOPY "Charter" "Charter - full" /IE
tar.exe -a -cf "Charter - full.zip" "Charter - full"
RENAME "Charter" "Charter - Windows"
CD "Charter - Windows"
DEL "Charter.jar"
RMDIR "Charter.app" /S /Q
CD rubberband
RMDIR "rubberband-3.1.2-gpl-executable-macos" /S /Q
CD ../../
tar.exe -a -cf "Charter - Windows.zip" "Charter - Windows"