CD target
RENAME "Charter" "Charter - Mac"
CD "Charter - Mac"
DEL "Charter.exe"
CD rubberband
RMDIR "rubberband-3.1.2-gpl-executable-windows" /S /Q
CD ../../
tar.exe -a -cf "Charter - Mac.zip" "Charter - Mac"
move "Charter - Mac.zip" ../