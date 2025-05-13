// 30. Switch goto statements referencing non-existent labels
item currentKillStreak: stats = 5;
channel(currentKillStreak){
    teleport(5):{
        broadcast("Pentakill!");
        flash 4;
        cancel;
    }
}