/* (IZ 2005-08-21)
To create controls for changing the parameters of a synth (or any other process that is run by a script), specify the names and ranges of the controls in an array and save it in the environment variable ~controlSpecs.  For example: 
*/

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'freq', 20, 20000, 'exp', 0, 440 ],
[ 'phase', 0, 6.28319, 'linear', 0, 0 ],
[ 'amp', 0, 1, 'amp', 0, 0.1 ]
];

/* The above example specifies 4 control parameters: out, freq, phase and amp. Select the present script and type t to create a gui. This will contain 4 sliders for controlling each of these parameters. Changing the values of the parameters through the GUI will always set the environment variables with the corresponding names in the scripts event-environment and will set the parameters of the Synth or Group process if it is running. 

The Script will initialize the envrironment variables with the default parameter values given in the spec, so that these can be used at startup as defaults.  In order for the Synth to use the existing values immediately at its creation, these must be included in the args argument of the Synth-new message, as follows: 
*/

~start = { Synth("variable_sin", ['out', ~out, 'freq', ~freq, 'phase', ~phase, 'amp', ~amp],
	~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };

/* The format of the control specs array is:
[spec1, spec2, spec3 ... specn] where each of spec1 ... specn has the form: 

[name, min, max, warp, step, default, (optional: action-function)]

The variable names above stand for the above values: 
name: a Symbol then name of the parameter
min:  a SimpleNumber (Integer or Float), minimum value that the parameter can take
max: a SimpleNumber, maximum value that the parameter can take
warp: A Warp specifying how the range of 0-1 from the slider is mapped to the min-max range.
     Usually one of: 'linear' ('lin') or 'exponential' ('exp').
step: If the step is other than 0, then the values that the parameter can take will be multiples of the step. 
default: The default value of the parameter, set when the script is initialized. 

The last value in the array, the action function, is optional. It can specify a function different one than the default action function that is called when the parameter is set via the gui or other action.  Use of the custom action function is shown in the next example, "group+synths". 
*/
