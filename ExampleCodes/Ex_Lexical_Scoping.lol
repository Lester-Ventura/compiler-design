// This defines a function called `split_string` in the global scope
item split_string = skill (
  item input: message, 
  item character: message
): message[] -> {
  // `input`, `character` are defined inside this block through the parameter list

  // `ret`, `current` are also defined in this block through these parameter declaration
  item ret: message[] = [];
  item current: message = "";


  cannon(item i = 0; i < input.length(); i++){
    // `ch` and `i` are defined in this block due to the for loop initialization and this variable declaration
    int ch: message = input[i];


    canwin(ch == "character"){
      ret.push(current);
      current = "";
    } lose current = current + ch;
  }

  ret.push(current);
  return ret;
}
