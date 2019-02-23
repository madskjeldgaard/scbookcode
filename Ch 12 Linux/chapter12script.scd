#!/usr/local/bin/sclang

// Figure 12.1

if (thisProcess.argv.isEmpty) {
	"Missing arguments!".postln;
	exit(1);
};

thisProcess.argv.do { | x, i |
	"% : %\n".postf(i, x);
};

exit(0);
