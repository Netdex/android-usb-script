---
--- wallpaper.lua: Changes a Windows 10 desktop wallpaper
---

usb = luausb.create({ id = 0, type = "keyboard" })
kb = usb.dev[1]

local file = usb.ask("Wallpaper to download?", "https://i.imgur.com/46wWHZ3.png")

while true do
    usb.log("idle")

    -- poll until usb plugged in
    while usb.state() == "not attached" do
        usb.delay(1000)
    end

    usb.log("running")
    usb.delay(1000)

    kb.press_keys(MOD_LSUPER, KEY_R)
    usb.delay(2000)
    kb.send_string("powershell\n")
    usb.delay(2000)
    kb.send_string("[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12;" ..
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

    usb.log("done")
    -- poll until usb unplugged
    while usb.state() == "configured" do
        usb.delay(1000)
    end
end