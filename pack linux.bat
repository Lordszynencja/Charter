CD target
RENAME "RS Charter" "RS Charter - Linux"
CD "RS Charter - Linux"
DEL "RS Charter.exe"
RMDIR "RS Charter.app" /S /Q
CD rubberband
RMDIR "rubberband-3.1.2-gpl-executable-macos" /S /Q
CD ../../
tar.exe -a -cf "RS Charter - Linux.zip" "RS Charter - Linux"