CD %~dp0
SET version=%1
SET oldVersion=%2

MD tmp_%version%
CD tmp_%version%

powershell -Command "(New-Object Net.WebClient).DownloadFile('https://github.com/Lordszynencja/Charter/releases/download/%version%/Charter-windows-%version%.zip', 'tmp.zip')"
tar -xf tmp.zip

ROBOCOPY Charter ../ /MOVE /NFL /NDL

CD ../
RD /S /Q tmp_%version%

REG COPY HKLM\Software\Lordszynencja\Charter\0.21.0 HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.0 /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.0 /f
REG COPY HKLM\Software\Lordszynencja\Charter\0.21.1 HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.1 /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.1 /f
REG COPY HKLM\Software\Lordszynencja\Charter\0.21.2 HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.2 /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.2 /f
REG COPY HKLM\Software\Lordszynencja\Charter\0.21.3 HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.3 /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.3 /f
REG COPY HKLM\Software\Lordszynencja\Charter\0.21.4 HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.4 /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.4 /f
REG COPY HKLM\Software\Lordszynencja\Charter\0.21.5 HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.5 /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.5 /f
REG COPY HKLM\Software\Lordszynencja\Charter\0.21.6 HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.6 /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.6 /f
REG COPY HKLM\Software\Lordszynencja\Charter\0.21.7 HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.7 /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.7 /f
REG COPY HKLM\Software\Lordszynencja\Charter\0.21.8 HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.8 /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.8 /f
REG COPY HKLM\Software\Lordszynencja\Charter\0.21.9 HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.9 /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\0.21.9 /f
REG COPY HKLM\Software\Lordszynencja\Charter\%oldVersion% HKLM\Software\Lordszynencja\Charter\%version% /s /f
REG DELETE HKLM\Software\Lordszynencja\Charter\%oldVersion% /va /f
REG DELETE HKLM\Software\Lordszynencja\Charter\%oldVersion% /f

CD ../

START Charter.exe