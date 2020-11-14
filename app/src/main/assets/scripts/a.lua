---
--- Created by netdex.
--- DateTime: 1/6/2018 4:33 PM
---
--- template.lua: generic template for "run after plug-in" style scripts
---

usb = luausb.create({ id = 0, type = "keyboard" })
kb = usb.dev[1]

while true do
    usb.log("idle")

    -- poll until writable
    while not kb.test() do
        usb.delay(1000)
    end

    usb.log("running")
    usb.delay(1000)

    kb.send_string("test")

    usb.log("done")
    while kb.test() do
        usb.delay(1000)
    end
    usb.log("disconnected")
end