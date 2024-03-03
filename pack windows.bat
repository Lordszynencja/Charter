CD target
RENAME "Charter" "Charter - Windows"
CD "Charter - Windows"
DEL "Charter.jar"
RMDIR "Charter.app" /S /Q
CD rubberband
RMDIR "rubberband-3.1.2-gpl-executable-macos" /S /Q
CD ../../
tar.exe -a -cf "Charter - Windows.zip" "Charter - Windows"
move "Charter - Windows.zip" ../
cd ../