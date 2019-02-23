/* (IZ 2005-09-05)
A. LIST OF CLASSES BY FOLDER

Script Lib: BuffArray, Samples, Script, ScriptBrowser, ScriptPatch, ServerWatcher, SynthDefs, LinkedBus
MIDI: MIDIConfig, MIDIListener, NoteOnOffResponder
Utils+Experimental: BufSynth, FX, Metro, SimpleServerWatcher, Warn

B. SUMMARY OF CLASSES BY FOLDER (Classes in alphabetic order)

=== Script Lib ===

BufArray: Holds an array of buffers of equal size for ready use by Synths. Can pass buffers to a Synth at creation and automatically de-allocate the buffers for re-use when the synth is freed. 

Samples: Loads audio files from disk to RAM. Creates gui for loading folders and files, viewing, playing and deleting loaded buffers, and assigning buffers to scripts by drag-and-drop. Reloads the current list of samples to the server when it boots. 

Script: Runs any SC code that needs to run in a predefined or interactively controlled time interval between a start  and a stop time-point. Utilities for creating control guis, linking inputs and outputs between scripts, control of parameters via MIDI and OSC, and some more. 

ScriptBrowser: Browser for loading folders containing scripts, editing, testing and creating script instances, etc. 

ScriptPatch: (This class is still a draft). Creates window with [a list of loaded scripts with keyboard commands for starting, stopping, opening and closing a control gui and deleting] and an array of buttons for starting, stopping and linking scripts, and menu for saving or loading its current state to/from disk. 

ServerWatcher: Notifies registered objects when a server boots or quits. Uses comparison of data in a buffer to prevent notification when the computer wakes up from sleep with the server running. Separate notification of boot objects and quit objects. 

SynthDefs: Loads SynthDefs from a folder of files. Creates gui for browsing, testing, editing and automatic generation of Scripts from SynthDefs. 

LinkedBus: A Bus with registration of the processes that write into it and read from it. Used for linking of I/O of Synth processes between Scripts. 

=== MIDI ===

MIDIConfig: Very simple configurable MIDI librarian. Builds a simple custom gui of MIDI controllers. A "learn" method waits for MIDI input and assigns each next input coming from different controller (different channel or number, but not different value/velocity) to the next controller gui item. Saves and loads MIDI configurations. Assigns MIDIResponders to a Script or its Parameters. 

MIDIListener: The class that does the listening for MIDIConfig. Creates a MIDIResponder when a MIDI message is received that is different in channel or number from the previous one. Presents a gui for editing all parameters of the Responder created, including its action function. 

NoteOnOffResponder: Holds a NoteOnResponder and NoteOff responder, for assignment to a midi control item that sends each of these alternately. 

=== Utils+ Experimental ===

Metro: 

FX:

SimpleServerWatcher: Older version of ServerWatcher without the "signature buffer" test. Waking up the computer from sleep when the server is already running will cause this watcher to notify its boot objects.

*/

