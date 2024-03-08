@echo off
call "set version.bat"
call "build_pack_windows.bat"
call "build_pack_linux.bat"
call "build_pack_mac.bat"
call "build_pack_mac_arm.bat"