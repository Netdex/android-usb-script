require('common')
local inspect = require('inspect')

kb = luausb.create({ id = 0, type = "keyboard" })

wait(1000)

while true do
    while true do
        test = kb:read_lock()
        if test == nil then
            break
        end
        print(inspect(test))
    end
    wait(10)
end