# HIDFuzzer
Android app that provides API along with sample applications to act as an HID device, allowing an Android phone to act as a BadUSB.

Requires https://github.com/pelya/android-keyboard-gadget patch applied to the kernel to expose `/dev/hidg0` and `/dev/hidg1`.

Use at your own risk.

Currently contains 3 tasks:
- Fuzzer: sends random data as HID device (use at own risk)
- Test: types test string
- Download: downloads file on Windows computers
