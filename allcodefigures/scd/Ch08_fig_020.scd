
	// figure 8.20 - flexible  serialization by lookup.

serialize {|index, pos, extent = 8, how = \hilbert|
	// [...]
	slice = this.slice(index, *ranges);
	// call function in serTypes dictionary
	^serTypes[how].(slice.asArray, extent)
}



