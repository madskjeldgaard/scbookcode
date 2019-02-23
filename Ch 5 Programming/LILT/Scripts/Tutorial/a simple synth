/* (IZ 2005-08-20)
The simplest way to make a Script create Synth when it starts is to define it in the Script's start function. The synth that is returned by this start function will be stored in the ~process variable of the script's event environment and in the process instance variable and will be freed by the Script's stop function: 
*/

~start = { Synth("variable_sin") };

/* To create a test instance of this script and its gui, click on the name of the present Script in the Scripts list of the Scripts browser in order to select this script, and then type "t" (= test) or the enter key (= create new Script instance). This opens a gui with a the start/stop button that bears the scripts's name. Click on the start/stop button to switch the script on and off.  Note that every Script that is created is automatically stored in the current Script group of the current Session, so that its Gui can be opened again from that group (see Sessions).

Note: Do not evaluate the above statement by pressing the "enter" key. Code written in the Script Browser is evaluated in its entirety when the script is created. This code only makes sense within the context of the entire Script instance that it belongs to.  To open a SuperCollider Document window with a Script's code, select the Script in the Script-list of the browser and type "e". 

Basic keyboard shortcuts that can be used anywhere on the gui window are:

space = toggle starting or stop the script.
g     = show the control parameter gui part of the window
h     = hide the control parameter gui part of the window

The next section explains how to add gui controls for setting script parameters. 
*/
