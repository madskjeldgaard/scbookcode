/* (IZ 2005-10-16) { 
} */

+ Collection {
	makeUniqueName { | name |
		// if name exists in me, add numbered suffix, counting up
		// till unique name is constructed.
		// all elements of the receiver must be Symbols for this to work
		var newName, count = 1;
		name = name.asSymbol;
		newName = name;
		while { this.includes(newName) } {
			count = count + 1;
			newName = (name ++ "[" ++ count.asString ++ "]").asSymbol;
		};
		^newName;
	}
}
