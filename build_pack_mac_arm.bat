call mvn -P lwjgl-natives-macos-aarch64^
 -Dlwjgl.natives=natives-macos-arm64^
 clean package

DEL "target\Charter\Charter.exe"
DEL "target\Charter\jasiohost64.dll"
DEL "target\Charter\librubberband-jni.so"
DEL "target\Charter\rubberband-jni.dll"

tar.exe -a -cf "Charter-mac-arm-%version%.zip" -C "target" "Charter"