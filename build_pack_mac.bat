call mvn -P lwjgl-natives-macos-x86_64^
 -Dlwjgl.natives=natives-macos^
 clean package

DEL "target\Charter\Charter.exe"
RMDIR "target\Charter\rubberband\rubberband-3.1.2-gpl-executable-windows" /S /Q

tar.exe -a -cf "Charter-mac-%version%.zip" -C "target" "Charter"