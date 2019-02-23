//	figure 8.1 - a Puppet class, and tests for it.
	
Puppet { 
	var <>myfreq; // an instance variable with a getter and a setter method
	
		// a method for creating a new object of this kind
	*new { |myfreq=50| ^super.new.myfreq_(myfreq) }
	
		// a simple method that uses 'myfreq' for something audible.
	blip { { Blip.ar(myfreq, 11) * XLine.kr(1, 0.01, 0.6, doneAction: 2) }.play; }
}

	// tests for the behavior implemented so far:
m = Puppet.new(50);// make an instance of Puppet, pass in myfreq

m.dump;			// test that myfreq is set correctly
m.myfreq;			// test accessing myfreq
m.blip;			// should sound
m.myfreq_(100);	// test setting myfreq
m.blip;		 // should sound differently




