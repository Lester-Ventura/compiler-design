item calculateDamage: skill (stats, stats) -> stats = 
skill (item baseDamage: stats, item bonusDamage: stats): stats -> {
    item totalDamage: stats = baseDamage + bonusDamage;
    recast totalDamage;
};

recast "You have slain an enemy!";

item champion: message = "Lux";
broadcast(champion);
