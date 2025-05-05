item testing: message[] = [
	"Hello World!",
	"This is a test"
];

item lengths: stats[] = testing.map(skill (item x: message): stats -> {
	item testing: message = x;
	
	dump_symbol_table();
	recast x.length();
});

item firstCharacters: message[] = testing.map(skill (item x222: message): message -> {
	recast x222.charAt(0);
});

broadcast(lengths);
broadcast(firstCharacters);
