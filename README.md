# HIDFuzzer
An android app that allows you to play around with emulating an HID device.

Requires https://github.com/pelya/android-keyboard-gadget patch applied to the kernel to expose `/dev/hidg0` and `/dev/hidg1`.

Use at your own risk.

A couple of demo applications are implemented:
- Fuzzing of HID protocol
- PowerShell download and run executable
- PowerShell download and run PowerShell script
- Serial transfer of data through output reports
- Change wallpaper
- Probably more but I was too lazy to update readme