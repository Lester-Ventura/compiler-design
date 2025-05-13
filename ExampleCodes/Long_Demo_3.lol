steal "Long_Demo_2.lol";

// ========= INPUT SECTION =========
item input1: message = chat("Enter first number: ");
item input2: message = chat("Enter second number: ");

item num1: stats = parseInt(input1);
item num2: stats = parseInt(input2);

// ========= EVEN/ODD CHECK =========
// EVEN/ODD for Number 1
canwin (isEven(num1)) {
    broadcast("Number 1 (" + num1 + ") is even");
} lose {
    broadcast("Number 1 (" + num1 + ") is odd");
}

// EVEN/ODD for Number 2
canwin (isEven(num2)) {
    broadcast("Number 2 (" + num2 + ") is even");
} lose {
    broadcast("Number 2 (" + num2 + ") is odd");
}

// ========= BASIC ARITHMETIC RESULTS =========
item sum: stats = add(num1, num2);
item difference: stats = subtract(num1, num2);
item product: stats = multiply(num1, num2);


item quotient: stats = 0;
canwin (num2 != 0) {
    quotient = divide(num1, num2);
} lose {
    feed "Cannot divide by zero!";
}

broadcast("Addition Result: " + sum);
broadcast("Subtraction Result: " + difference);
broadcast("Multiplication Result: " + product);
broadcast("Division Result: " + quotient);

// ========= COMPARISON =========
item greater: stats = max(num1, num2);
item lesser: stats = min(num1, num2);

broadcast("Greater number: " + greater);
broadcast("Lesser number: " + lesser);

// ========= BITWISE CHECK =========
item andResult: stats = num1 & num2;
item orResult: stats = num1 | num2;
item xorResult: stats = num1 ^ num2;

broadcast("Bitwise AND: " + andResult);
broadcast("Bitwise OR: " + orResult);
broadcast("Bitwise XOR: " + xorResult);

// ========= LOGICAL ANALYSIS =========
item bothOver10: goat = (num1 > 10) && (num2 > 10);
item isEqual: goat = (num1 == num2);

broadcast("Are both numbers over 10? " + bothOver10);
broadcast("Are they equal? " + isEqual);

// ========= FEEDBACK =========
canwin (sum > 100) {
    broadcast("High total! Looks like you're working with big numbers.");
} remake (sum > 50) {
    broadcast("Moderate total. That's a decent sum.");
} lose {
    broadcast("Small total. Pretty manageable!");
}
