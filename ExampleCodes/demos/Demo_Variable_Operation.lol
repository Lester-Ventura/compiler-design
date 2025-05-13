item health: stats = 100;
item mana: stats = 50;
item name: message = "Faker";
item isAlive: goat = faker;

// Arithmetic operations
item damage: stats = 25;
health = health - damage;

// Conditional statement
canwin (health > 0) {
    broadcast("Champion " + name + " has " + health + " HP remaining.");
} lose {
    broadcast("Champion " + name + " has been slain!");
    isAlive = shaker;
}

// Boolean operations
canwin (isAlive && mana > 20) {
    broadcast("Can cast a spell!");
} lose {
    broadcast("Cannot cast a spell!");
}
