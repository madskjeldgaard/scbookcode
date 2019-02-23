//	figure 8.17 - codeDump tests and usage in Shout

this.codeDump.postcs;	// anything there yet? by default, this is nil.
this.codeDump = this.codeDump.addFunc(Shout.defaultCodeDumpFunc); // add Shout
this.codeDump.postcs	// should be there now
//!! test whether Shout works now
this.codeDump = this.codeDump.removeFunc(Shout.defaultCodeDumpFunc);
this.codeDump.postcs	// should be gone now

//!! should be off again

Shout { 
	...
	*add { var interp = thisProcess.interpreter; 
		interp.codeDump = interp.codeDump
			.removeFunc(defaultCodeDumpFunc) // remove it first so it will 
										// only be in the list once
			.addFunc(defaultCodeDumpFunc); 
	}
	*remove { var interp = thisProcess.interpreter; 
		interp.codeDump = interp.codeDump.removeFunc(defaultCodeDumpFunc); 
	}
	...	
}

// tests
Shout.add;
//!! test whether Shout works now - it should!
Shout.remove;
//!! test whether Shout works now - should be off.







