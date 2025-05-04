// all the samples
item binary: stats = 0b1001010; 
item octal: stats = 0e1765210;
item decimal: stats = 1234512;
item hexadecimal: stats = 0x123ABCF;
item float: stats = 1234.123456;

// simple operations for testing
item smallDecimal : stats = 1;
item smallHexadecimal : stats = 0xF;

// this should be 16, cause it's 15 + 1
smallDecimal = smallDecimal + smallHexadecimal;

<<<<<<< HEAD:ExampleCodes/Ex_Valid_NumberTypes.lol
<<<<<<< HEAD:ExampleCodes/Ex_Valid_NumberTypes.lol
broadcast(smallDecimal);
=======
broadcast(smallDecimal);
>>>>>>> b89331d3fe0e9f540d05520ac66890a31266d0f5:ExampleCodes/Demo_NumberTypes.lol
=======
broadcast(smallDecimal);
>>>>>>> b89331d3fe0e9f540d05520ac66890a31266d0f5:ExampleCodes/Demo_NumberTypes.lol
