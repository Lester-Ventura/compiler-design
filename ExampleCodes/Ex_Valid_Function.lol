item split_string: skill (message, message) -> message[] = 
  skill (item input: message, item character: message): message[] -> {
    item ret: message[] = [];
    item current: message = "";

    cannon (item i: stats = 0; i < input.length(); i = i + 1) {
      item ch: message = input[i];

      canwin (ch == character) {
        ret.push(current);
        current = "";
      } lose {
        current = current + ch;
      }
    }

    ret.push(current);
    recast ret;
  };

broadcast(split_string("hello", "world"));