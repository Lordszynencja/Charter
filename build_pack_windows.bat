call mvn -P lwjgl-natives-windows-amd64^
 -Dlwjgl.natives=natives-windows^
 clean package

DEL "target\Charter\Charter.jar"
RMDIR "target\Charter\Charter.app" /S /Q
RMDIR "target\Charter\rubberband\rubberband-3.1.2-gpl-executable-macos" /S /Q

tar.exe -a -cf "Charter-windows-%version%.zip" -C "target" "Charter"