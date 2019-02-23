/* (IZ 2005-09-03)
The class SynthDefs loads on the  Local Server all SynthDefs defined by SC code in text files located in subfolders of the folder /Lilt/SynthDefs/ at startup. A window for browsing all SynthDefs thus loaded can be opened from the Browsers window or by executing the code: 

	SynthDefs.makeGui

The leftmost list is a list of folders. The right list (Def-List) is a list of SynthDefs defined by files in this folder.  The right text panel shows the controlSpecs arguments that correspond to the arguments of the selected SynthDefs UGen function. Following keyboard commands are available when a SynthDef is selected in the Def-List: 
e = Edit the file of the selected SynthDef.
r = Reload the selected SynthDef from file. 
t = Test the SynthDef by creating a synth with the default arugment values.
s = Create a Script text that will start the selected SynthDef and provide controlSpecs for the  arguments of the SynthDef function. The Script is placed in the currently selected folder of the Scripts browser. In order to run this Script you must first save it onto file. 

The blue drag-source view is for dragging a SynthDef to the instrument parameter of a pattern script in order to change the instrument which this pattern uses for playing. (Not yet implemented!).

Class methods for loading folders: 

*openFolderDialog
Create dialog window for selecting a folder, load the synthdefs in this folder and its subfolders. 

*loadFolder(path, loadSubfolders = true);
Load all synthdefs in files under the folder given in path. If loadSubfolders is true, then also load files in subfolders of the folder. 
*/

