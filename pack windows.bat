CD target
RENAME "RS Charter" "RS Charter - Windows"
CD "RS Charter - Windows"
DEL "RS Charter.jar"
RMDIR "RS Charter.app" /S /Q
CD rubberband
RMDIR "rubberband-3.1.2-gpl-executable-macos" /S /Q
CD ../../
tar.exe -a -cf "RS Charter - Windows.zip" "RS Charter - Windows"