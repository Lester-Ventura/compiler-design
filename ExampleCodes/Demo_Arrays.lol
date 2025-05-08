// This should be rechecked once we get standard library stuff
// Array creation and manipulation
item champions: message[] = ["Caps", "Uzi", "Pewdiepie", "Bengi", "Perkz"];
broadcast("First champion: " + champions[0]);

// Array methods
champions.push("Yone");
broadcast("Team size: " + champions.length());

// Filtering an array
item filterChampions: skill (message) -> goat = 
skill (item name: message): goat -> {
    recast name.length() > 4;
};

item longNameChampions: message[] = champions.filter(filterChampions);
broadcast("\nChampions with long names:");
cannon (item champ: message of longNameChampions) {
    broadcast(champ);
}

item testing: skill () -> passive = skill (): passive -> {
    dump_call_stack();
    broadcast("this is running inside demo_arrays.lol, testing");
};