/* (IZ 2007-01-01) { 
	A "shell" containing a Group, that returns the real Group in answer to 'asTarget'.
	Used as a proxy-target in a Script's environment variable ~target.
	If the Server of the session to which the Script instance belongs is running, 
	then the NodeTier stored in environment variable ~target of the Script will contain real Group,
	otherwise it will contain nil
	
} */

NodeTier {
	var <>group;
	*new { | group |
		^super.newCopyArgs(group);
	}
	asTarget {
		^group
	}
}
