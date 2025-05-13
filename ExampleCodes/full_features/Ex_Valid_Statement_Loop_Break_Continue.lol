broadcast("Enter a blank line to exit...");

wave(faker){
  item response: message = chat("What is your name? ");
  
  canwin(response != "") next;
  clear;
}

broadcast("Exiting...");