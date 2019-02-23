//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//variations needed, also pass in parameters from a GlobalHarmony data holder? 

//one of these created for each measure sequence (typically 4 of 4/4)
//abstract representation of harmonic underlay, may not be explicitly stated

//can have control parameters for harmonicmodel1

InfnoHarmony {

	classvar <>globalkey=0; //just used at rendering time? 
	classvar <>difficultchance= 0.0;

	var <harmonicrhythm;
	var <chordlist; //as root, chord 
	var <basslist;
	
	classvar diatonic, chords, transitions, substitutions, database; 
	
	*initClass {
	
	diatonic= [0,2,4,5,7,9,11]; 
	
	chords= (0..5).collect{arg val; diatonic.wrapAt(val+[0,2,4])};
	
	transitions =[
	//no prior
	[0,1,2,3,4,5],
	//one prior - list of next, list of weights
	[
	[1,2,3,4,5],[0.15,0.05,0.25,0.25,0.25],
	[3,4,5],[0.3,0.4,0.3],
	[0,1,3,5],[0.2,0.1,0.35,0.35],
	[0,1,4,5],[0.25,0.25,0.25,0.25],
	[0,1,3,5],[0.4,0.1,0.25,0.25],
	[0,1,2,3,4],[0.25,0.15,0.1,0.25,0.25]
	],
	//one prior, one post - add to cover the loop condition
	//[]
	
	];
	
	
	//no tritone substitution or other outside key subs for now since representation not adequate yet 
	substitutions =[
	[[[3,0],[4,0],[5,0]],[0.2,0.5,0.3]],
	[[[5,1]],[1.0]],
	[[[2,0],[0,2]],[0.5,0.5]],
	[[[0,3],[5,3],[1,3]],[0.3,0.4,0.3]],
	[[[0,4],[1,4],[5,4]],[0.4,0.5,0.1]],
	[[[0,5],[1,5],[2,5],[3,5]],[0.3,0.3,0.1,0.3]]
	];
	
	//of known chord sequences as [harmonicrhythm, chordlist]
	//chordtypes: chromatic tone then type 
	database= [
	[4.0.dup(4),[0,1,3,4]],
	[4.0.dup(4),[0,4,5,3]],
	[4.0.dup(4),[0,3,0,3]],
	[4.0.dup(4),[0,0,4,0]],
	[4.0.dup(4),[0,5,1,4]],
	[4.0.dup(4),[5,0,5,4]],
	[4.0.dup(4),[0,3,5,4]],
	[4.0.dup(4),[4,3,4,3]],
	[4.0.dup(4),[1,1,2,2]],
	[4.0.dup(4),[0,0,3,3]],
	[[2.0,2.0,4.0,4.0,4.0],[0,3,1,3,4]],
	[4.0.dup(4),[3,4,0,0]],
	[4.0.dup(4),[[0,5],[5,3],[-2,4],[1,7]]]	//test database representation 2 for chromatic
	];
	
	}
	
	*new {|metersequence|
	
	^super.new.init(metersequence)
	}
	
	init {arg meter;
	
		meter = meter ?? {[4,4,4,4]};
	
		//change back to 0.6
		if(difficultchance.coin,{
		this.harmonicmodel1(meter);
		},{
		this.harmonicmodel2(meter);
		});
	
		//[\seedtest3, thisThread.randData, chordlist].postln;
	
	}
	
	//use database, easy pop sequences mainly
	harmonicmodel2 {arg meter;
	
		var template;
		var temp, temp2;
	
		//[\seedtest4, thisThread.randData].postln;
	
		template= database.choose.copy;
		
		//[\seedtest4, thisThread.randData, template].postln;
		
		harmonicrhythm= template[0];
		
		if(0.1.coin,{template[1]= template[1].scramble;});
		
		//[\seedtest4, thisThread.randData, template].postln;
		
		if(0.2.coin,{template[1]= template[1].rotate(template[1].size.rand);});
		
		//[\seedtest4a, thisThread.randData, template].postln;
		
		//4.0.dup(4),[[0,5],[5,3],[-2,4],[1,7]]
		
		//[\seedtest4b, thisThread.randData, template[1]].postln;
		
		chordlist= template[1].collect{|val|   if(val.size==0,{
		
		//[\debug1, val, val.size, chords, chords[val]].postln;
		
		(chords[val]).copy
		
		},{
		
	
		temp=val[0];
		
		//chord types: more general scheme of possible alterations? 
		//or dictionary of chord types named
		temp2= switch(val[1],
		0,{[0,4,7]},
		1,{[0,3,7]},
		2,{[0,2,7]},
		3,{[0,4,7,10]},
		4,{[0,3,7,10]},
		5,{[0,3,6,10]}, //half dim
		6,{[0,3,6,9]}, //dim
		7,{[0,4,7,11]} //maj 7
		);
		
		//[\debug2, val, val.size,val[1], temp, temp2, (temp2+temp)%12].postln;
		
		
		(temp2+temp)%12;
		})
		
		};
		
		//[\seedtest4, thisThread.randData, chordlist].postln;
		
		//transpose
		if(0.2.coin,{
		temp= [5,7,2,-2,rrand(1,11)].wchoose([0.3,0.3,0.15,0.15,0.1]);
		chordlist=chordlist.collect{|val| (val+temp)%12};
		});
		
		//[\seedtest4, thisThread.randData, chordlist].postln;
		
		//globaltranspose
		chordlist=chordlist.collect{|val| (val+(InfnoHarmony.globalkey))%12};
		
		basslist= chordlist.collect{arg val; 
		
		if(val.size>3,{if(0.1.coin,{val.at([1,2,3].wchoose([0.4,0.3,0.3]))},{val[0]})},
		{if(0.1.coin,{val.at([1,2].wchoose([0.6,0.4]))},{val[0]})});
		
		};
	
	}
	
	
	harmonicmodel1 {arg meter;
		var last, tmp, tmp2;
		var params;
		var splits= [#[2.0,2.0],#[3.0,1.0],#[1.0,3.0],#[2.5,1.5]];
		
		harmonicrhythm= List();
		
		//coin proababilities: 
		params=[
		[0.3,rrand(0.0,0.4),rrand(0.0,rrand(0.0,1.0))].choose,  //0 = rhythm split chance
		[0.0,rrand(0.05,0.15),rrand(0.0,0.2),rrand(0.0,0.5)].wchoose([0.3,0.47,0.2,0.03]), //1= transposition shift 
		[{[1,-1,6,-2,2].wchoose([0.1,0.1,0.2,0.3,0.3])},{[1,-1,rrand(-6,6),5,7].wchoose([0.1,0.1,0.2,0.3,0.3])}].choose, //2 = transposition function
		 [0.0,0.1,rrand(0.0,0.2)].choose,//3= third chance
		 [0.0,0.1,rrand(0.0,0.2)].choose,//4= add 7
		 [0.0,0.1,rrand(0.0,0.2)].choose,//5= add 9
		 [0.0,0.05,rrand(0.0,0.2)].choose,//6= alter 5th
		 [0.0,0.1,0.2].choose, //7= transpose whole sequence
		 [0.0,rrand(0.1,0.3),0.2,exprand(0.01,0.5)].wchoose([0.1,0.4,0.4,0.1]), //8= inversion chance for bass line selecting other than root 
		 [0.0,exprand(0.01,1.0)].wchoose([0.9,0.1]), //9= chromatic bass note substitution
		];
		
		//[thisThread.randData, \params,params].postln;
		
		//meter.do {arg val; if (0.3.coin && (val>2.25), {List.add(2.0) List.add(val-2)},{List.add(val)})};
		
		//CHECK meter.sum == harmonicrhythm.sum
		
		//different chord decision networks
		//tonal harmony (Pistonesque)
		//naive harmony- chord juxtaposition
		//new harmonic system? common tones, leading note 
		//how to deal with chord substitutions, bass inversions
		
		//important stipulations- good loop quality? stronger resolution at loop point, stronger bass at start of loop
		
		//algorithm:
		//generate four chords from Piston rules- except may have two part - was at A, need to get to C, what is B? for loop
		
		//sweep- substitutions - either random chords (to simulate naivety), bass inversions (bassline coherence measure), two for one, chord substitutions (ii -> V for V)
		//possibly iterate sweeps more than once? 
		
		//novelty so far- everything has a novelty score- must avoid over exciting options if scoring a lot so far. 
		//novelty total over whole construct?
		
		//chance of repeat first pattern twice
		
		chordlist= List[transitions[0].choose];
		
		last=chordlist[0];
		
		3.do{arg val; 
	
		tmp= (transitions[1][2*last]).wchoose(transitions[1][2*last+1]);
		
		chordlist.add(tmp.copy);
		
		last=tmp;
		
		};
		
		
		//substitutions //create harmonicrhythm same time
		
		tmp= chordlist; 
		
		chordlist= List();
		harmonicrhythm= List();
		
		//will need to involve meter eventually
		tmp.do{arg chord;
		var sub;
		
		if (params[0].coin, {
		tmp2= splits.wchoose([0.93,0.04,0.02,0.01]);
		
		harmonicrhythm.add(tmp2[0]); harmonicrhythm.add(tmp2[1]);
		sub= substitutions[chord].deepCopy;
		
		sub= sub[0].wchoose(sub[1]);
		
		chordlist.add(sub[0]);
		chordlist.add(sub[1]);
		
		},{
		harmonicrhythm.add(4.0);
		chordlist.add(chord);
		})
		
		
		};
		 
		 
		//substitute random (convert chordlist into actual chords, preserve bassline)
		
		chordlist= chordlist.collect{arg val; tmp= (chords[val]).copy;   
		
		//randoms- add 7th, compact 5th, sharpen 3rd, add 9th, shift whole by 1, tritone or fifth
		
		//"chgeck!".postln;
		//tmp.postln;
		
		//shift by transposition
		
		if(params[1].coin,{tmp= tmp + (params[2].value)});
		
		//random change 3rd quality- major/minor swap or suspended fourth or second
		if(params[3].coin,{tmp[1]= tmp[1]+([1,-1].choose)});
		
		//random add 7
		if(params[4].coin,{tmp= [tmp,[tmp[0]+([10,11].wchoose([0.8,0.2]))]].flatten});
		
		//random add 9
		if(params[5].coin,{tmp= [tmp,[tmp[0]+([13,14].wchoose([0.1,0.9]))]].flatten});
		
		//random alter 5th (should be quite rare!)
		if(params[6].coin,{tmp[2]= tmp[2]+([2,1,-1,-2].choose)});
		
		tmp%12;
		
		};
		 
		//transpose whole thing
		if(params[7].coin,{
		tmp2= [5,7,2,-2,rrand(1,11)].wchoose([0.3,0.3,0.15,0.15,0.1]);
		chordlist=chordlist.collect{|val| (val+tmp2)%12};
		});
		
		//global transposition
		chordlist=chordlist.collect{|val| (val+(InfnoHarmony.globalkey))%12};
		 
		//bassline continuity/inversions - add in bassline from roots, also chance of random bassnote (Prokofiev like)
		
				
		basslist= chordlist.collect{arg val; 
		
		tmp2= if(val.size>3,{if(params[8].coin,{val.at([1,2,3].wchoose([0.4,0.3,0.3]))},{val[0]})},
		{if(params[8].coin,{val.at([1,2].wchoose([0.6,0.4]))},{val[0]}); });
			
		if(params[9].coin,{tmp2= rrand(-12,12)});
		
		tmp2
		};   	
		
	}
	
	getSixteenths {
		var pattern, sofar, tmp;
		
		pattern= Array.fill(64,{0});
		
		sofar=0;
		
		harmonicrhythm.do {arg val,j; 
		
		tmp= (val*4).round(1).asInteger;
		
		pattern[sofar]= 1;
		
		sofar=sofar+tmp;
		};
		
		^pattern;
	
	}
	
	
	get16thForBass {
		var bassseq, sofar, tmp;
		
		bassseq= Array.fill(64,{0});
		
		sofar=0;
		
		harmonicrhythm.do {arg val,j; 
		
		tmp= (val*4).round(1).asInteger;
		
		tmp.do {arg i; bassseq[sofar+i]= basslist[j]};
		
		sofar=sofar+tmp;
		};
		
		^bassseq;
	
	}
	
	
	
	get16thChord {
		var chordseq, sofar, tmp;
		
		chordseq= Array.fill(64,{0});
		
		sofar=0;
		
		harmonicrhythm.do {arg val,j; 
		
		tmp= (val*4).round(1).asInteger;
		
		tmp.do {arg i; chordseq[sofar+i]= (chordlist[j]%12)};
		
		sofar=sofar+tmp;
		};
		
		^chordseq;
	
	}
	
	//get a diatonic for each successive chord, return index sequence and diatonic list
	//diatonic is best matching scale from 12 major and 12 minor to the union of the current
	//AND THE NEXT chord
	getScales {
		var indexsequence, scales, sofar, tmp, tmp2;
		
		indexsequence= Array.fill(64,{0});
		
		sofar=0;
		
		scales= chordlist.copy; //Array.fill(chordlist.size,{[0]})
		
		harmonicrhythm.do {arg val,j; 
		
		tmp= (val*4).round(1).asInteger;
		
		tmp.do {arg i; indexsequence[sofar+i]= j};
		
		//unify with standard diatonic, noting discrepancies? 
		tmp2= chordlist[j];
		
		if(j<(chordlist.size-1),{tmp2=tmp2+(chordlist[j+1]);});
		
		scales[j]= this.bestscalematch(tmp2);
		
		sofar=sofar+tmp;
		};
		
		^[scales, indexsequence];
	
	}
	
	bestscalematch {|pattern|
		var bestscore, bestscale;
		var major, minor;
		var tmp, score;
		
		major=[1,0,1,0,1,1,0,1,0,1,0,1]; //[0,2,4,5,7,9,11]; 
		minor=[1,0,1,1,0,1,0,1,1,0,0,1]; //[0,2,3,5,7,8,11]; //harmonic since melodic would come out the same!
		
		bestscore= (-1000); 
		
		12.do{|i|  
		
		tmp= major.rotate(i); 
		score= 0;
		
		pattern.do {|val| if(tmp[val]==1,{score=score+1});};
		
		if(score>bestscore,{bestscore=score; bestscale=([0,2,4,5,7,9,11]+i)%12;});
		
		tmp= minor.rotate(i); 
		score= 0;
		
		pattern.do {|val| if(tmp[val]==1,{score=score+1});};
		
		if(score>bestscore,{bestscore=score; bestscale=([0,2,3,5,7,8,11]+i)%12;});
		
		};  //(major+i)%12;
		
		^bestscale;
	}
	
	*createfrombassline {|bass|
	
	}
	
	*createfromleadline {|lead|
	
	}
	
	*variation {|input|
	
	
	}

}