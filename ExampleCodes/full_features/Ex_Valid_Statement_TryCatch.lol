support {
	/* this is assumed to be error-prone code */
	item team : message = "Bili-bili Gaming";
    
    canwin(team == "t1"){
        broadcast("T1 are the past, the present, and the future.");
    } lose {
        // this throws the error along with the accompanying message
        feed "It is not your time yet."; 
    }
} carry (e) {
    broadcast(e);
	broadcast("Knight sucks at Ahri. L."); 
}
