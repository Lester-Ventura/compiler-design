item numbers: stats[] = [1, 6, 123, 8, 1, 9, 6456, 90];
item sortedNumbers: stats[] = numbers.toSorted(skill (item a: stats, item b: stats): stats -> {
  canwin(a > b) recast 1;
  canwin(a < b) recast -1;
  recast 0;
});

broadcast("\nSorted numbers:");
broadcast(sortedNumbers);