steal "./Features_Import_Target.lol";

item adder: skill (stats) -> stats = addGenerator(5);
broadcast(adder(10));