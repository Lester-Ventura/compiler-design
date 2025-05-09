// 19. Non-boolean if-statement branch conditions
// This can be disabled using the `--lenient` flag

// This would not work if the lenient flag is not enabled
canwin(1) {
    broadcast(1);
}

// "faker" is a truthy value
canwin("faker") {
    broadcast("This guy is faker");
} lose {
    broadcast("shaker :madge:");
}
