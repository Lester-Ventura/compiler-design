// This is a sample created to demonstrate scoping

// this should still be accessible inside the if statement since that is inside the scope
item outside : message = "outside";
item inside : message;

cannon(faker) {
    inside = "this should work " + outside + " now";
}

// This should now throw an error because we have declared the variable in its scope already
broadcast(inside);
