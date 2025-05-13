// 32. Foreach variable being an incompatible type to the iterated value
item deathCount: stats[] = [10,9]
cannon(item death: message of deathCount){
    broadcast(deathCount);
}
