---
--- Simple mass storage device using default options
---

_ = luausb.create({ type = "storage" })

while true do
    wait(1000)
end
