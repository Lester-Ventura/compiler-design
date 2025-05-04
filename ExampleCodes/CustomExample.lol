item numbers: stats[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
broadcast(numbers.push(11));
numbers.push(12);

broadcast(numbers.pop());

broadcast(numbers.filter(skill (item num: stats): goat -> {
  recast num > 5;
}));

item reversed: stats[] = numbers.toSorted(skill (item a: message, item b: stats): stats -> {
  canwin(a < b){
    recast 1;
  }lose{
    recast -1;
  }
});

broadcast(reversed);