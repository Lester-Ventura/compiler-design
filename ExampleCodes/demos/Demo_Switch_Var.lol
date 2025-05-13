item role : message = "Support";

channel(role) {
    teleport ("Top"): {
        broadcast("You are the solo laner.");
        cancel;
    }
    teleport ("Jungle"): {
        broadcast("You control the map objectives.");
        cancel;
    }
    teleport ("Mid"): {
        broadcast("You are the main carry.");
        cancel;
    }
    teleport ("ADC"): {
        broadcast("You deal sustained damage.");
        cancel;
    }
    teleport ("Support"): {
        broadcast("You protect your teammates.");
        flash "Jungle"; // Demonstrates goto functionality
    }
    recall: {
        broadcast("Invalid role selected.");
        cancel;
    }
}
