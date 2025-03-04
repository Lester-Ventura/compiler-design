steal "standard.lol";

// this is a comment

// number literals
item binaryNum: stats = 0b1010;  // 10 in decimal
item octalNum: stats = 0e12;    // 10 in decimal
item decimalNum: stats = 10;
item hexNum: stats = 0xA;       // 10 in decimal
item floatNum: stats = 10.5;

// variables
item x: stats;
item y: stats;
item result: stats;

broadcast("Enter first number: ");
x = chat();
broadcast("Enter second number: ");
y = chat();

// arithmetic operations
result = add(x, y) - subtract(x, y) * multiply(x, y) / add(x, y);
broadcast("Complex Arithmetic Result: " + result);

// bitwise operations
item bitwiseAnd: stats = x & y;
item bitwiseOr: stats = x | y;
item bitwiseXor: stats = x ^ y;
item bitwiseShift: stats = x << 2;
broadcast("Bitwise AND: " + bitwiseAnd);
broadcast("Bitwise OR: " + bitwiseOr);
broadcast("Bitwise XOR: " + bitwiseXor);
broadcast("Bitwise Shift Left: " + bitwiseShift);

// logical operations
item logicalResult: goat = (x > 5) && (y < 10) || (x == y);
broadcast("Logical Operation Result: " + logicalResult);

// if else
canwin (result > 10) {
    broadcast("Result is greater than 10");
} remake (result == 10) {
    broadcast("Result is exactly 10");
} lose {
    broadcast("Result is less than 10");
}

// switch
channel (result) {
    teleport 10: {
        broadcast("\n\tThe result is ten.");
        cancel;
    }
    teleport 15: {
        broadcast("\n\tThe result is fifteen.");
        cancel;
    }
    recall: {
        broadcast("\n\tThe result is unknown.");
    }
}

// looping
wave (x > 0) {
    broadcast("x is positive: " + x);
    x = x - 1;
} clear;

cannon (item i: stats = 0; i < 5; i = i + 1) {
    broadcast("Loop iteration: " + i);
}

cannon (item num of [1, 2, 3, 4, 5]) {
    broadcast("For each: " + num);
}

// object type declaration
build Player {
    item name: message;
    item health: stats;
    item mana: stats;
}

item myPlayer: Player;
myPlayer.name = "Invoker";
myPlayer.health = 100;
myPlayer.mana = 200;
broadcast("Player: " + myPlayer.name + ", Health: " + myPlayer.health + ", Mana: " + myPlayer.mana);

// error handling
support {
    feed "Something went wrong!";
} carry (error) {
    broadcast("Caught error: " + error);
}


