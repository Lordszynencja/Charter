CD %~dp0

SET version=%1
SET oldVersion=%2
ECHO Set versions to %oldVersion% -> %version%

ECHO Creating folder 'tmp_%version%'
MD tmp_%version%
CD tmp_%version%
ECHO Created folder 'tmp_%version%'

ECHO Downloading new version zip
powershell -Command "(New-Object Net.WebClient).DownloadFile('https://github.com/Lordszynencja/Charter/releases/download/%version%/Charter-windows-%version%.zip', 'tmp.zip')"
ECHO Downloaded new version zip

ECHO Unpacking new version zip
tar -xf tmp.zip
ECHO Unpacked new version zip

ECHO Copying new files
ROBOCOPY Charter ../ /S /NFL /NDL /NJH /NJS /NS /NC /NP > nul
ECHO Copied new files

ECHO Removing temporary folder
CD ../
RD /S /Q tmp_%version%
ECHO Removed temporary folder

ECHO Updating registry keys
REG COPY HKLM\Software\Lordszynencja\Charter\%oldVersion% HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\%oldVersion% /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\%oldVersion% /f
ECHO Updated registry keys

CD ../

ECHO Starting Charter
START Charter.exe