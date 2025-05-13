item toDigit: skill (message) -> stats = skill (item ch: message): stats -> {
    canwin (ch == "0") recast 0;
    canwin (ch == "1") recast 1;
    canwin (ch == "2") recast 2;
    canwin (ch == "3") recast 3;
    canwin (ch == "4") recast 4;
    canwin (ch == "5") recast 5;
    canwin (ch == "6") recast 6;
    canwin (ch == "7") recast 7;
    canwin (ch == "8") recast 8;
    canwin (ch == "9") recast 9;
    recast 0;
};

item parseInt: skill (message) -> stats = skill (item str: message): stats -> {
    item ret: stats = 0;
    
    cannon (item i: stats = 0; i < str.length(); i = i + 1) {
        ret = ret * 10 + toDigit(str.charAt(i));
    }

    recast ret;
};

item x: stats = parseInt(chat("Enter the first number: "));
item y: stats = parseInt(chat("Enter the second number: "));

broadcast("Number - Number operations");
broadcast("x + y = " + (x + y));
broadcast("x - y = " + (x - y));
broadcast("x * y = " + (x * y));
broadcast("x / y = " + (x / y));
broadcast("x % y = " + (x % y));
broadcast("x ** y = " + (x ** y));
broadcast("x & y = " + (x & y));
broadcast("x | y = " + (x | y));
broadcast("x ^ y = " + (x ^ y));
broadcast("x << y = " + (x << y));
broadcast("x >> y = " + (x >> y));
broadcast("x < y = " + (x < y));
broadcast("x > y = " + (x > y));
broadcast("x <= y = " + (x <= y));
broadcast("x >= y = " + (x >= y));
broadcast("x == y = " + (x == y));
broadcast("x != y = " + (x != y));

item x_string: message = "" + x;
item y_string: message = "" + y;

broadcast("String - String operations");
broadcast("x_string + y_string = " + (x_string + y_string));
broadcast("x_string == y_string = " + (x_string == y_string));

broadcast("String - Number operations");
broadcast("x_string + y = " + (x_string + y));

item x_boolean: goat = (x & 0b1) == 1;
item y_boolean: goat = (y & 0b1) == 1;

broadcast("String - Boolean operations");
broadcast("x_string + y_boolean = " + (x_string + y_boolean));

broadcast("Boolean - Boolean operations");
broadcast("x_boolean && y_boolean = " + (x_boolean && y_boolean));
broadcast("x_boolean || y_boolean = " + (x_boolean || y_boolean));
broadcast("x_boolean == y_boolean = " + (x_boolean == y_boolean));
broadcast("x_boolean != y_boolean = " + (x_boolean != y_boolean));
