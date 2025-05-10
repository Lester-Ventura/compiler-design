// ========== CHAMPION STATS ==========
item playerName: message = "Ezreal";
item playerLevel: stats = 10;
item isAlive: goat = faker;         
item hasMana: goat = faker;
item hasHP: goat = shaker;
item abilityState: goat = shaker;  

// ========== OBJECT STRUCTURE ==========
build Champion {
    name: message;
    level: stats;
    isActive: goat;
}

item ezreal: build Champion = {
    name: playerName,
    level: playerLevel,
    isActive: isAlive
};

// ========== DAMAGE MODEL ==========
item damageValues: stats[] = [50, 75, 100, 125];

item attack: skill (stats, goat) -> stats =
skill (item damage: stats, item crit: goat): stats -> {
    canwin (crit) {
        recast damage * 2;
    } 
    recast damage;
};

item baseDamage: stats = 100;
item totalDamage: stats = attack(baseDamage, faker);
broadcast("" + playerName +" dealt total damage: " + totalDamage);

// ========== ARITHMETIC CHECK ==========
item manaCost: stats = 10;
item damageMultiplier: stats = (0b1010 + 0e12 - 0xA) * 10 / 10.5;
broadcast("Damage multiplier (complex calc) =  " + damageMultiplier);

// ========== BITWISE STATUS FLAGS ==========
item bitwiseAnd: stats = playerLevel & 7;
item bitwiseOr: stats = playerLevel | 3;
item bitwiseXor: stats = playerLevel ^ 2;
item bitwiseShift: stats = playerLevel << 1;

broadcast("Status Flag (AND): " + bitwiseAnd);
broadcast("Status Flag (OR): " + bitwiseOr);
broadcast("Status Flag (XOR): " + bitwiseXor);
broadcast("Status Flag (Shifted Level): " + bitwiseShift);

// ========== LOGICAL ABILITY CHECK ==========
item canCastSpell: goat = hasMana && hasHP || isAlive;

canwin (canCastSpell) {
    broadcast(playerName + " can cast a spell.");
} lose {
    broadcast(playerName + " cannot cast spells right now.");
}

// ========== LEVEL BRACKETING ==========
canwin (playerLevel >= 18) {
    broadcast(playerName + " is MAX LEVEL!");
} remake (playerLevel >= 11) {
    broadcast(playerName + " is MID LEVEL.");
} lose {
    broadcast(playerName + " is LOW LEVEL.");
}

// ========== TELEPORT EVENT ==========
channel (playerLevel) {
    teleport (10): {
        broadcast(playerName + " has just reached level 10!");
        flash 15;
        cancel;
    }
    teleport (15): {
        broadcast(playerName + " has just reached level 15!");
        flash 18;
        cancel;
    }
    teleport (18): {
        broadcast(playerName + " has reached MAX LEVEL!");
        cancel;
    }
    recall: {
        broadcast("Level status unknown.");
    }
}

// ========== LOOPING DAMAGE PREVIEW ==========
cannon (item dmg: stats of damageValues) {
    item critHit: stats = attack(dmg, faker);  // simulate no crit
    broadcast("Simulated hit: base " + dmg + ", actual: " + critHit);
}

// ========== ABILITY COOLDOWN ==========
canwin (abilityState == faker) {
    broadcast("Ability is READY!");
} remake (abilityState == shaker) {
    broadcast("Ability is on COOLDOWN.");
} lose {
    broadcast("Ability state unknown.");
}
