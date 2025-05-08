item another_testing: skill () -> passive = skill (): passive -> {
    dump_call_stack();
};

item testing: skill () -> passive = skill (): passive -> {
    another_testing();
    broadcast("this is running inside demo_arrays.lol, testing");
};