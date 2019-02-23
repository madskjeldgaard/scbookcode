/*
	IZ 040825 Utilities for finding the largest and smallest element in a collection
*/

+ Collection {
	largest {
		var largest;
		largest = this.detect { true };	// get first element even for a Set
		this.do {|a| largest = if (a > largest ) { a } { largest }};
		^largest;
		}
	smallest {
		var smallest;
		smallest = this.detect { true };	// get first element even for a Set
		this.do {|a| smallest = if (a < smallest ) { a } { smallest }};
		^smallest;
	}

}

