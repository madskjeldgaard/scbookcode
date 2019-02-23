Figure 5.11
// run each line separately: 
currentEnvironment;	// empty if no environment variables have been set
~alpha = pi;			// set env. variable ~alpha to pi
currentEnvironment;	// see current Environment again: ~alpha is set
~freq = 800;			// set another environment variable
Server.local.boot;
{ LFNoise0.ar(~freq, 0.1) }.play; // use an environment variable
// setting an environment variable to nil is equivalent to removing it: 
~alpha = nil;
currentEnvironment;	// alpha is no longer set

