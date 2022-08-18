usb = luausb.create({ type = "mouse", id = 0 })

ms1 = usb.dev[1]

-- poll until usb plugged in
while usb.state() == "not attached" do
    wait(1000)
end

while usb.state() == "configured" do
    ms1.click(BTN_LEFT)
    ms1.move(30, 0)
    ms1.scroll(127)
    wait(1000)
end
