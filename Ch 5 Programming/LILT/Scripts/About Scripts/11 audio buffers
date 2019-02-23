/* (IZ 2005-09-03)
The class Samples provides tools for loading a list of audio files from disk to Buffers in RAM, for browsing and testing those Buffers, and for assigning those buffers to any Script for use by the "bufnum" parameter of this Script (the default parameter for playing a buffer).  Open the Buffers browser from the Scripts browser menu or by evaluating: 
	Samples.makeGui
This opens a gui for the Samples instance on the default server. You can create instances on other servers and their guis with: Samples(aServer).makeGui.

Use the load folder button to load a folder. Subfolders are not loaded automatically. Use the load file(s) button to load one or more audio files. 

Samples makes sure all its buffers are loaded: 
If the Samples's server is running, Samples loads the buffers immediately, otherwise it stores them in its list and loads them as soon as the server boots. If the server quits and reboots while there is a buffer list, Samples will reload all buffers. When loading a list of buffers, Samples checks to see if a Buffer with the same path already exists in its buffers list.  If yes, it will free that buffer before reloading it, so as to avoid loading multiple copies of the same buffer. 

Rhe present script contains a basic example of sample playback. The startPos parameter only works when the loop is off and the playback is triggered by restarting the script or by setting the trig parameter from 0 to 1. This script tests PlayBuf UGen functionality only.

Deleting buffers: 
Typing the delete key when the focus is on the buffer list view frees the selected buffer from the local server and deletes the buffer from the buffer list. 

Requiring buffers by code: 
aSamples.require(path) : load the buffers matching path only if not already loaded. Return array of buffer-name-symbols and buffers. Useful for making sure that a buffer is loaded. Example: 

// Load all audio files under the users home folder ~sounds whose name starts with "bird":
aSamples.require("~/sounds/bird*");
// Example result (buffer info has not yet been updated at method return time):

[ [ birds1, Buffer(52, nil, nil, nil, /Users/coacoanuttt/sounds/birds1.WAV) ], [ birds2, Buffer(51, nil, nil, nil, /Users/coacoanuttt/sounds/birds2.WAV) ] ]

Getting the Samples of the default server:
Samples.default // return the sample list for Server.default. Example:
// load all samples under "~/sounds" to the default server, if not already loaded:
*///Samples.default.require("~/sounds/*"); 
/*
Shortcut for access of buffer numbers on default server Samples: 
Samples[bufname] returns the bufnum of the buffer named bufname from Samples.default. 

Keyboard commands on the buffer list view:
delete = free selected buffer and delete it from the list
return = play selected buffer
escape = stop any playing buffer synths that were started by return
enter  = post path of selected buffer to message window

*/

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'bufnum', 0, 1023, 'linear', 1, 0 ],
[ 'rate', 0.125, 8, 'exp', 0, 1 ],
[ 'trigger', 0, 1, 'linear', 1, 1 ],
[ 'startPos', 0, 1, 'linear', 0, 0 ],
[ 'loop', 0, 1, 'linear', 1, 1 ],
[ 'amp', 0, 1, 'amp', 0, 1 ]
];

~start = { Synth("playbuf_looping", ['out', ~out, 'bufnum', ~bufnum, 'rate', ~rate, 'trigger', ~trigger, 'startPos', ~startPos, 'loop', ~loop, 'amp', ~amp]) };
