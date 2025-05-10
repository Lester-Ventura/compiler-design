// a number is not an array
cannon (item num: stats of 1) {  
	broadcast(num);
}     

// must iterate through an expression
cannon (item c: stats of ) {     
	broadcast(c);
}         

// must be of the same type
cannon (item stat: stats of ["A", "B", "C"]) { 
	broadcast(stat);
} 

// LoLang cannot iterate through the characters of a String
cannon (item c: message of "String") { 
 	broadcast(c);
}

// No braces for loop body
cannon (item win: stats of wins)
	broadcast(win);
