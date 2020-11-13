---
--- Created by netdex.
--- DateTime: 1/6/2018 4:32 PM
---
--- fuzzer.lua: fuzzes HID protocol
---

math.randomseed(os.time())

while not cancelled() do
    log("idle")

    -- poll until /dev/hidg0 is writable
    while not cancelled() and not test() do delay(1000) end
    if cancelled() then break end

    log("running")
    delay(1000)

    while test() and not isCancelled() do
        kbuf = {}
        mbuf = {}
        --for i=1,7 do kbuf[i] = math.random(0, )end
        --for i=1,4 do end
    end
    kbuf = {}
    mbuf = {}
    log("done")
    while not cancelled() and test() do
        delay(1000)
    end
    log("disconnected")
end