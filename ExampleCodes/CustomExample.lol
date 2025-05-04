broadcast("Debug for equals");

item comma: message = ",";

// so this works
broadcast("," == ",");

// but how about this
broadcast(comma == ",");

// maybe it's charAt
broadcast(comma.charAt(0) == ",");