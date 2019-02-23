/* (IZ 2005-08-21)
Envelopes with release nodes can be used to create a smooth fade-out of the sound when stopping a synth. To stop a synth by releasing an envelope, store the message that you wish to send to the synth to release it together with its parameters in the environment variable ~stopMessage. The synth will keep running after the stop message for the duration of the release time of the envelope. The start/stop button indicates this by changing its background color to grey until the release phase is over. 
*/

// equivalent to: ~process.set(\gate, 0), sent by stop method: 
~stopMessage = [\set, \gate, 0];

// The controls for this example: 
~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'freq', 20, 20000, 'exp', 0, 500 ],
[ 'amp', 0, 1, 'amp', 0, 0.1 ],
[ 'attack', 0.001, 5, 'exp', 0, 2 ],
[ 'release', 0, 16, 'linear', 0, 3 ],
[ 'vibFreq', 0, 6, 'linear', 0, 3 ],
[ 'vibAmp', 0, 2, 'linear', 0, 1 ]
];

// The synth creating start function:
~start = { Synth("martianwinter", ['out', ~out, 'freq', ~freq, 'amp', ~amp, 'attack', ~attack, 'release', ~release, 'vibFreq', ~vibFreq, 'vibAmp', ~vibAmp], ~target,#[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
