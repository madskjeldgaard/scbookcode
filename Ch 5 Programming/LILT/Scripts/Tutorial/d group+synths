/* (IZ 2005-09-02)
To start, stop and control several synths at once from one Script, create a Group and create the Synths inside that Group: 

Instead of a Synth, the start function of the Script can create and return a Group. When the script stops, it will free the group, thereby freeing any synths that were created within the group. The script may start any number of synths within this group. All these synths will be freed when the script frees the group. Also, setting a groups parameter via a control parameter of the script will set the corresponding parameters in any of the synths within the group that have the same parameter. 

The present example shows how to set the freq parameter of each one of 4 synths to a different multiple of the value of the script's freq control parameter.  The 4 synths are stored in the variables d1, d2, d3, d4, which are declared as local variables of the ~start function. The function that sets the freq parameters of these synths is stored in ~freqAction. ~freqAction is also created by the ~start function and therefore has access to the d1, d2, d3, d4 variables: 
*/

~start = {
	var group, d1, d2, d3, d4;
	// Create group: 
	group = Group(~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]);
	// Note: #[...][~addAction] translates the numeric value of ~addAction to a symbol
	// which is required by the Group.new method. 
	// ~addAction default is 0, i.e. an Integer, for compatibility with Patterns (!)
	// -------- Create 4 synths and store them in d1, d2, d3, d4: 
	#d1, d2, d3, d4 = { Synth("decaysin", ['out', ~out, 'freq', ~freq, 'amp', ~amp,
		'pulse', ~pulse, 'attack', ~attack, 'decay', ~decay, 'vibfreq', ~vibfreq,
		'vibamp', ~vibamp], group, \addToHead); } ! 4;
	~freqAction = { | freq |
		[d1, d2, d3, d3].do { |s, i| s.set(\freq, 3 / 2 ** i * freq ) };
	};
	~freqAction.(~freq); // call freqAction at start to initialize freqs!
	group;
};

/* 
In order to make the control gui use ~freqAction, a custom action function is provided for the 'freq' parameter: 
*/

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'freq', 20, 20000, 'exp', 0, 440, { |freq| // here is the custom function
	~freqAction.(freq);	// it calls the freqAction with then new freq value
	~freq = freq;			// and then stores the new freq value
}],
[ 'amp', 0, 1, 'amp', 0, 0.1 ],
[ 'pulse', 0.1, 30, 'exp', 0, 0.7 ],
[ 'attack', 0.001, 5, 'exp', 0, 0.3 ],
[ 'decay', 0.001, 5, 'exp', 0, 1 ],
[ 'vibfreq', 1e-05, 30, 'exp', 0, 1 ],
[ 'vibamp', 0, 1000, 'linear', 0, 50 ],
];

/* The default stop function must also be modified to prevent the freq control from trying to set the synths while they are no longer running. The synth-set message is removed from the freqAction:
*/

~stop = {
	~process.free;
	~freqAction = { | freq | ~freq = freq }; // remove s.set, keep ~freq assignment
}
 
/* Storing the new value of freq even when the is not running is useful, because it makes the script start with the latest value of freq set by the user independently of whether the script was running or not. 
*/
