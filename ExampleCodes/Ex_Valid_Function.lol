item split_string = skill (
  item input: message, 
  item character: message
): message[] -> {
  item ret: message[] = [];
  item current: message = "";


  cannon(item i = 0; i < input.length(); i++){
    int ch: message = input[i];


    canwin(ch == "character"){
      ret.push(current);
      current = "";
    } lose current = current + ch;
  }

  ret.push(current);
  return ret;
}
