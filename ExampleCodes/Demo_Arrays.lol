// This should be rechecked once we get standard library stuff
// Array creation and manipulation
item champions: message[] = ["Caps", "Uzi", "Pewdiepie", "Bengi", "Perkz"];
broadcast("First champion: " + champions[0]); // Caps since he is the first champion in the array

// Array methods
champions.push("Yone");
broadcast("Team size: " + champions.length()); // 6.0 since a new element was added

// Filtering an array, this function would be passed to the champions.filter() as a parameter
item filterChampions: skill (message) -> goat = 
skill (item name: message): goat -> {
    recast name.length() > 4;
};

item longNameChampions: message[] = champions.filter(filterChampions);
broadcast("\nChampions with long names:");
cannon (item champ: message of longNameChampions) {
    broadcast(champ);
}

// Outputs: Pewdiepie, Bengi, Perkz since their name lengths are > 4

item testing: skill () -> passive = skill (): passive -> {
    dump_call_stack();
    broadcast("this is running inside demo_arrays.lol, testing");
};