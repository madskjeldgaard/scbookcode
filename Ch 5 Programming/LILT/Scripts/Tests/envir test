/* IZ 2007-02-07 { SC3
Demonstrates the saving and loading of script-specific data in the Session-creation file. 
} */

var timeStamp;

~start = {
	// here post any data saved for this file from a previous Session save: 
	~scriptData.at(~script.scriptGroup.name, ~script.name).postln;
	timeStamp = thisThread.seconds;
};

~stop = {
	// create some data to save
	~data = [timeStamp, thisThread.seconds, thisThread.seconds - timeStamp];
};

~saveData = { | envir, file |
	// save the created data
	file putAll: envir.data.asCompileString;
};

