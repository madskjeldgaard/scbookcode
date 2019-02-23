	//	figure 8.11 - a Shout class

// begin of file - Shout.sc
Shout {
	classvar <>tag="//!!";
	var <win, <txtView;
	
	*new { ^super.new }
} 
// end of file - Shout.sc

// tests: 
Shout.tag;
Shout.tag_("//SHOUT");

a = Shout.new; 
a.win;
a.txtView;


