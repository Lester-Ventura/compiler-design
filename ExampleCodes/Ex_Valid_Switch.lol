item x: stats = 2;

channel(x) {
	teleport 1: {
		broadcast("\nMinions have Spawned");
		cancel; // this ends case 1
}
	teleport 2: {
	broadcast("Welcome to League of Languages");		
		flash 1; // switch specific goto
}
	recall: {
broadcast("Report jungle");
		cancel;
}
}
// Output: 
// Welcome to League of Languages
// Minions have Spawned
