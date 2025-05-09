// An example showing different array methods in LoLang
item champions: message[] = ["Caps", "Uzi", "Pewdiepie", "Bengi", "Perkz", "Miss Fortune"];
item THRESHOLD: stats = 6;

// Array::filter
item longNameChampions: message[] = champions.filter(skill (item name: message): goat -> {
    recast name.length() > THRESHOLD;
});

broadcast("\n\n\nChampions with long names:");
broadcast(longNameChampions);

// Array::map
item modifiedChampions: message[] = champions.map(skill (item name: message): message -> {
  recast name + " is a champion";
});

broadcast("\n\n\nModified champions list:");
broadcast(modifiedChampions);

// Array::push
item T1Roster: message[] = ["Doran", "Oner", "Faker", "Gumayushi", "Keria", "Smash"];
cannon(item champ: message of T1Roster){
  champions.push(champ);
}

// Remove "Smash" from the Roster
champions.pop(); 

broadcast("\n\n\nNew Value of champions:");
broadcast(champions);
