//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//typically a 4 bar segment (possibility of 8 or more arbitrary setups?), with rendering capability

//various ways to copy these around, i.e. preserving harmony etc

InfnoHyperMeasure {

	var <rhythm, <harmony; 	//arrays of source patterns	
	var <parts;
	
	//<bass, <lead1, <lead2, <lead3, <chord;
	
	*new {arg infno,creationlist, activity;
	
	^super.new.makeHighLevelStructure(infno).compose(infno, creationlist, activity);
	}
	
	makeHighLevelStructure {|infno|
	
	var tempo= infno.tempo;
	
	//could make an InfnoMetricalStructure class
	//definitely make it a metre class!  
	rhythm= InfnoRhythm(tempo);
	harmony= InfnoHarmony();
	
	//[\seedtest1, thisThread.randData].postln;
	
	}
	
	//set up parts with any rendering info they need at this point? but mix will hold real data...
	compose {|infno, creationlist, activity|
	
	var dynamicprogramming = infno.dynamicprogramming; 
	
	//just create into predefined slots, always create all; quick check by creation classes to see which exist already
	//avoid search through loop 
	
	parts = Array.fill(10,nil);
	
	//symbol switch on which? put a lookup function in Infno base class for these symbols? 
	creationlist.do {|which, i|  
	var whichindex= infno.lookupindex[which];
	
	//(\SC++\Window).asSymbol.asClass
	
	//which.postln;
	//infno.lookupclass[which].postln;
	//whichindex.postln;
	
	//passes in parts so far and choices will work accordingly
	parts[whichindex]= (infno.lookupclass[which]).new(infno, harmony, rhythm, parts, whichindex-5);
	
	};
	
	//bass= InfnoBassline(harmony, rhythm);
//	
//	chord= InfnoChordline(harmony, rhythm, bass);
//	
//	//using fast version rather than dyn prog for now
//	lead1= InfnoLeadline(60,harmony, rhythm,if(0.4.coin, {bass},nil), dynamicprogramming);
//	lead2= InfnoLeadline(60,harmony, rhythm,lead1, dynamicprogramming);
//	lead3= InfnoLeadline(72,harmony, rhythm,[bass,lead1,lead2].choose, dynamicprogramming);
//	

	
	}
	
	//make a subtle variation of existing data using a secondary pattern
	variation {|other, activity=0.02|
	
		//just crossover percussion
		parts[0..3].do{|val,i| val.crossover(other.parts[i],activity)};
	
	}
	
	fillfunc {|indices|
		var tmp;
		
		tmp= [2,4,8].wchoose([0.1,0.7,0.2]);
		
		^[indices,{indices.rotate(tmp*(rrand(1,indices.size.div(2))))}, {this.flickerfunc(indices)}].wchoose([0.4,0.3,0.3]).value;
	}
	
	rollfunc {|indices|
		var repeatstart; 
		
		repeatstart= nil; 
		
		^indices.collect{|i,j| var prop; prop=j/(indices.size);  if(repeatstart.isNil,{if(prop.coin,{repeatstart=i;}); i},{repeatstart}); };
	}
	
	//assumes indices size is even
	flickerfunc {|indices|
		
		if(indices.size.odd,{^indices});
		
		^Array.fill(indices.size.div(2),{|i| var now= indices[2*i]; [[now,now],[now,now+1],[now+1,now+1]].choose }).flatten;
		
	}
	
	//usually for end of hypermeasure, not necessarily all instruments
	//different each time? 
	fill {|infno, mix, which, n=64,indexing|
	
		var fill, indexingnow; 
		
		fill= (this.fillfunc((0..15)))++(this.fillfunc((16..31)))++(this.fillfunc((32..47)));
		
		fill = fill++([{this.rollfunc((48..63))},{this.fillfunc((48..63))}].choose.value); 
	
		//"fill!".postln;
		//fill.postln;
	
		which= which ?? {(0..8)}; //play all if not given
		
		//remove reptitions 
		which=which.asSet.asArray; 
		
		{
		//which array of indices or track names? 
		which.do {|index|
		
		if(parts[index].notNil,{
		
		indexingnow= if(index<4,{fill},{indexing});
		
		parts[index].play(infno, index, mix,n, indexingnow);
		});
		
		};
		
		}.fork(infno.clock)
	
	}
	
	//can shorten this code very much using perform! 
	//infno object gives bus etc, mix gives params like busnum and synthdefs
	play {|infno, mix, which, n=64, indexing|
		//var synthdef;
		//var bars;
		
		//synthdef= mix.synthdef;
		
		which= which ?? {(0..8)}; //play all if not given
		
		//remove reptitions 
		which=which.asSet.asArray; 
		
		//which.postln;
		
		{
		//which array of indices or track names? 
		which.do {|index|
		
		//"play".postln;
		//index.postln;
		//parts[index].postln;
		
		//no range check on index<10
		if(parts[index].notNil,{
		//any special arguments are already in the object like lag array etc
		parts[index].play(infno, index, mix,n, indexing);
		
		});
		
		};
		
		}.fork(infno.clock)
	
	}

	
	/*
	*interpolate {|other|
	
	}
*/

}