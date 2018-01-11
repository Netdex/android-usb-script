# Android HID Script

**Use at your own risk. For educational purposes only.**

An Android app that provides a simple Lua interface for emulating an HID device, on top of the existing `android-keyboard-gadget` patch by `pelya`. **Root access is required.**

**Requires https://github.com/pelya/android-keyboard-gadget patch applied to the kernel to expose `/dev/hidg0` and `/dev/hidg1`.**  
If you own a `jfltexx` device, forks of `android_kernel_samsung_jf` and `android_device_jf-common` are under my account which already have this patch applied. Otherwise, you would have to find an existing kernel or apply the patch and build the kernel yourself.

## HID Emulation?
```
In computing, the USB human interface device class (USB HID class) is a part of the USB 
specification for computer peripherals: it specifies a device class (a type of computer 
hardware) for human interface devices such as keyboards, mice, game controllers and 
alphanumeric display devices.
                                                                      - Wikipedia
```
With a small kernel patch, Android devices possess the ability to emulate HID devices. That is, they are able to act as any HID device (mouse, keyboard, MIDI keyboard, etc.), even simultaneously. The `android-keyboard-gadget` patch which this project relies on exposes two HID descriptors, for mouse and keyboard. This app provides a simple interface for scripting an HID device emulator, controlling the emulated mouse and keyboard programmatically. In addition, this app contains wrappers around the HID devices, allowing developers to easily integrate HID functionality into their own apps.

On the news recently, use and abuse of the trust given to HID devices was demonstrated with the [BadUSB](https://www.wired.com/2014/07/usb-security/) attack, where USB devices were abused to utilize HID protocol to carry out nefarious actions.

## Use Cases of Scripted HID Emulation
- Automation of deployment solutions (ie. configuring computer BIOs settings in an automated fashion)
- 
- Use in computer espionage or social engineering attacks

## Features
A couple of demo applications are implemented:
- Fuzzing of HID protocol
- PowerShell download and run executable
- PowerShell download and run PowerShell script
- Serial transfer of data through output reports
- Change wallpaper ([video demonstration](https://my.mixtape.moe/zxerjz.mp4))

New demo applications can be added to `assets/scripts`. The API is pretty much self-documenting, just look at the existing demos to get a feel for how the API works.

For people who want to implement HID functionality in their own apps, HID interfacing code available [here (HID.java)](https://github.com/Netdex/android-hid-script/blob/master/app/src/main/java/cf/netdex/hidfuzzer/hid/HID.java), 
and a simple ease-of-use wrapper is available [here (HIDR.java)](https://github.com/Netdex/android-hid-script/blob/master/app/src/main/java/cf/netdex/hidfuzzer/hid/HIDR.java). The documentation should be enough to understand how it works.

## Third-party
- Requires ChainFire's [libsuperuser](https://github.com/Chainfire/libsuperuser) to keep a su shell open.
- Requires LuaJ to provide Lua binding and interpret Lua scripts.
