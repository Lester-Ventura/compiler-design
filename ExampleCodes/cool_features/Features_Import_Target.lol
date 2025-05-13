item addGenerator: skill (stats) -> skill (stats) -> stats = 
  skill (item a: stats): skill (stats) -> stats -> {
    recast skill (item b: stats): stats -> {
      recast a + b;
    };
  }