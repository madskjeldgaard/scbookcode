
	// figure 8.19 - a fixed serialization method.

serialize {|index, pos, extent = 8|
	var ranges, slice;

	// get ranges
	ranges = pos.collect{|pos, i| 
		((pos-(extent*0.5))..(pos+(extent*0.5)-1)) % this.shape[i] 
	};
	// get sub-slice
	slice = this.slice(index, *ranges);
	// trivial serialization of multidim. slice
	^slice.flat
}


