/* (IZ 2005-08-15)

Lilt works with Scripts. A Script is an object that can run code stored in a file and provides facilities for interacting with the program that is contained in that code. These facilities include: 
- Creation of a GUI for starting and stopping a process created by the code
- Adding widgets for setting the values of parameters in the environment of the script and for automatically sending their values to synth processes created by the code. 
- Connecting the audio or control output of processes from one script to another
- Loading synthdefs and buffers
- Saving and reloading configurations of scripts including their guis, interconnections, presets, and midi controls

The present file is already a script. To create an instance of this script, select the present script in the ScriptBrowser and press the [Enter] key. This should open a gui with a button for starting and stopping the synth created by the script. 
*/

if (Server.default.serverRunning.not) { Server.default.boot };

~start = { Synth("variable_sin") };
