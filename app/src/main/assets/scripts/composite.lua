---
--- Composite device composed of every suupported gadget
---

require('common')

kb, ms, st, sl = luausb.create(
    { type = "keyboard" },
    { type = "mouse" },
    { type = "storage" },
    { type = "serial" }
)

while true do
    print("idle")

    wait_for_state("configured")
    wait_for_detect(kb)
    print("running")

    wait(1000)

    print("done")
    wait_for_state("not attached")

    print("disconnected")

    wait(1000)
end