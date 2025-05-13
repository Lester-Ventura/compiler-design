// 27. Switch statement labels being of a different type to the condition
item currentKillStreak: stats = 4;
channel(currentKillStreak){
    teleport("Penta"):{
        broadcast("Pentakill! Ace!");
        cancel;
    }
}