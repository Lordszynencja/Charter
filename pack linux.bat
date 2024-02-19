CD target
RENAME "Charter" "Charter - Linux"
CD "Charter - Linux"
DEL "Charter.exe"
RMDIR "Charter.app" /S /Q
CD rubberband
RMDIR "rubberband-3.1.2-gpl-executable-macos" /S /Q
CD ../../
tar.exe -a -cf "Charter - Linux.zip" "Charter - Linux"
move "Charter - Linux.zip" ../