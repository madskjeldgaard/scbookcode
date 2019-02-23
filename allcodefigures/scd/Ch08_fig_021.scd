

// figure 8.21 — some initial serialization methods, and adding an alternative. 

	*initClass {|numDims = 4|
		...
		// slice here is a 4d hypercube of extent <extent>
		serTypes = (
			hilbert: {|slice, extent|
				extent.isPowerOfTwo.not.if({
					"QCD:serialize: extent has to be a power of two".error
				});
				HilbertIndices.serialize(slice)
			},
			torus: {|slice, extent|
				slice.flat;
			},
			scramble: {|slice|
				slice.flat.scramble;
			}
		);
	}

	// add a new serialization type at runtime
QCD.serTypes.put(\star, {|slice|
	var starSize = slice.size div:2;
	var numDims = 4;
	var starShape;

	starShape = neighbours1.collect({ |nb|  
		(0..starSize).collect(_ * nb) 
	}).flatten(1).collect {|indexN| 
		indexN + (starSize.div(2)+1).dup(numDims) 
	};
	
	starShape.collect{|iA| slice.slice(*iA)}
});


