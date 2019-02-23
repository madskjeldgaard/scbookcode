/* (SC3)
----- Done 070125: 
- Complete implementation of link list window for Script.
- Add keyboard commands to link list: 
	- backspace: remove link
	- return: start linked script
	- escape: stop linked script
	- shift return: show GUI of linked script
	- shift escape: hide GUI of linked script

	- Snapshots of parameter settings

----- 070206
Added to the Session-as-Script saving mechanism one more step that calls for each script the function stored in environment variable "saveData". Any script that defines that variable can write any additional data to the session script at that point. This is just before the final include statement at the end of the Session Script file.  The call to the saveData function is:
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



All arrays of the above form are put in the environment variable ~scriptData of the environment of the Script that created the Session where the present Script resides. The Script that created the Session is accessible to the present Script through: 
~script.session.script;
so its environment is: ~script.session.script.envir;

*/
