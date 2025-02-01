# Charter
![GitHub License](https://img.shields.io/github/license/Lordszynencja/Charter) ![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/Lordszynencja/Charter/total) [![CodeFactor](https://www.codefactor.io/repository/github/lordszynencja/charter/badge)](https://www.codefactor.io/repository/github/lordszynencja/charter) 

 ![java](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/built-with/java_vector.svg) ![github](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/github_vector.svg) <img alt="maven" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/built-with/maven_vector.svg">

A simple viewer, player, and editor for guitar charts. Charter is a Free and Open Source app for Desktop (Windows, MacOS, and Linux.) using Java and jasio.

## Table of Contents
- [Charter](#charter)
- [Screenshots](#screenshots)
  - [Download & Install](#download--install)
    - [Windows](#windows)
    - [MacOS](#macos)
    - [Linux](#linux)
- [TODO](#todo)
- [Contributing and Building](#contributing-and-building)
  - [Contributing](#contributing)
  - [Building](#building)

# Screenshots

![ezgif-6-9bfe91b5f2](https://github.com/Lordszynencja/Charter/assets/106457611/89a3201e-9d40-4947-a8dc-21e528c31251)

## Download & Install

Download the latest version of Charter from the [releases page](https://github.com/Lordszynencja/Charter/releases/latest).

### Windows

Download `Charter-windows-<version>.zip` then unzip it, move `jasiohost64.dll` and `rubberband-jni.dll` to the `Windows\system32` folder, then run `Charter.exe`.

Works For Windows 10 and above

### MacOS

Download `Charter-mac-<version>.zip` then unzip it, move libraries to the correct folder, then run `Charter.jar`.

For Apple Silicon/ARM (M1/M2/M3) based Macs, download `Charter-mac-arm-<version>.zip` then unzip it. Then run `Charter.jar`.

### Linux

Download `Charter-linux-<version>.zip` then unzip it, move libraries to the correct folder, then run `Charter.jar`.

## TODO

NOTE: This list is subject to change.

<details>
  <summary>TODO</summary>
  
new features:
- ctrl + rclick to split hand shape
- improve lyric tapping
- option to add/remove audio at the end of the song
- octave up low pass option
- ER options for bonus rhythm
- more validations
- straightening beats when anchor is removed
- vocal notes creation/deletion revamp
- preview of beats' movements when dragging them
- ability to shift all further beats
- smarter slides and linked notes
- add autogenerated fhp after slide ends
- join linked notes with same flags
- change vibrato tail for linked notes
- modern theme tail types
- auto fhp creating moving top fret instead of bottom fret if new is above
- slide shape options
- tab notation under edit area
- add tabs with arrangements
- new select options:
> - select all after etc.
> - select by clicking note tail
> - select like
> - select by string with list of filters?
- handling strings beyond max?
- FHP creation switch stretch/make new/ask user?
- multiple tracks preview
- quick paste section/phrase tab
- stems
- spectrogram

bugs:
- 3D view repeat FHPs
- gp8 import hand shapes
- error checks for wrong fhps for handshapes
- gp5 import triplet feel
- bend gp5 import positions
- midi bass
- check tempo moving notes
- After accidentally linking the previous note to a chord at the start of a handshape (which removed the chord) when I recreated it, the notes behaved like individual notes and not like a chord. No matter what I tried I could not get it to properly treat it like a chord again. This caused it to display odd in both the 3D preview and in Rocksmith after building the project.

formats to add:
- psarc
  
3D preview:
- exploding notes with shaky camera
- arpeggios colored note shadows
- editable camera settings
- camera more like in RS
- string vibrating when plucked

Future things:
- Campaign/Guitarcade
- NAM support/integration (Neural Amp Module)
- Local/Online Multiplayer

</details>


## Contributing and Building

#### Contributing
1. Install Java Development Kit 16 or newer, for example [Main release](https://www.oracle.com/java/technologies/javase/jdk16-archive-downloads.html) or [OpenJDK]([https://jdk.java.net/](https://jdk.java.net/archive/))
2. Download and install Maven from [here](https://maven.apache.org/index.html).
3. Fork the repo and make pull requests
4. Profit

You can also suggest features by making a new issue and you can submit them in the RSCharter [Discord](https://discord.gg/JA6Jan3pcx)!

#### Building
1. Go to main directory and run `mvn clean package`
2. Built program will appear in `<dir>\target\Charter\`
