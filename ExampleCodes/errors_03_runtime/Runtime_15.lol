item x: stats = 1;

channel(x) {
	teleport(1): {
		broadcast("\nMinions have Spawned");
		cancel; 
	}
}

cancel; 
