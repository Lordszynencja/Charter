CALL mvn -P lwjgl-natives-macos-aarch64^
 -Dlwjgl.natives=natives-macos-arm64^
 clean package

tar.exe -a -cf "Charter-mac-arm-%version%.zip" -C "target" "Charter"