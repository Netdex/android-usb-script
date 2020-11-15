---
--- Created by netdex.
--- DateTime: 1/6/2018 4:33 PM
---
--- template.lua: generic template for "run after plug-in" style scripts
---

usb = luausb.create({ type = "keyboard", id = 0 }, { type = "keyboard", id = 1 })
kb1 = usb.dev[1]
kb2 = usb.dev[2]

while true do
    --usb.log("idle")
    --
    ---- poll until writable
    --while not kb1.test() do
    --    usb.delay(1000)
    --end
    --
    --usb.log("running")
    --usb.delay(1000)
    --
    --kb1.send_string("test")
    --usb.delay(1000)
    --kb2.send_string("test")
    --
    --usb.log("done")
    --while kb1.test() do
    --    usb.delay(1000)
    --end
    --usb.log("disconnected")
    usb.delay(1000)
end