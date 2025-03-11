@echo off
CALL "build scripts/set version.bat"
CALL "build scripts/build_pack_windows.bat"
CALL "build scripts/build_pack_linux.bat"
CALL "build scripts/build_pack_mac.bat"
CALL "build scripts/build_pack_mac_arm.bat"