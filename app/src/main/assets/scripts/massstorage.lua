---
--- a simple default mass storage device
---

usb = luausb.create({ id = 0, type = "storage" })

while true do
    wait(1000)
end
