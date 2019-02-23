/* (IZ 2007-01-17) {

(Underway: Rewrite of MIDIListener to simplify and generalize functionality)

- Complete saving and loading of sessions:
	- midi-settings of scripts

- ScriptBrowser:
	- Complete and improve menu
	- Script drag view should create script linked to any parameter to whose view it is dragged. 

The SynthDefs and Samples browsers should be rewritten. Possibly just as Scripts that load items from files. These scripts can then be included as parts of a session so that a session knows the synthdefs and the samples that it needs.

- Add Load Sound File / Load Folder items to the Session menu so that each Session remembers its own set of sound samples instead of all the samples that are loaded in its server.

- Redo the SynthDefs class to remove bug: Recompiling SC does not immediately load the SynthDefs that were newly put in the folder SynthDefs. Also a menu should be added to load other SynthDef folders. 

- Possibly all of links, Scripts, MIDI-bindings, snapshots and Script-data could be saved under a single library in the environment of the Session's script and therefore be uniformly available to all Scripts of a Session.

- Rewrite basics of Script class creation so as to use a ScriptPrototype that holds those parts of the scripts of the same type that are shared, namely: 
1. The path
2. The browser-path (folder and file name without extension)
3. The text (string) of the script itself. 
4. I/O characteristics of the script (has ar input, has kr/ar output, has buffer etc)
5. Possibly also pre-store at least part of the Parameters, such as their specs, as well as dimensions of GUI etc and other stuff that remains constant with all script instances generated from the same script text.
Note: Given that a session would usually have less than 50 scripts, and that the number of scripts of the same script text would normally be limited to far below this, the following idea for ScriptPrototype looks like overkill. But besides making things a little more efficient it also takes out some of the code from the Script class, thereby reducing it from the overblown situation it is now. See ScriptPrototype class draft for more details. 

- After implementing the ScriptPrototype class, Scripts can be made to send the required synthdefs to the server at session server boot time, instead of loading all SynthDefs in the SynthDefs folder at compile time. That way the synthdefs directory in SC3 can remain untouched by Lilt, and only those SynthDefs required by the scripts of the Session(s) loaded will be sent to the Server of that Session.  

- SynthDefs can be made to create Script instances and their GUIs directly without having to put them in the ScriptBrowser and save them first. ControlSpecs that differ from the defaults created from the SynthDef's Function's arguments could be stored as the value of an Association whose key is the SynthDef name and which is stored in an environment as a class variable of SynthDefs. 

- Make Sessions that share a server also share its Groups, so that not each Session that opens on the same server creates its own set of Groups. (??? Doubtful if this is a good idea)

- Work on methods for saving sequences of parameter settings and automating playback with routines and / or envelopes mapped to their control parameters

- Envelope editor for control envelopes of parameters as tracks - Extend Lance Putnam's envelope editor class - http://www.uweb.ucsb.edu/~ljputnam/sc3.html

- also time-line object editor? -- see also SCSoundFileView.

- Interpolation of parameters 

- Rewrite window creation code in all classes using WindowManager, to remove window variables and make the code shorter. 




================ OLDER TO-DOS

050904
// ============== Following todos are for Script: ===============
//TODO: MAKE this.changed calls and {}.defer consistent in methods that use these
//TODO: See how patterns can be linked - use a method similar to existing 'listenTo' to 
// create custom start functions for patterns that and also set the scripts type accordingly?
//TODO: implement removeInput
//TODO: check consistency of synth and node structure when adding / removing inputs
//TODO: implement fx scripts that will run an fx groups grouping several fx synths 
// to which several synths can write, so that 1 synth can send its output to many synths
// at once. This is useful for sending control signals of one synth to several synths. 
//TODO: Script: Add ~buffers ~inputs and ~outputs specs which create their own kind of GUI (not together with controlSpecs). ~buffers: menu of available buffers, inputs: appropriate drag view, and perhaps also menu (!) etc.


 */

