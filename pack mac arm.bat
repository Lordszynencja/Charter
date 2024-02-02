CD target
RENAME "RS Charter" "RS Charter - Mac arm"
CD "RS Charter - Mac arm"
DEL "RS Charter.exe"
CD rubberband
RMDIR "rubberband-3.1.2-gpl-executable-windows" /S /Q
CD ../../
tar.exe -a -cf "RS Charter - Mac arm.zip" "RS Charter - Mac arm"
move "RS Charter - Mac arm.zip" ../