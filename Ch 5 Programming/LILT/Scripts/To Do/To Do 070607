/* IZ 2007-06-07 { SC3

-	Sessions should always activate MIDI when they open? 
	Session kbd cmd and / or menu items for activating / deactivating all MIDI? 
-	Create a "take init snapshot" menu item of sessions that takes a snapshot of 
	for each script and names it init. 
	If an init snapshot exists, it should be overwritten.
	Then when a session is opened, it will execute the init snapshot for all scripts if it exists. 
	Ideas for snapshot keyboard commands on script GUI: 
		- enter: take snapshot (already implemented)
		- shift-enter: take init snapshot
		- option-enter: take init snapshot for whole session and save it ... (!?)
-	Second alternative of above: 
	Shift-enter key on a script or script group creates an init snapshot for that script or script group.
	When a session is opened it loads those snapshots which are named init for each script that has one. 
-	Extended way of doing the above: Sessions should be able to save snapshots.
	Thoughts for implementation: 
	- create variable snapshots in Session
	- store snapshots as ListModel
	- Use similar gui as for Scripts for storing and editing session snapshot names in list
	- each snapshot in a session is an array of specs with script group name, script name, followed by 
		array in data format identical to the snapshot for the individual script. 
- 	snapshots of sessions (as well as scripts ?!) should be bindable to midi
-	make midi function for snapshots editable so you can address single parameters
- 	modify standard midi bindings to be sticky - start responding only when next param value is in the vicinity of current param value
-	how to save / load midi responder bindings for any object, with their action functions? 

-	070609: Create TargetParameter that stores an array of scripts (or other targets?) in the environment variable ~targets of a Script, so that the script can access them. To add a target, pop up a window with a list of scripts from the session (?). Such scripts can be useful for activating snapshots of another script by midi binding, for driving another parameter's script with a pattern, or other stuff. 

-	070614: ??? Redo ListModel so that the update messages go from the list inside the model, not from the ListModel. Reason: You may want to have several list models listening to the same list and updating their guis. ??? 
-	070622	Rather redo entire GUI building system. 
		To do this, start with a test case. See Script Classes/Drafts/GuiTest.sc

- snippets: Functions that run with a Script as target. A Script can have any number of snippets. The snippets are stored in a dictionary, each snippet under its own name. A Script will create a GUI for viewing and editing snippets: 
Left pane: List of snippets.
Right pane: the code of the selected snippet.
The code must return a function. 
The script will prepend to the code of the snippet the lines: 
	var script;
	script = ~script;
And will evaluate the code of the snippet within the Script's envir. This way, the variable 'script' will be bound to the script to which the snippet belongs. 
Additionally, define Script method 'routineSnippet', which will create a routine that runs the function given to routineSnippet as argument, and additionally defines variable 'isRunning' for showing status of routine and for preventing double-starting of the routine while it is already running.  

Snippets should be able to be bound to MIDI. 

} */

