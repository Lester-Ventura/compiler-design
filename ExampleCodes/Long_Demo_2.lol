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
    } lose {
        feed "Division by zero error!";
    }
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
    } lose {
        recast b;
    }
};

item min: skill(stats, stats) -> stats =
skill (item a: stats, item b: stats): stats -> {
    canwin (a < b) {
        recast a;
    } lose {
        recast b;
    }
};
