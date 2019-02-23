
	// figure 8.12 - more class variables and initClass method.

// begin of file - Shout.sc
Shout {
	classvar <>tag="//!!", <>width=1250, <>defaultCodeDumpFunc; 
	var <win, <txtView;
	
	*initClass { 
		defaultCodeDumpFunc = { |str| if (str.beginsWith(tag)) { Shout(str.drop(tag.size)) } };
	}
	*new { ^super.new }
} 
// end of Shout.sc

Shout.width;
Shout.defaultCodeDumpFunc;

