cannon(item level: stats = 0; level < 5; level++) {
    canwin(level < 3) {
        broadcast("\nFarming minions...");
    }
    lose {
        broadcast("\nWaiting for the perfect moment!");
    }
}

clear; 
