/* IZ 2007-02-02 { SC3

The format for saving and loading MIDI specifications for a Script is as follows: 

[<script group>, <script>
	[<parameter name>, <responder>],
	[<parameter name>, <responder>],
	[<parameter name>, <responder>],
	...
]

Where: 

<parameter name> is a symbol corresponding to the name of a Parameter of the Script. The Parameter is looked up in the parameterEnvir variable of the Script. If the Parameter is named 'script', then the script itself is returned. 

<responder> is an instance of some kind of MIDIResponder , saved as the code that will create an that instance. Cases: 

NoteOnResponder(nil, <src>, <chan>, <num>, <veloc>, false);
NoteOffResponder(nil, <src>, <chan>, <num>, <veloc>, false);
CCResponder(nil, <src>, <chan>, <num>, <value>, false);
TouchResponder(nil, <src>, <chan>, <value>, false);
BendResponder(nil, <src>, <chan>, <value>, false);
NoteOnOffResponder(nil, <src>, <chan>, <num>, <veloc>, false);

Where: 

The first argument is always nil because it is provided by the Parameter. 

<src>, <chan>, <num>, <veloc> or <value> are the specs for matching the MIDIResponder. 
<src> and <veloc> or <value> will be nil, and <chan> and <num> will be integers by default if assigned by learning via MIDIListener. 

The last argument, false, prevents the Responder from getting installed at creation time. 

} */
