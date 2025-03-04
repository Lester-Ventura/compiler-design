item factorial = skill (item n: stats): stats -> {
    canwin (n <= 1) {
        recast 1;
    } lose {
        recast n * factorial(n - 1);
    }
};

// Using the function
item num: stats = 5;
broadcast("Factorial of " + num + " is: " + factorial(num));

// Another function example - calculate damage
item calculateDamage = skill (
    item baseDamage: stats,
    item armorPen: stats,
    item targetArmor: stats
): stats -> {
    item effectiveArmor: stats = targetArmor * (1 - armorPen / 100);
    item multiplier: stats = 100 / (100 + effectiveArmor);
    recast baseDamage * multiplier;
};

// Using the damage calculation
item damage: stats = calculateDamage(100, 30, 50);
broadcast("Final damage dealt: " + damage);
