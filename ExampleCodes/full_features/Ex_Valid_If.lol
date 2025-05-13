item rabadon: goat = faker;
// you will win if you're faker (true)
canwin(rabadon) {
	broadcast("Good Game, Well Played!");
} lose {
	broadcast("You threw the game!");
}
// Output: Good Game, Well Played!

item minutes: stats = 3;
//Initial Statement
canwin (minutes > 15) {
	// do something
} remake (minutes < 3) {
	broadcast("gg yuumi afk");
} lose {
	ff(15);
}

// Output: ff(15) terminates program
