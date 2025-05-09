// This is a demonstration of First order functions in LoLang
// Functions in LoLang are expressions, meaning they can be used wherever an expression is expected

// In this specific example, we are creating a function that returns another function
item addGenerator: skill (stats) -> skill (stats) -> stats = 
  skill (item a: stats): skill (stats) -> stats -> {
    recast skill (item b: stats): stats -> {
      recast a + b;
    };
  };

item plus5: skill (stats) -> stats = addGenerator(5);
broadcast(plus5(10));