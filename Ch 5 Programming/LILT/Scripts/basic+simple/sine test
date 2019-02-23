/* This is a simple test script. It creates a synth with a sine tone and specifies controls for setting its output channel, frequency, phase, and amplitude interactively. 

Select this script in the rightmost list view of the Scripts window, then type "t" to open a window for starting/stopping the script and a window for controlling the parameters.

*/

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'freq', 20, 20000, 'exp', 0, 440 ],
[ 'phase', 0, 6.28319, 'linear', 0, 0 ],
[ 'amp', 0, 1, 'amp', 0, 0.1 ]
];

~start = { Synth("variable_sin", ['out', ~out, 'freq', ~freq, 'phase', ~phase, 'amp', ~amp]) };
