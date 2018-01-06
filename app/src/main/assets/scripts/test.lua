---
--- Created by netdex.
--- DateTime: 12/30/17 10:12 PM
---
--- test.lua: reads keyboard for light changes (num lock, caps lock, scroll lock)
---

kbl.begin()

log("start")

while not cancelled() do
    if kbl.available() then
        log("success!")
        local state = kbl.get()
        log(string.format("num: %s, caps: %s, scroll: %s", tostring(state.num), tostring(state.caps), tostring(state.scroll)))
    end
    delay(250)
end

log("cancelled")