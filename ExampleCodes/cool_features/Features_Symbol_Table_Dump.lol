item x: message = "This is the at depth 0 (root)";

{
  item x: message = "This is the at depth 1";

  {
    item x: message = "This is the at depth 2, child 0";
    broadcast(x);
    dump_symbol_table(); // print here
  }

  broadcast(x);

  {
    item x: message = "This is the at depth 2, child 1";
    broadcast(x);
  }
}

broadcast(x);