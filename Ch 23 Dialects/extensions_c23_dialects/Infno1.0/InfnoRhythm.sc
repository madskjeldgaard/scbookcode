//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//stripped down to the minimum for now, using 4 bars of 4/4 assumption for generation and cutting short for playback if desired...

//metre sequence over a section

//aggregate, master rhythm - multiple views? 
//assuming 16ths for now

//could have a database and merge any two from database
//probability template to generate master rhythm, then probability filter for 
//taking a given subset


InfnoRhythm {
	
//	classvar aggtemplate;
	
	var <aggregate;
	//var <totallength;
	//var <barchain;

/*
	*initClass {
		
		aggtemplate = [];
	
	
	}
*/
	*new {arg tempo;
	
		^super.new.init(tempo)
		}
	

	init {|tempo|

		//calculation of this aggregate could be the function to be creatively determined?
		//abstract layers are most subject to new algorithms, filter constraints of other parts more critical
		//most have some heuristics to enforce useful interpretations? 
		 
		aggregate= Array.fill(4,{[rrand(0.95,1.0),rrand(0.8,1.0),rrand(0.93,1.0),rrand(0.85,1.0)]}).flatten;
		
		//barchain= [16,16,16,16];
		//totallength=64;
		
	
	}
	
	


}