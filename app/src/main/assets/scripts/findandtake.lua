---
--- Copy a file from the system to a mass storage gadget
---
usb = luausb.create({ id = 0, type = "keyboard"}, {id = 0, type = "storage" })
kb = usb.dev[1]

local LABEL = "MY_DRIVE_LABEL" -- label of the drive (as assigned by you)

while true do
    print("idle")

    -- poll until usb plugged in
    while usb.state() == "not attached" do
        wait(1000)
    end

    print("running")
    -- wait 1 second for things to settle down
    wait(1000)

    kb.chord(MOD_LSUPER, KEY_R)
    wait(1000)
    kb.string("powershell\n")
    wait(2000)
    kb.string("$drive = Get-WmiObject -Class Win32_LogicalDisk -Filter \"VolumeName='" .. LABEL .. "'\" | Select -Expand DeviceID\n")

    print("done")
    -- poll until usb unplugged
    while usb.state() == "configured" do
        wait(1000)
    end
    print("disconnected")
end

