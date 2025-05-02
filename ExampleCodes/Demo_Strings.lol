// simple strings
item msg : message = "Welcome to Summoner\'s Rift"; //is a valid String.
item msgTwo : message = 'Welcome to Summoner\'s Rift'; //is a valid String.

// with variables / expressions
item playerName : message = "Perkz";
item playerMsg : message = playerName + "have slain an enemy."; 

// Concatenates the value of playerName with "have slain an enemy."
item escapeCharacters: message = "These are escape characters, \n \t \c \\ \' \" ";
item msg: message = "put two and " + "two together"; 
// this creates the string: "put two and two together"
