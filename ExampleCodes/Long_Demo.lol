steal "standard.lol";

// primitive types
item playerName: message = "Ezreal";
item playerLevel: stats = 10;
item isAlive: goat = shaker;

// number literals
item binaryNum: stats = 0b1010;  // 10 in decimal
item octalNum: stats = 0e12;    // 10 in decimal
item decimalNum: stats = 10;
item hexNum: stats = 0xA;       // 10 in decimal
item floatNum: stats = 10.5;

// arrays
item damageValues: stats[] = [50, 75, 100, 125];

// objects, NOT SURE WITH BUILD SAMPLE CODE
build Champion {
    item name: message;
    item level: stats;
    item isActive: goat;
}

item player1: Champion; 
player1.name = "Zed"; 
player1.level = 18; 
player1.isActive = shaker;

broadcast("Player 1: " + player1.name + " (Level " + player1.level + ")");

// functions
skill attack(item damage: stats, item crit: goat): stats -> {
    canwin (crit) {
        recast damage * 2;
    } lose {
        recast damage;
    }
}

item totalDamage: stats = attack(100, shaker);
broadcast("Total damage dealt: " + totalDamage);

// arithmetic operations
item complexCalc: stats = (binaryNum + octalNum - hexNum) * decimalNum / floatNum;
broadcast("Complex Arithmetic Result: " + complexCalc);

// bitwise operations (only for stats)
item bitwiseAnd: stats = playerLevel & 7;
item bitwiseOr: stats = playerLevel | 3;
item bitwiseXor: stats = playerLevel ^ 2;
item bitwiseShift: stats = playerLevel << 1;
broadcast("Bitwise AND: " + bitwiseAnd);
broadcast("Bitwise OR: " + bitwiseOr);
broadcast("Bitwise XOR: " + bitwiseXor);
broadcast("Bitwise Shift Left: " + bitwiseShift);

// logical operations (only for goat)
item hasMana: goat = shaker;
item hasHP: goat = faker;
item canCastSpell: goat = hasMana && hasHP || isAlive;
broadcast("Can cast spell: " + canCastSpell);

// relational operators (only for stats)
canwin (playerLevel > 15) {
    broadcast("High-level champion!");
} remake (playerLevel == 15) {
    broadcast("Mid-level champion!");
} lose {
    broadcast("Low-level champion.");
}

// using teleport, and flash
channel (playerLevel) {
    teleport 10: {
        broadcast("\n\tChampion has reached level 10.");
        flash 18;  //sample usage of flash
        cancel;
    }
    teleport 18: {
        broadcast("\n\tChampion has reached max level!");
        cancel;
    }
    recall: {
        broadcast("\n\tChampion level unknown.");
    }
}

// looping through an array
cannon (item dmg of damageValues) {
    broadcast("Dealt " + dmg + " damage!");
}

// for cooldown keyword
item abilityState: goat = cooldown;

canwin (abilityState == shaker) {
    broadcast("Ability is ready!");
} remake (abilityState == cooldown) {
    broadcast("Ability is on cooldown.");
} lose {
    broadcast("Ability state unknown.");
}


// error handling
support {
    feed "Something went wrong!";
} carry (error) {
    broadcast("Caught error: " + error);
}
