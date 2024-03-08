call mvn -P lwjgl-natives-macos-aarch64^
 -Dlwjgl.natives=natives-macos-arm64^
 clean package

DEL "target\Charter\Charter.exe"
RMDIR "target\Charter\rubberband\rubberband-3.1.2-gpl-executable-windows" /S /Q

tar.exe -a -cf "Charter-mac-arm-%version%.zip" -C "target" "Charter"