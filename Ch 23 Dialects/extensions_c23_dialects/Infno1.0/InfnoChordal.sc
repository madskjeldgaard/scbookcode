//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//make chords, pads, stabs

//calculate sustain at render time as necessary

InfnoChordal : InfnoPitched {
	
	var <defaultsustain; //<iois, <times, <pitches;
	var <sustainbehaviour, sustainproportion;
	
	*new {|infno, harmony, rhythm, parts|
	
		^super.new.basenote_(60).initChordal(infno, harmony, rhythm, parts)
		}
	
	//can have alternative functions here later
	initChordal {|infno, harmony, rhythm, parts|

	if(0.5.coin,{
	this.makeChordal1(infno, harmony, rhythm, parts);
	},{
	//"pad!".postln;
	
	this.makePad(infno, harmony, rhythm, parts);
	});
	
	}
	
	//returning 0 for last occasionally? 
	//return array of sustain times in beats
	calculateSustains{|array|
		var prev,count;
		var susbeats;
		
		//precalculate distances to next event
		susbeats= Array.fill(64,{arg i; 0.25}); 
		
		prev=0;
		count=0;
		
		sixteenths.do {arg val,j; 
		
		if(val>0.5,{
		
		susbeats[prev]=count*0.25;
		prev=j;
		
		count=0;
		});
		
		if((j==63) && (count>0),{susbeats[prev]=count*0.25;});
		
		count=count+1;
		};
	
		^susbeats;
	
	}
	
	makeBasic {|parts,indices|
		var basic, allevents;
		var tmp;
			
		allevents= this.getRelations(parts, [[[0,1,2,3],0]], indices);
		//could be nil if nothing composed yet! 
		
		if(allevents.isNil,{allevents= Array.fill(16,{1.0})});	
			
		basic= Array.fill(16,{arg i;  [rrand(0.8,1.0), rrand(0.0,0.4), rrand(0.5,1.0), rrand(0.0,0.7)][i%4]});
		
		//could add a weight control basic= basic + (weight*addition);
		basic = basic + ([
		{//follow kick
		allevents= this.getRelations(parts, [[0,0]], indices);
		//could be nil if nothing composed yet! 
		
		if(allevents.isNil,{allevents= Array.fill(16,{arg i; if(i%4==0, 1, 0)});});
		
		allevents
		},
		{
		allevents;
		},
		{
		Array.fill(16,{arg i; if(i%8==0, 1, 0)});
		},
		{
		Array.fill(16,{arg i; if(i%8==4, 1, 0)});
		},
		{ //offbeats
		Array.fill(16,{arg i; if(i%4==2, 1, 0)});
		},
		{//follow bass
		tmp= [0,16,32,48].choose;
		
		allevents= this.getRelations(parts, [[4,0]], (tmp..(tmp+15)));
		//could be nil if nothing composed yet! 
		
		if(allevents.isNil,{allevents= Array.fill(16,{arg i; if(i%4==0, 1, 0)});});
		
		allevents
		}
		].choose.value);
		
		basic= basic.normalizeSum;
		
		^basic;
	}


	//sustain following harmony exactly
	makePad {|infno, harmony, rhythm, parts|
		
		var tmp, indices;
		
		//0 = sustain till next chord, 1= sustain for fixed time, 2= sustain for fixed beats
		sustainbehaviour= 0;
		sustainproportion=[1,rrand(0.5,1.0)].wchoose([0.9,0.1]);
			
		tmp = harmony.get16thChord;
				
		sixteenths= harmony.getSixteenths;	
			
		//sixteenths.postln;	
				
		pitches = Array.fill(64,{arg i; if(sixteenths[i]>0.5, {
		
		//greater chance of octave jump up if lower down and vice versa
		tmp[i] + (tmp[i].collect{|val| var degree; degree= (0.5*((12-val)/12.0)); [-12,0,12].wchoose([0.5*(1-degree),1-degree,degree]/(1.5-(0.5*degree)))});
		
		//probability of shuffling notes a little and doing different inversions
		
		}, 0)}); 
		
		defaultsustain= this.calculateSustains(sixteenths);
	
	}



	makeChordal1 {|infno, harmony, rhythm, parts|
		
		var basic, basic2, activity, tmp, indices;
		
		//0 = sustain till next chord, 1= sustain for fixed time, 2= sustain for fixed beats
		sustainbehaviour= [{sustainproportion=[1,rrand(0.1,1.0)].wchoose([0.8,0.2]); 0},{sustainproportion=rrand(0.1,1.0); 1},{sustainproportion=rrand(1,8)*0.25; 2}].choose.value;
	
		indices= (0..15);
		
		//can have more calls than slots, perfectly safe
		activity= [rrand(1,4),rrand(2,8), rrand(5,15)].wchoose([0.3,0.6,0.1]);
	
		basic= this.makeBasic(parts,indices);	
		
		//one way if don't normalize so much sixteenths = Array.fill(64,{arg i; if(basic[i%16].coin,1,0)});
		
		sixteenths = Array.fill(64,{arg i; 0.0}); 
		
		//first three bars, 3-6 bassnotes a bar? 
		3.do{arg j; activity.do{tmp= indices.wchoose(basic); sixteenths[j*16+tmp]=1}; };
		
		//change activity?
		if(0.4.coin,{activity= [rrand(2,8), rrand(5,15), rrand(16,32)].wchoose([0.3,0.4,0.3]);});
		
		//different for final bar
		basic2= this.makeBasic(parts,(48..63));	
		
		activity.do{tmp= indices.wchoose(basic2); sixteenths[48+tmp]=1}; 
		
		tmp = harmony.get16thChord;
				
		pitches = Array.fill(64,{arg i; if(sixteenths[i]>0.5, {
		
		tmp[i]
		
		//probability of shuffling notes a little and doing different inversions
		
		}, 0)}); 
		
		defaultsustain= this.calculateSustains(sixteenths);
	
	}


	
	
		//play whole chordline over 4 bars worth
	play {|infno, index, mix,n=64, indexing| 
		var latency, susnow, tmp;
		var sustainbeats;
		var synthdef, busnum, bufnum;
			
		busnum=infno.bus[index].index;
		synthdef=mix.synthdef[index];
		bufnum= mix.bufnum[index]; //assumes sample based, but not necessarily used
			
		
		//args= args ? [];
		
		sustainbeats=if(indexing.isNil,{defaultsustain},{this.calculateSustains(sixteenths.at(indexing))  });
		
		//sustainbeats.postln;
		
		{
		n.do{arg j; 
			var val, i;
			
			i= if(indexing.notNil, {indexing[j]},{j}); //allows stutters and permutations
			
			val= sixteenths[i];
			
			if(val>0.5,{
			latency= infno.s.latency+ if(j%2==1,infno.groove, 0.0);
			
			susnow= sustainbeats[i]; //if(indexing.isNil,{susbeats[i];},{0.25});
			//depends on location of next chord and sustain behaviour
			tmp= susnow*(infno.clock.tempo.reciprocal); //susnow in absolute time converted from tempo
			
			susnow= switch(sustainbehaviour,0,{tmp*sustainproportion},1,{min(sustainproportion,tmp)},2,{min(tmp,sustainproportion*(infno.clock.tempo.reciprocal))});
				
			//play chord
			pitches[i].do {arg val;
			infno.s.sendBundle(latency, ["/s_new", synthdef, -1, 0, infno.groupID, \out, busnum, \bufnum, bufnum, \note, val+basenote, \sustain, susnow]); //++args
			};
			
			}); 0.25.wait
		}
		
		}.fork(infno.clock);
		
	}

}