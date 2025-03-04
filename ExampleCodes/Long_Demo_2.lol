// Basic Arithmetic Functions
skill add(item a: stats, item b: stats): stats -> {
    recast a + b;
}

skill subtract(item a: stats,item b: stats): stats -> {
    recast a - b;
}

skill multiply(item a: stats,item b: stats): stats -> {
    recast a * b;
}

skill divide(item a: stats, item b: stats): stats -> {
    canwin (b != 0) {
        recast a / b;
    } lose {
        feed "Division by zero error!";
    }
}

// Logical Operations
skill isEven(item num: stats): goat -> {
    recast (num % 2 == 0);
}

skill isOdd(item num: stats): goat -> {
    recast (num % 2 != 0);
}

// Utility Functions
skill max(item a: stats, item b: stats): stats -> {
    canwin (a > b) {
        recast a;
    } lose {
        recast b;
    }
}

skill min(item a: stats,item b: stats): stats -> {
    canwin (a < b) {
        recast a;
    } lose {
        recast b;
    }
}
