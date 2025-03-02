support {
    item value: stats = 100;
    item divisor: stats = 0;
    
    canwin (divisor == 0) {
        feed "Division by zero error!";
    }
    
    item result: stats = value / divisor;
    broadcast("Result: " + result);

} carry (e) {
    broadcast("Error caught: " + e);
    broadcast("Using default value instead.");
    item result: stats = 0;
    broadcast("Result: " + result);
}

// Another example with error handling for array access
item performArrayOperation = skill (): passive -> {
    item scores: stats[] = [10, 20, 30];
    item index: stats = 5; // Out of bounds index
    
    support {
        canwin (index >= scores.length()) {
            feed "Array index out of bounds!";
        }
        broadcast("Score at index " + index + ": " + scores[index]);
    } carry (e) {
        broadcast("Error: " + e);
    }
};

performArrayOperation();
