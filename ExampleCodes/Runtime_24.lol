item botlane_duo: message[] = ["Ashe", "Blitzcrank"];

channel(botlane_duo) {
    teleport("Ashe"): {
        broadcast("Support selected!");
        cancel;
    }
    recall: {
        broadcast("Fallback champion!");
    }
}
