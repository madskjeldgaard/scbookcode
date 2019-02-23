//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//aggregate, master rhythm - multiple views? 

//creativity as multiple sweeps, changing mind many times, often based on influence of other parts? can choose rule for each sweep? Perturb system and let it settle into oscillations of revision?

//various priors-
//make a basic template then add bias:
//1. from kick
//2. from offbeat or onbeat
//3. from anti-rhythm
//4. anticipatory
//normalizeSum for array

//choose activity level, determines number of selections from prior mould
//then select rhythm from this, for each bar - different generative mould for final bar? 
//(create as 16 beat slots first)

//now convert to event times
//allow a possibility of one tuplet
//choose bass notes from harmony bassline only, +- octave in certain patterns - + - +, ++-- etc 
//could optionally start to allow other pitches, i.e. walking bass, interpolants, even extra events as connectives

//another way- riffs? (both rhythmic and melodic cells)
//yet another way- cross riffs with locations? 


//CO_COMPOSED PATTERNS 1001001010010010, 1010001010100010 with tonic tonic one below tonic tonic one below etc

//two conditions- both location, and second sweep to check for connectives

InfnoBass : InfnoPitched {
	
	*new {|infno, harmony, rhythm, parts|
	
		^super.new.initBass(infno, harmony, rhythm, parts)
		}
	
	//can have alternative functions here later
	initBass {|infno, harmony, rhythm, parts|
	
	this.makeBass1(infno, harmony, rhythm, parts);
	
	}
	
	makeBasic {|parts,indices|
		var basic;
		var allevents;
		var temp;
			
		basic= Array.fill(16,{arg i;  [rrand(0.8,1.0), rrand(0.0,0.4), rrand(0.5,1.0), rrand(0.0,0.7)][i%4]});
		
		allevents= this.getRelations(parts, [[[0,1,2,3],0]], indices);
		//could be nil if nothing composed yet! 
		
		if(allevents.isNil,{allevents= Array.fill(16,{1.0})});
		
		//could add a weight control basic= basic + (weight*addition);
		basic = basic + ([
		{
		allevents= this.getRelations(parts, [[0,0]], indices);
		//could be nil if nothing composed yet! 
		
		if(allevents.isNil,{allevents= Array.fill(16,{arg i; if(i%2==0, 1, 0)});});
		
		allevents
		},
		{
		allevents;
		},
		{	//antirhythm - check not all 1! 

		temp=allevents.sum;
		
		if(temp>15.5,{
		allevents= [Array.fill(8,{[0.0,0.2.rand].choose}),Array.fill(8,{1.0.rand})].lace(16);
		});
		
		1-allevents;
		},
		{
		Array.fill(16,{arg i; if(i%4==0, 1, 0)});
		},
		{
		Array.fill(16,{arg i; if(i%4==2, 1, 0)});
		},
		{
		Array.fill(16,{arg i; if(i%4==3, 1, 0)});
		},
		{
		 Array.fill(16,{arg i; if(i%2==0, 1, 0)});
		}
		].choose.value);
		
		basic= basic.normalizeSum;
		
		^basic;
	}


	makeBass1 {|infno, harmony, rhythm, parts|
		
		var basic, basic2, activity, tmp, indices;
		
		
		indices= (0..15);
		
		//can have more calls than slots, perfectly safe
		activity= [rrand(2,8), rrand(5,15), rrand(16,32)].wchoose([0.3,0.4,0.3]);
	
		basic= this.makeBasic(parts, indices);	
		
		//one way if don't normalize so much sixteenths = Array.fill(64,{arg i; if(basic[i%16].coin,1,0)});
		
		sixteenths = Array.fill(64,{arg i; 0.0}); 
		
		//first three bars, 3-6 bassnotes a bar? 
		3.do{arg j; activity.do{tmp= indices.wchoose(basic); sixteenths[j*16+tmp]=1}; };
		
		//change activity?
		if(0.4.coin,{activity= [rrand(2,8), rrand(5,15), rrand(16,32)].wchoose([0.3,0.4,0.3]);});
		
		//different for final bar
		basic2= this.makeBasic(parts,(48..63));	
		
		activity.do{tmp= indices.wchoose(basic2); sixteenths[48+tmp]=1}; 
		
		[{this.fundamental(harmony);},{this.walking1(harmony);}].wchoose([0.7,0.3]).value;
		
		//leave for now
		//ioi representation and hence tuplets are problematic for groove factor 
		//make tuplet: find x in a row, substitute tuplet over their iois
		
	
	}
	
	//tracking fundamental line only, with chance of octave bopping
	fundamental {|harmony|
		var tmp;
		var dooctaves, upordown, octavechance;
	
		tmp = harmony.get16thForBass;
		
		dooctaves=0.2.coin;
		octavechance= [1,exprand(0.1,1.0),rrand(0.1,1.0)].choose;
		upordown= [12,-12,24].wchoose([0.6,0.35,0.05]);
		
		pitches = Array.fill(64,{arg i; if(sixteenths[i]>0.5, {
		
		//if eighth note offbeat
		if((dooctaves) && (i%4==2) && (octavechance.coin),{tmp[i]+upordown},
		{
		tmp[i]
		});
		
		}, 0)}); 
	
	}
	
	//look at difference to previous note, if same start walking around... chromatically down or following master harmony:
	//tone then out by fifth and return? 
	//look at target notes too? harmony.basslist
	walking1 {|harmony|
		var bassnotes, scales, tmp;
		var dooctaves, upordown, octavechance;
		var prev, change;
		var scalenow, walkpos, walkprob, shiftprob;
		//get diatonic template for each chord in 12 tones
		
		//[scales,indices]
		scales = harmony.getScales;
		bassnotes= harmony.get16thForBass;
		
		prev=bassnotes[0]+1;
		
		pitches = Array.fill(64,{arg i; 
		var bassnow;
		
		bassnow= bassnotes[i];
		
		change= if(bassnow==prev,false,{
		
		scalenow= scales[0][scales[1][i]]; //over two octaves? 
		
		scalenow = scalenow++(scalenow+([12,-12,5,7,-5,-7].wchoose([0.3,0.3,0.1,0.1,0.1,0.1])));
		
		scalenow=scalenow.sort; //to keep in proper order...
		
		walkpos= scalenow.size.rand;
		
		walkprob= [1.0,rrand(0.0,1.0)].wchoose([0.3,0.7]);
		//shiftprob=[1.0,rrand(0.0,1.0)].wchoose([0.3,0.7]);
		
		true});
		
		//set up walking scale and transition probabilities/walk nature
		
		prev=bassnow;
		
		if(sixteenths[i]>0.5, {
				
		if(change,{bassnow},{
		//can have probability of change within scale, or of returning to root
		//walk by successive steps in the scale
		
		//(scales[0][scales[1][i]]).choose;
		
		walkpos= walkpos + ([-1,0,1].choose);
		
		if(walkpos<0, {walkpos=3.rand});
		if(walkpos>(scalenow.size-1), {walkpos=(scalenow.size)-1 - (3.rand);});
		
		if(walkprob.coin,{scalenow[walkpos]},{bassnow});
		
		});
		
		}, 0)}); 
	
	}

	//look at master harmony, fundamental line and relation to metre
	//moving {
	//}


}