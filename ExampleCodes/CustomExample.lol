item array : stats[] = [1, 2, 3];

item performArrayOperation: skill (message, stats) -> passive = 
  skill (item testing: message, item kills: stats): passive -> {
    broadcast("Hello, World! " + testing);
    broadcast(array[0]);
  } 

performArrayOperation("this is just nice to know", 10);
