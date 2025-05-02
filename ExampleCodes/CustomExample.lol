// simple operations for testing
item smallDecimal : stats = 1;
item smallHexadecimal : stats = 0xF;
smallDecimal = smallDecimal + smallHexadecimal;

canwin(smallDecimal > 15) {
    broadcast("it works");
}