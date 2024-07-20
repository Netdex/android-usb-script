---
--- Common library functions
---

-- Wait for the system to detect us by polling for the first output report
function wait_for_detect(kb)
    while true do
        local lock = kb:read_lock()
        if lock ~= nil then
            return lock
        end
        wait(100)
    end
end

function wait_for_state(state)
    while luausb.state() ~= state do
        wait(100)
    end
end

-- make it really obvious when a script is done running
function flash(kb)
    kb:press(KEY_NUMLOCK)

    wait(100)
    local lock
    while true do
        local val = kb:read_lock()
        if val == nil then break end
        lock = val
    end
    if lock == nil then return end

    if lock.num_lock then kb:press(KEY_NUMLOCK) end
    if lock.caps_lock then kb:press(KEY_CAPSLOCK) end
    if lock.scroll_lock then kb:press(KEY_SCROLLLOCK) end

    local state = luausb.state()
    while luausb.state() == state do
        kb:press(KEY_NUMLOCK, KEY_CAPSLOCK, KEY_SCROLLLOCK)
        wait(50)
        kb:press(KEY_NUMLOCK, KEY_CAPSLOCK, KEY_SCROLLLOCK)
        wait(950)
    end
end