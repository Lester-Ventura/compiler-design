// Basic Arithmetic Functions
item add: skill(stats, stats) -> stats =
skill (item a: stats, item b: stats): stats -> {
    recast a + b;
};

item subtract: skill(stats, stats) -> stats =
skill (item a: stats, item b: stats): stats -> {
    recast a - b;
};

item multiply: skill(stats, stats) -> stats =
skill (item a: stats, item b: stats): stats -> {
    recast a * b;
};

item divide: skill(stats, stats) -> stats =
skill (item a: stats, item b: stats): stats -> {
    canwin (b != 0) {
        recast a / b;
    }
    feed "Division by zero error!";
};

// Logical Operations
item isEven: skill(stats) -> goat =
skill (item num: stats): goat -> {
    recast (num % 2 == 0);
};

item isOdd: skill(stats) -> goat =
skill (item num: stats): goat -> {
    recast (num % 2 != 0);
};

// Utility Functions
item max: skill(stats, stats) -> stats =
skill (item a: stats, item b: stats): stats -> {
    canwin (a > b) {
        recast a;
    } 
    recast b;
};

item min: skill(stats, stats) -> stats =
skill (item a: stats, item b: stats): stats -> {
    canwin (a < b) {
        recast a;
    }
    recast b;
};


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
