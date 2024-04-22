call mvn -P lwjgl-natives-linux-amd64^
 -Dlwjgl.natives=natives-linux^
 clean package

DEL "target\Charter\Charter.exe"
DEL "target\Charter\jasiohost64.dll"
DEL "target\Charter\librubberband-jni.dylib"
DEL "target\Charter\rubberband-jni.dll"
RMDIR "target\Charter\Charter.app" /S /Q

tar.exe -a -cf "Charter-linux-%version%.zip" -C "target" "Charter"