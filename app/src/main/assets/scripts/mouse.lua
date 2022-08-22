---
--- Draw some cool circles using the mouse
---

ms1 = luausb.create({ type = "mouse", id = 0 })

-- poll until usb plugged in
while luausb.state() == "not attached" do
    wait(1000)
end

t = 0
s = 0.05
r = 200
x = r
y = 0
while luausb.state() == "configured" do
    ax, ay = r * math.cos(t), r * math.sin(t)
    dx, dy = math.floor(ax - x), math.floor(ay - y)
    x, y = x + dx, y + dy
    t = t + s
    ms1.move(dx, dy)
end
