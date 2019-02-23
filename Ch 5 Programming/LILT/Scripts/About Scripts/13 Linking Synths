/* (IZ 2005-09-03 - completed 2007-02-04) (SC3)

Note: To make a Script receive values from a Parameter of another Script whenever that Parameter changes in SCLang (client), see 14 Binding Parameters

Linking of a script ('source') to another script ('destination') makes the output of the 'source' script act as input or control to a specific parameter of the 'destination' script. For example, one may link the output of a 'source' script that produces an audio signal on a bus to the input parameter 'in' of a 'destination' script that processes the audio signal in order to add an effect such as reverberation, granulation, spatialization or the like.  One can also link the control-rate signal output by a 'source' script to any control parameter of a 'destination' script in order to map the control-rate bus where the 'source' script outputs to the input of the given control parameter of the 'destination' script.  Summary of ways for connecting and disconnecting Scripts: 

1. Linking scripts via drag-and-drop
Drag the drag-source view marked "o" at the right of a Script's main button to the label of any parameter from another script that accepts input of the same type as the output. Outputs and inputs are color coded as follows: 
	- Red: audio-rate
	- Blue: control-rate
Since both any input and any output may have more than one outputs or inputs linked to them respectively, to disconnect them, one needs to have a list of their links to select from. This is done with the keyboard commands described next: 

2. Keyboard commands
Typing 'l' on a Script's window opens a links-inspector for that script. The left pane of the inspector shows the links to inputs and the right pane shows the links from the output of the script. To remove a link, type the "backspace" key with on the currently selected link in a list-inspector pane. To start the script which this link is connecting to, type the return key. To start both the script to which this link connects and the script to which the inspector pane belongs, type shift-return. 

3. SuperCollider Code
To connect the output of a script w to the input of another script w write: 
	w.addReaderScript(r, <parameter name>)
Where <parameter name> is the name of the parameter to receive the output from w, and defaults to 'in'.
To remove the output of a script w from the input of another script w write: 
	w.removeReaderScript(r, <parameter name>)
Where <parameter name> is the name of the parameter to receive the output from w, and defaults to 'in'.

To and and remove links between Parameters themselves only the following 2 methods should be used: 
	anOutputParameter.addReader(aParameter)
	anOutputParameter.removeReader(aParameter)
These are the heads of the algorithms that allocate and deallocate busses and bus links and set the appropriate Server Groups of their Scripts. All other link-related methods are parts of those two algorithms and should not be used by themselves.  


*/
