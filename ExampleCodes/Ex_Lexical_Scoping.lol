// This should be rechecked once we get standard library stuff
// uses array.push()

// This defines a function called `split_string` in the global scope
item split_string: skill(message, message) -> message[] = 
skill (
  item input: message, 
  item character: message
): message[] -> {
  // `input`, `character` are defined inside this block through the parameter list

  // `ret`, `current` are also defined in this block through these parameter declaration
  item ret: message[] = [];
  item current: message = "";

  cannon(item i: stats = 0; i < input.length(); i = i + 1) {
    // `ch` and `i` are defined in this block due to the for loop initialization and this variable declaration
    item ch: message = input[i];

    canwin(ch == character) {
      ret.push(current);
      current = "";
    } lose {
      current = current + ch;
    }
  }

  ret.push(current);
  recast ret;
};

