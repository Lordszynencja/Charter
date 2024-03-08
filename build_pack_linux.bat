call mvn -P lwjgl-natives-linux-amd64^
 -Dlwjgl.natives=natives-linux^
 clean package

DEL "target\Charter\Charter.exe"
RMDIR "target\Charter\Charter.app" /S /Q
RMDIR "target\Charter\rubberband\rubberband-3.1.2-gpl-executable-macos" /S /Q

tar.exe -a -cf "Charter-linux-%version%.zip" -C "target" "Charter"