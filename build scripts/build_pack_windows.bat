Echo Building with Maven
CALL mvn -P lwjgl-natives-windows-amd64^
 -Dlwjgl.natives=natives-windows^
 clean package

echo detecting required modules
jdeps -q^
 --multi-release 17^
 --ignore-missing-deps^
 --print-module-deps^
 --class-path "target\libs"^
 --module-path "%JAVAFX_HOME%\lib"^
 --add-modules=javafx.base^
 --add-modules=javafx.swing^
 target\Charter.jar >temp.txt

echo Dependencies:
more temp.txt
set /P detected_modules= <temp.txt
del temp.txt

echo detected modules: %detected_modules%

Echo Packing into a zip
tar.exe -a -cf "Charter-windows-%version%.zip" -C "target" "Charter"

Echo Creating runtime image
jlink ^
 --module-path "%JAVAFX_HOME%\lib"^
 --add-modules=javafx.base^
 --add-modules=javafx.controls^
 --add-modules=javafx.fxml^
 --add-modules=javafx.graphics^
 --add-modules=javafx.web^
 --add-modules=javafx.media^
 --add-modules=javafx.swing^
 --bind-services^
 --output target/java-runtime

Echo Creating installer
jpackage -i target/Charter^
 --main-jar Charter.jar^
 --win-dir-chooser^
 --win-shortcut^
 --icon src/main/resources/icon.ico^
 --app-version "%version%"^
 --vendor Lordszynencja^
 --license-file LICENSE^
 --copyright "SBD 3-Clause"^
 -n CharterTest^
 --file-associations AssociationProjectFile.properties^
 --java-options '-Djava.library.path=$APPDIR'^
 --runtime-image target/java-runtime^
 -t msi^
 -d target

CD target
REN "CharterTest-%version%.msi" "CharterTest-windows-%version%-installer.msi"
MOVE "CharterTest-windows-%version%-installer.msi" ../
CD ../