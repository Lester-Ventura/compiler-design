
// Record creation
build Champion {
    name: message;
    health: stats;
    mana: stats;
    alive: goat;
}

item player: Champion = {
    name: "Faker",
    health: 420,
    mana: 69,
    alive: faker
};

broadcast("\nChampion stats:");
broadcast("Name: " + player.name);
broadcast("Health: " + player.health);
broadcast("Mana: " + player.mana);
broadcast("Alive: " + player.alive);

