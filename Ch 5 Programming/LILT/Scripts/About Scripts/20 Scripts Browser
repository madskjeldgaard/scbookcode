/* (IZ 2005-08-14)
The Scripts Browser is a window that serves to browse, select, edit, test and create instances of Scripts. One can also use it to store and evaluate or browse any useful piece of code or documentation. At startup, the ScriptBrowser class loads all subfolders of the folder SCClassLibrary/LiltHZ/Scripts and the paths of the files contained in them into the ScriptBrowser class variable "all". Methods of the ScriptBrowser class as well as associated kebyoard commands of the Scripts Browser permit to either create instances of Scripts from any file path registered in the "all" class variable, or to just evaluate that file directly. 

To open a Scripts Browser, evaluate this code: 

	ScriptBrowser.makeGui;

This will open a window called "Scripts". The lefmost pane of this window lists all subfolders of the Scripts folder ("folders list") while the list view to its right ("script list") shows the files contained in the currently selected folder without their last extension.  Selecting a script file from the script list will display the contents of the file in the script view, located at the right part of the window.  The script text can be edited in the script view. To save the edited text, press the enter key. 

The items of the menu on the top left part of Scripts Browser menu are the following: 
"Load Folder ..."          Open a file dialog for loading all subfolders of 
                           any folder to the Script Browser.
"Load Scripts ..."         Open a file dialog for loading any file path
                           to the Script Browser (multiple path selections are possible
                           on one dialog window).
"Save Script as ..."       Save the currently selected script under a new file.
"Revert to saved version"  Discard all edits made to the script and revert its 
                           contents to the most recently saved version on disk.
"New Patch"                Open a new Script Patch window that displays 
                           Script instances created from the Script Browser, allowing 
                           starting, stopping, configuration, control of the instances.
"Open Patch ... "          Reload a configuration of Scripts saved from a previous
                           Script Patch window on disc (see Script Patch).
"Reload top folder"        Reload the last folder that was loaded from the Load Folder
                           menu item or by Scripts.loadFolder(...)
                           This refreshes the contents of the browser lists, if any
                           changes were made in the folder or file names. 
"Open SynthDefs Browser"   Open the SynthDefs browser.
"Open Buffers Browser"     Open the Buffers browser.
"Open Globals Browser"     Open the Global Scripts browser.

The currently selected Script is shown also on the pink drag-source view on the top row of the window. To create a Script instance from the currently selected Script Drag, drag the selected script from the drag source view the drag-both view marked "@" to the right of any script button on a Script Patch window. The following commands are activated by typing a key while the focus is on the script list:

t    = Create a Script instance and its corresponding start/stop and control GUI.
       Closing the GUI will stop the test Script instance.
space = Create a Script instance and add it to the active Script Group of the Session.
       Open a gui for the Script Group if not already present. See '21 Script Groups'. 
T    = Evaluate the SC code of the file of the selected Script (do not create Script instance)
e    = Edit the selected Script on a separate SC document window.
g    = Create a Script instance and store it in the globalScripts class variable
     of Scripts under the name of the Script.
R    = Reload the top folder of scripts and all its subfolders. (BUG: Must repeat command to work)

BUGS: Reloading the top folder does not update the lists properly the first time. One must repeat the command 2 or 3 times for all script names of all subfolders to be updated in the lists.

*/

