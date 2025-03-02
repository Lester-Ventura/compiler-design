// Array creation and manipulation
item champions: message[] = ["Caps", "Uzi", "Pewdiepie", "Bengi", "Perkz"];
broadcast("First champion: " + champions[0]);

// Array methods
champions.push("Yone");
broadcast("Team size: " + champions.length());

// Filtering an array
item filterChampions = skill (item name: message): goat -> {
    recast name.length() > 4;
};

item longNameChampions: message[] = champions.filter(filterChampions);
broadcast("\nChampions with long names:");
cannon (item champ of longNameChampions) {
    broadcast(champ);
}
