---
--- Composite device composed of two keyboards
---
---

require('common')

kb1, kb2 = luausb.create({ type = "keyboard" }, { type = "keyboard" })

while true do
    print("idle")

    wait_for_state("configured")
    wait_for_detect(kb1)
    print("running")

    -- send a string from keyboard 1
    kb1:string("kb1")
    wait(1000)
    -- send a string from keyboard 2
    kb2:string("kb2")

    print("done")
    wait_for_state("not attached")

    print("disconnected")

    wait(1000)
end