// 8. Non-existent fields being accessed on dot-gettable types
steal "Demo_Object_Record.lol"; // object is defined here

rune Rengar : build Champion = {
    name: "Kitty",
    health: 1500,
    mana: 400,
    alive: faker
};

broadcast(Rengar.skin);
