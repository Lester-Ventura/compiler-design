// 6. Function calls containing parameters of the wrong type
item wrongPass: skill (message) -> passive = skill (item limit:message): passive ->{
    broadcast(limit+": Working!");
};
wrongPass(2);