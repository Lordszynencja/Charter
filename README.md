# Charter
![GitHub License](https://img.shields.io/github/license/Lordszynencja/Charter) ![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/Lordszynencja/Charter/total) [![CodeFactor](https://www.codefactor.io/repository/github/lordszynencja/charter/badge)](https://www.codefactor.io/repository/github/lordszynencja/charter) 

![java](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/built-with/java_vector.svg) ![github](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/github_vector.svg) <img alt="maven" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/built-with/maven_vector.svg">

A simple viewer, player, and editor for guitar charts. Charter is a Free and Open Source app for Desktop (Windows, MacOS, and Linux.) using Java and jasio.

[<img src="https://github.com/user-attachments/assets/bc3fe648-daab-448d-ab9f-87b800587c84" alt="PayPal donate button" width="200"/>](https://www.paypal.com/donate/?hosted_button_id=YH2SN57E68LK8)

## Table of Contents
- [Charter](#charter)
- [Screenshots](#screenshots)
  - [Download & Install](#download--install)
    - [Windows](#windows)
    - [Linux](#linux)
    - [MacOS](#macos)
- [Contributing and Building](#contributing-and-building)
  - [Contributing](#contributing)
  - [Building](#building)

# Screenshots

![ezgif-6-9bfe91b5f2](https://github.com/Lordszynencja/Charter/assets/106457611/89a3201e-9d40-4947-a8dc-21e528c31251)

## Download & Install

Download the latest version of Charter from the [releases page](https://github.com/Lordszynencja/Charter/releases/latest).

### Windows

**Currently, the installer is recognized by some antivirus software as a trojan, Windows Defender sometimes says it's `Trojan:Script/Wacatac.B!ml`, as there is no way to fix it other than to pay M$ money.
You have to deal with it by ignoring the issue or turning check for that file off in the software, however most antivirus scanners say that the file is clean.**

Autoupdater needs to have writing permissions where you installed the program to update it, so you can change permissions for the program directory once it's installed, give it administrator privileges or install it in a directory that's not protected.

1. Download and run the installer `Charter-windows-<version>-installer.exe`.
1. Run `Charter.exe`.

Works For Windows 10 and above.

### Linux

1. You need Java 17 (JDK) or newer to run it, you can install it from [Oracle Main release](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or [OpenJDK](https://jdk.java.net/archive/).
1. Download `Charter-linux-<version>.zip` then unzip it.
1. Download [librubberband-jni.so](https://github.com/JorenSix/RubberBandJNI/blob/master/jni/JVM/librubberband-jni.so) and put them in the libraries folder, one of `~/lib`, `/usr/local/lib` or `/usr/lib`.
1. Run `Charter.jar`.

### MacOS

1. You need Java 17 (JDK) or newer to run it, you can install it from [Oracle Main release](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or [OpenJDK](https://jdk.java.net/archive/).
1. Download `Charter-mac-<version>.zip` for Intel Macs, or `Charter-mac-arm-<version>.zip` for ARM, then unzip it and move into `Applications`.
1. Download [librubberband-jni.dylib](https://github.com/JorenSix/RubberBandJNI/blob/master/jni/JVM/librubberband-jni.dylib) and put it in the libraries folder, one of `~/lib`, `/usr/local/lib` or `/usr/lib`.
1. Run `Charter.jar`.

## Contributing and Building

### Contributing
1. Install JDK 17 or newer, for example [Main release](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or [OpenJDK](https://jdk.java.net/archive/)
2. Download and install Maven from [here](https://maven.apache.org/index.html).
3. Fork the repo and make pull requests
4. Profit

You can also suggest features by making a new issue and you can submit them in the Charter [Discord](https://discord.gg/JA6Jan3pcx)!

### Building
1. Go to main directory and run `mvn clean package`
2. Built program will appear in `<dir>\target\Charter\`
