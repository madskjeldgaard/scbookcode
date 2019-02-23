RBE_SDIF_File : File {

	*new {|path| ^super.new(path, "rb");}
	
	readFramesToPartials {
		var size, string, streamID, numMatrices, matrixName, time; 
		var matrixDataType, numRows, numColumns, getItemSelector;
		var partialIndex, dict, list;
		//var oldKeys, keys;
		dict = IdentityDictionary.new;
		this.seek(0, 0); // go to beginning and check for SDIF TypeID
		string = "";
		4.do({string = string ++ this.getChar});
		if(string != "SDIF", {"Not a SDIF File".warn; ^nil});
		
		// skip past opening header
		size = this.getInt32;
		("SDIF Spec Version:" + this.getInt32).postln;
		("SDIF Standard Types Version:" + this.getInt32).postln;
		//this.seek(size, 1); // go past file header
		
		// check for RBEP frames and parse if found
		while({(string = this.getChar).notNil},
		{
			//keys = Set.new;
			3.do({string = string ++ this.getChar}); // frame type
			if(string == "RBEP", {
				//"RBEP Frame Found".postln;
				size = this.getInt32; // num of bytes in frame
				time = this.getDouble;
				streamID = this.getInt32;
				
				// check that this matches the expect spec for an RBEP Frame
				numMatrices = this.getInt32; // in theory this should be 1;
				if(numMatrices != 1, {
					"Unusual number of matrices for a RBEP Frame. Aborting".warn;
					^nil;
				});
					// matrix header starts here
				matrixName = this.nextN(4); // should be RBEP
				if(matrixName != "RBEP", {
					"Non RBEP matrix in RBEP Frame. Aborting".warn;
					^nil;
				});
				
				// get matrix data type
				// should be 0x4 (32 bit big endian float) or 0x8 (64 bit big endian float)
				matrixDataType = this.getInt32; 
				getItemSelector = switch(matrixDataType,
					0x4, {'getFloat'},
					0x8, {'getDouble'}
				);
				if(getItemSelector.isNil, {
					"Unknown matrix data type. Aborting".warn;
					^nil;
				});
				numRows = this.getInt32;
				numColumns = this.getInt32;
				// partialIndex, freq, amp, phase, noise, timeOffset
				numRows.do({
					// get the data, 32 or 64 bit
					partialIndex = this.perform(getItemSelector);
					//keys.add(partialIndex);
					if(dict[partialIndex].isNil, {dict[partialIndex] = List.new});
					// time freq amp phase bw
					dict[partialIndex].add(Array.fill(4, {this.perform(getItemSelector)})
						.add(this.perform(getItemSelector) + time).rotate(1));
				});
				
				// it's possible that there would be padding bytes here, 
				// but for the moment that doesn't seem to be the case
				//(keys == oldKeys).postln;
				//oldKeys = keys;
			},
			{
				// ignore other types for now, including RBEL and RBEM; just bypass the frame
				(string + "frame found. Skipping...").postln;
				size = this.getInt32;
				this.seek(size, 1); 
			});
		
		});
		
		list = dict.values.collectAs({|item| // a list of Lists, one for each partial
			var oldTime = 0.0;
			item = item.flop;
			// start-time, iphase, times, amps, freqs, bw
			
			[item.first.first, item[3].first, item[0].differentiate.drop(1), item[2], item[1], 
				item[4]];
		}, Array);
		^list;
	}

}