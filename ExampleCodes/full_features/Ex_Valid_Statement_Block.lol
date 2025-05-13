item x: message = "This is the at depth 0 (root)";

{
  item x: message = "This is the at depth 1";

  {
    item x: message = "This is the at depth 2, child 0";
    broadcast(x);
  }

  broadcast(x);

  {
    item x: message = "This is the at depth 2, child 1";
    broadcast(x);
  }
}

broadcast(x);