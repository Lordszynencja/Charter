call mvn -P lwjgl-natives-linux-amd64^
 -Dlwjgl.natives=natives-linux^
 clean package

tar.exe -a -cf "Charter-linux-%version%.zip" -C "target" "Charter"