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