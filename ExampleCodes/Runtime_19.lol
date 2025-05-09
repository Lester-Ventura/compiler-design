cannon(item level: stats = 0; level < 10; level++) {
    canwin(level < 5) {
        broadcast("\nClearing jungle...");
    }
    lose {
        broadcast("\nBack to base!");
    }
}

next;
