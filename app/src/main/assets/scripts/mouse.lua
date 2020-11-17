usb = luausb.create({ type = "mouse", id = 0 })

ms1 = usb.dev[1]

while true do
    ms1.click(BTN_LEFT)
    ms1.move(30, 0)
    ms1.scroll(128)
    usb.delay(1000)
end
