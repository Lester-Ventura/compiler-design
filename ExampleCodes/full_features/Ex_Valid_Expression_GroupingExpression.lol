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

item x: stats = parseInt(chat("Enter a number: "));
item x_boolean: goat = (x & 0b1) == 1;

broadcast("Unary operations");
broadcast("x_boolean = ((x & 0b1) == 1) = " + (x_boolean));
broadcast("!x_boolean = " + (!x_boolean));
broadcast("-x = " + (-x));