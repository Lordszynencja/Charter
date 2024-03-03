CD target
RENAME "Charter" "Charter - Mac arm"
CD "Charter - Mac arm"
DEL "Charter.exe"
CD rubberband
RMDIR "rubberband-3.1.2-gpl-executable-windows" /S /Q
CD ../../
tar.exe -a -cf "Charter - Mac arm.zip" "Charter - Mac arm"
move "Charter - Mac arm.zip" ../
cd ../