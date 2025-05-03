// This is a sample created to demonstrate scoping

// this should still be accessible inside the if statement since that is inside the scope
item outside : message = "outside";

canwin(faker) {
    item inside : message = "this should only work inside of the if statement and not " + outside;
}

// This should throw an error due to an undeclared variable
<<<<<<< HEAD
broadcast(inside);
=======
broadcast(inside);
>>>>>>> 147e0ebcf61adee3252729ef4d853f9fb3cdff47
