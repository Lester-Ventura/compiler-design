build LCKPlayer {
  name: message;
  getPentas: skill () -> stats;
  setPentas: skill (stats) -> passive;
}

item createLCKPlayer: skill (message, stats) -> build LCKPlayer =
  skill (item name: message, item _pentas: stats): build LCKPlayer -> {
    item pentas: stats = _pentas;
    
    recast {
      name: name,
      getPentas: skill (): stats -> {
        recast pentas;
      },
      setPentas: skill (item newPentas: stats): passive -> {
        pentas = newPentas;
      }
    };
  };

item player: build LCKPlayer = createLCKPlayer("Bob", 1);
broadcast(player.name);
player.setPentas(2);
broadcast(player.getPentas());