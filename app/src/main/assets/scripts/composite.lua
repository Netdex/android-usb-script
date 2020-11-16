---
--- Composite device composed of two keyboards
---
---
usb = luausb.create({ type = "keyboard", id = 0 }, { type = "keyboard", id = 1 })
kb1 = usb.dev[1]
kb2 = usb.dev[2]

while true do
    usb.log("idle")

    -- poll until usb plugged in
    while usb.state() == "not attached" do
        usb.delay(1000)
    end

    usb.log("running")
    usb.delay(1000)

    -- send a string from keyboard 1
    kb1.send_string("kb1")
    usb.delay(1000)
    -- send a string from keyboard 2
    kb2.send_string("kb2")

    usb.log("done")

    -- poll until usb unplugged
    while usb.state() == "configured" do
        usb.delay(1000)
    end

    usb.log("disconnected")

    usb.delay(1000)
end