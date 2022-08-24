---
--- Composite device composed of two keyboards
---
---

require('common')

kb1, kb2 = luausb.create({ type = "keyboard", id = 0 }, { type = "keyboard", id = 1 })

while true do
    print("idle")

    -- poll until usb plugged in
    while luausb.state() == "not attached" do
        wait(1000)
    end

    wait_for_detect(kb)
    print("running")

    -- send a string from keyboard 1
    kb1:string("kb1")
    wait(1000)
    -- send a string from keyboard 2
    kb2:string("kb2")

    print("done")

    -- poll until usb unplugged
    while luausb.state() == "configured" do
        wait(1000)
    end

    print("disconnected")

    wait(1000)
end