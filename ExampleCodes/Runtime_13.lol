// lenient flag off
item championName: message = "faker";

canwin (championName) {
    broadcast("Faker is on the Rift. Play around mid.");
} lose {
    broadcast("Just wait for late game.");
}

// lenient flag on
item surrenderVote: stats = 0;

canwin (surrenderVote) {
    broadcast("The team has surrendered.");
} lose {
    broadcast("Game continues.");
}
