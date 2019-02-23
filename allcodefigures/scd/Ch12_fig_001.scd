//Figure 12.1
//#!/usr/local/bin/sclang
if (thisProcess.argv.isEmpty) {
	"Missing arguments!".postln;
	exit(1);
};
thisProcess.argv.do { | argu, i |
	"% : %\n".postf(i, argu);
};
exit(0);

