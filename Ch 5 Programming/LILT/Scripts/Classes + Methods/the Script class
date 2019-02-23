/* IZ 050705 ... UNDER DEVELOPMENT ...

The Script class creates objects that can be used to start, stop and control a script. A script is simply a piece of text, stored as a String, that contains SuperCollider code. Inside each instance of Script the script is stored in the instance variable 'script'. The Script instance creates a window with a button as a gui for starting and stopping the script, as well as optionally additional control for influencing the behavior of the script.

Environment variables ...

The script of a Script instance is evaluated immediately upon loading with
	envir.use(script.compile); 



To play a synth, the script must set the environment variable ~start 
with a function that returns one of the following objects: 
- Synth
- Pattern
- Function. The function is played as a Routine with the clock that is 
  stored in envir.clock. Otherwise default is SystemClock. 
- Event. The event is supplied defaultParentEnvir as parent and then played
  This works in a similar way as a Pbind except you provide the keys as 
  keys of the event. You must use \duration instead of \dur
  and if the keys hold patterns, you must make these into streams yourself.

The function to start the script is saved in ~start;
The function to stop the script is saved in ~stop (defaults to process.stopMessage)

The script may modify the environment (add environment variables or change 
values of fields) and it may use any environment variable in envir. 
A globalParentEnvir is used as parent for all environments of Score instances. 

Additionally Script provides menus a menu to the left of the button with 
commands for opening, saving and "automatic MIDI binding" (see below).

When one edits the script in a document window that was opened from the menu
command "edit", closing that document after saving will update the script. 
Otherwise, one must use the menu command "reload" to reload the script from
file on disk. 

Finally, there is a simple protocol for specifying GUI sliders that set environment 
variables and update synth parameters where appropriate. To do: 

Currently, whenevever there is a new midi binding, the MIDI settings only are 
automatically saved in a file config<timestamp> in the subfolder "config" 
located in the same folder as the script file. Where <timestamp> is the date and time
of the saving. 

=== Automatic MIDI binding ===

Automatic binding to MIDI functions by starting to listen to incoming MIDI messages
of a certain kind (for example note-on) until the next MIDI message of that kind arrives.
When such a message is received, the Script will record the channel and optionally the
key number of that message, and will start reacting to any other messages from that 
channel and with that key number to switch on-off. This will remain until another 
MIDI binding is chosen. The action performed when receiving a midi message from this
binding can be customized by storing a function in environment variable ~midiAction.

=== Control sliders ===

The environment variable ~parameters stores specs for creating an array of 
sliders in a separate window, which set the values of variables in the scripts
environment and also set synth parameters where appropriate. The spec format is: 
~parameters = [
	[name, min, max, warp, step, default, action],
	[name, min, max, warp, step, default, action],
	...
]

where: 
name is the name of the slider and the environment variable to be set, 
min, max warp, step, default are the specs for the ControlSpec that converts
the values 1-0 to an arbitrary specified range. 
action, if given, is the action performed with the value of the controller as follows:
	envir.use({ | val | action.value(val) })

=== To Do ===

Convert lastLoadedScripts into a dictionary, then: 
Add scriptLib dictonary var holding script groups in dictionaries with their configs

Implement "load group configs ... " for opening configs of entire group of Scripts

Add menu items: 
"save script config ..." save config of THIS script instance on separate file
"load script config ..." load config of THIS script instance from separate file

Add arguments chan, num to control item specification list for easy customizing of midi bindings

Make gui items reverse colors when waiting for midi

*/


