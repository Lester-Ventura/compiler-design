item i: stats = 5;

wave (i > 0) {
	broadcast("\ncountdown ");
	broadcast(i); 
	i--;
}

item i: stats  =5;
wave (i < 0)
	broadcast("One line op");
