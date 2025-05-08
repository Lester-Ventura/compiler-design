steal "./Demo_Arrays.lol";

testing();

item factorial: skill (stats) -> stats = skill (item x: stats): stats -> {
  canwin(x > 1){
    recast (x) * factorial(x - 1);
  }lose{
    dump_call_stack();
    recast 1;
  }
};

broadcast(factorial(5));