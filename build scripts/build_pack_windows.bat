CALL mvn -P lwjgl-natives-windows-amd64^
 -Dlwjgl.natives=natives-windows^
 clean package

tar.exe -a -cf "Charter-windows-%version%.zip" -C "target" "Charter"

jpackage -i target/Charter^
 --main-jar Charter.jar^
 --win-dir-chooser^
 --win-shortcut^
 --icon src/main/resources/icon.ico^
 --app-version "%version%"^
 --vendor Lordszynencja^
 --license-file LICENSE^
 --copyright "SBD 3-Clause"^
 -n Charter^
 --file-associations AssociationProjectFile.properties^
 -d target

CD target
REN "Charter-%version%.exe" "Charter-windows-%version%-installer.exe"
MOVE "Charter-windows-%version%-installer.exe" ../
CD ../