CD %~dp0
SET version=%1
SET oldVersion=%2

MD tmp_%version%
CD tmp_%version%

powershell -Command "(New-Object Net.WebClient).DownloadFile('https://github.com/Lordszynencja/Charter/releases/download/%version%/Charter-windows-%version%.zip', 'tmp.zip')"
tar -xf tmp.zip

ROBOCOPY Charter ../ /S /NFL /NDL

CD ../
RD /S /Q tmp_%version%

REG COPY HKLM\Software\Lordszynencja\Charter\%oldVersion% HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\%oldVersion% /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\%oldVersion% /f

CD ../

START Charter.exe