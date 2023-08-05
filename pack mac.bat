CD target
RENAME "RS Charter" "RS Charter - Mac"
CD "RS Charter - Mac"
DEL "RS Charter.exe"
CD rubberband
RMDIR "rubberband-3.1.2-gpl-executable-windows" /S /Q
CD ../../
tar.exe -a -cf "RS Charter - Mac.zip" "RS Charter - Mac"
move "RS Charter - Mac.zip" ../