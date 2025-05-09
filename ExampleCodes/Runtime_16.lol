item champion: message = "Jhin";

channel(champion) {
	teleport("Jhin"): {
		broadcast("\nJhin, the Virtuoso, is ready!");
		cancel; // Ends Jhin's case
	}
}

flash "Lux";
