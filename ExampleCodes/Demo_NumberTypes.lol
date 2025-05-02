// all the samples
item binary: stats = 0b1001010; 
item octal: stats = 0e1765210;
item decimal: stats = 1234512;
item hexadecimal: stats = 0x123ABCF;
item float: stats = 1234.123456;

// simple operations for testing
item smallDecimal : stats = 1;
item smallHexadecimal : stats = 0xF;
smallDecimal = smallDecimal + smallHexadecimal;

canwin(smallDecimal > 15) {
    broadcast("it works");
}