---
--- Created by netdex.
--- DateTime: 1/6/2018 4:33 PM
---
--- template.lua: generic template for "run after plug-in" style scripts
---

while not cancelled() do
    progress("idle")

    -- poll until /dev/hidg0 is writable
    while not cancelled() and not test() do delay(1000) end
    if cancelled() then break end

    progress("running")
    delay(1000)

    send_string("test")

    progress("done")
    while not cancelled() and test() do
        delay(1000)
    end
    log("disconnected")
end