/* IZ 2007-02-07 { SC3
	 saving mechanism one more step that calls for each script the function stored in environment variable "saveData". Any script that defines that variable can write any additional data to the session script at that point. This is just before the final include statement at the end of the Session Script file.  The call to the saveData function is:
		script.envir.saveData(file); 
	So the function stored in ~saveData will have the form: 
		{ | envir, file |
			... 
		}
	So it can access the data from the Script's environment and write them for example as follows: 
		{ | envir, file |
			var data;
			data = envir.data;
			// Following can vary according to structure of the data, but it must 
			// be a valid syntax creating either a single object or an array of objects
			// separated by commata:
			data do: file.putAll(_);
			// examples of acceptable output would be: 
			// 1234.5678 // a single item
			// [\bw, 0.1, ControlSpec(0.01, 0.5)] // an array of data
			// 1, 2, \x, (0, -0.1 .. -1) // items separated by commata
		}

	The method Script:saveData provides the appropriate opening and closing elements to the string written by envir.data so that an array of the following form is created: 

	[<script group name>, <script name>, <data item 1>, <data item 2> ... etc]

All arrays of the above form are put in the environment variable ~scriptData of the environment of the Script that created the Session where the present Script resides. This environment is the parent event of every script belonging to that Session. Therefore, every Script's environment has access to the environment of the Script of the Session that created it.

Here is an example of a Script that creates an arrray of 3 numbers when it is stopped, and stores is in its environment variable ~data. It writes these data in the Session it belongs to and can restore it when that Session is loaded.

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
};*

~saveData = { | envir, file |
	// save the created data
	file putString: envir.data.asCompileString;
};
