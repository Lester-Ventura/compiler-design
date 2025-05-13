item role : message = "Top"; // input

channel(role) {
	teleport ("Top"): { 
        broadcast("You are the solo laner."); 
        cancel; 
    }
	recall: { 
        broadcast("Invalid role selected."); 
        cancel; 
    }
}
