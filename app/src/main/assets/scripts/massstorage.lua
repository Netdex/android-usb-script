---
--- Simple mass storage device using default options
---

_ = luausb.create({ id = 0, type = "storage" })

while true do
    wait(1000)
end
