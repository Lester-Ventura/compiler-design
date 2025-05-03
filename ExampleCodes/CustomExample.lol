build Player {
    name: message;
    alias: message;
    careerLength: stats;
    status: goat;
}

item Faker: build Player = {
  name: "Lee Sang-hyeok",
  alias: "Faker",
  careerLength: 12,
  status: faker
};

broadcast(Faker.name);