/* (IZ 2005-08-21)

When a Script instance is loaded from file, it compiles the text of the file inside a new Event. This means that you can create any variables within that event by assigning to them:

~test = 0; // create event var. 'test' and assign 0 to it. 

You can write data or algorithms of arbitrary form and complexity and store them in any variables in the script's event environment. To use these by "playing" the script, you need to provide values for one or more "reserved" variables which define the behavior of the script. These are (in order of importance): 

~start : 
Function that is evaluated to start the script. Must return one of: Synth, Group, Pattern, Event, Function. 
- If a Synth or Group is returned, then it will continue to run until the Script is stopped.  
- Functions are executed inside a routine the script constructs and then plays. So functions can be treated as the main body of a routine's Function that one wants to play with the script. 
- Patterns are played. Besides Pbind and Ppar, any type of Pattern such as Pseq can also be played by itself (see examples in patterns&routines). 
- Events are played. Any value of the event can be a stream, with the exception of the value of \dur. 

~controlSpecs : 
Array of arrays of specs. Each spec has the form: 
	['name', min, max, warp, step, default, (optional: action)]
These specs create parameters that can be set via sliders of an automatically generated control-gui or that can be bound to outputs from other scripts (ar or kr signals or discrete values coming from patterns (!)) or, if appropriate, to an audio buffer. The parameters are used as arguments to the Synth or Group if appropriate, and can be changed interactively by the gui at any time. 

~process : 
This is the process that is playing when a script is started and as long as it is running. It can be one of : Synth, Group, EventStreamPlayer, PatternPlayer, Event, Routine. It is set by the ~start function. When the script stops, it sets ~process to nil. 

~stopMessage : 
This is the message sent to the ~process object in order to stop it. The normal message is \stop. One can customize it, for example, to stop a synth by releasing an envelope through a gate parameter (See for example Script playful_sounds/martianwinter): 

~stopMessage = [\set, \gate, 0]; // send to ~process the message \set with arguments \gate, 0.

~stop :
The function stored here evaluated when the script receives the message 'stop'. A default function is provided for the ~stop variable, which works for most cases. It may be set by the user to further customize the stop behavior in special cases. 

~script: 
Stores the script itself for access from within the script. One example is the message 'listenTo' which makes the script create special start and stop functions that make the script react to update messages sent from another object. (see elsewhere in doc). 

Last but not least, all environments of all scripts share as parent a single ocpy of the default parent event created at startup as follows: 

globalParentEnvir = Event.parentEvents[\default].copy;

This means the following things: 

1. Patterns and Events can be played from a Script by using the defaults defined by Event.parentEvents[\default].  Modifications made to a Script's event by writing on it will not affect the shared parent-event. 

2. The globalParentEnvir can store any objects that need to be globally available to all Scripts. For example, one can store global "effects" scripts there.  There are OSC commands for loading, interconnecting, and controlling global scripts, so one can also use scripts remotely from an other program via OSC. 

*/
