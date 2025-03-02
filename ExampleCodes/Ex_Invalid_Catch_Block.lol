support {
	
} // a support statement without a carry statement would not work

support { // the support and carry must both have a starting and ending brace
carry (e) {}

carry (e) { /* a carry with no support would not work */ }

support {}
carry () {} // a carry with no parameters would also not work

support {} // a support can only have one carry
carry (e1) {}
carry (e2) {}
