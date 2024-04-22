call mvn -P lwjgl-natives-windows-amd64^
 -Dlwjgl.natives=natives-windows^
 clean package

DEL "target\Charter\Charter.jar"
DEL "target\Charter\librubberband-jni.dylib"
DEL "target\Charter\librubberband-jni.so"
RMDIR "target\Charter\Charter.app" /S /Q

tar.exe -a -cf "Charter-windows-%version%.zip" -C "target" "Charter"