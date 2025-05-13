// Record creation patterned after
build Player {
	name: message;
	alias: message;
	careerLength: stats;
	status: goat;
}

// assigning the object literal
item Faker: build Player = {
  name: "Lee Sang-hyeok",
  alias: "Faker",
  careerLength: 12,
  status: faker
};

// dot access to use one of the object's values
broadcast(Faker.name);