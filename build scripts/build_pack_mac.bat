CALL mvn -P lwjgl-natives-macos-x86_64^
 -Dlwjgl.natives=natives-macos^
 clean package

tar.exe -a -cf "Charter-mac-%version%.zip" -C "target" "Charter"