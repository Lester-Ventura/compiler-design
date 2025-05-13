item factorial: skill (stats) -> stats = 
  skill (item n: stats): stats -> {
    canwin(n >= 2) recast n * factorial(n - 1);
    
    // only print the stack in the base case
    dump_call_stack();
    recast 1;
  };

broadcast(factorial(5));