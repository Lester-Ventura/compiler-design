item playerPosition: stats = 1; // Assume 1 is top lane

channel(playerPosition) {
    teleport(2): { // Case 2 - Mid lane
        broadcast("Welcome to Mid lane!");
        flash 2; // goto 2 is a valid statement here
    }

    teleport(3): { // Case 3 - Bot lane
        broadcast("Bot lane coming through!");
        flash 3; // Going to an invalid case (Case 4 doesn't exist)
    }

    recall: {
        broadcast("Recall initiated.");
        flash 4; // Error! Case 4 doesn't exist.
    }
}