//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//could have snare two, quieter and biased to offbeats

InfnoPerc : InfnoInstrument {

	*new {|infno, harmony, rhythm, parts|
	
		^super.new.initPerc(infno, rhythm, parts)
		}
	
	initPerc {|infno, rhythm, parts|

		this.makeSixteenths(infno, rhythm, parts);


		//or chance of entirely different approach
		[
		{this.makeSixteenths(infno, rhythm, parts)}, //can have fourth argument additional if want to pass extra stuff through; could also pass in bar method name! 
		{this.offbeatperc},
		{this.sixteenbeatperc}
		].wchoose([0.85,0.1,0.05]).value;
		
		}

	makeBar {|infno, rhythm, parts,indices, additional|

		var perctemplate, numperc, tmp; 
		var pattern; 
		
		var agg, relation;
		
		agg=rhythm.aggregate;
		
		//[index, 0=support, 1=oppose] second argument actually chance of opposition
		//oppose aggregate of kick and snare is one option, else relation to lead 1
		relation= this.getRelations(parts, [[[0,2],1.0.rand], [[1,2],1.0.rand], [[1,4],rrand(0.0,1.0)], [8,rrand(0.0,1.0)]].scramble, indices);

		if(relation.isNil,{relation= Array.fill(16,{1.0})});

		//what if relation has all zeroes? could happen if perc first created o kick and snare take up all 16ths

		perctemplate= [
		{tmp= relation.collect({arg val,i; if((val>0.5),rrand(0.9,1.0),0) }); },
		{var which= 4.rand; tmp= Array.fill(16,{0.0}); 4.do{arg i; tmp[4*i+which]= rrand(0.9,1.0)}; tmp},
		{var output; output= [Array.rand(8,0.9,1.0),Array.fill(8,{0.0})].lace(16); if(infno.tempo<2.2,{output.rotate(4.rand)}); output},
		{var blank = Array.rand(16,0.8,1.0); 4.do{blank[16.rand]=0;};  blank}
		].choose.value; 
		
		//both first and last option can bias semiquaver offset of 1 too much; see filter step below
		
		numperc= 0;
		
		//don't allow overlaps
		
		pattern= agg.collect({arg val,i; if(val.coin,{if(perctemplate[i].coin && (relation[i]>0.5),{numperc= numperc+1; 1},{0})},{0}); });
		
		//clean some out if too many and higher tempo
		//more generally, want to inhibit certain awkward patterns, but this will do for now 
		if((infno.tempo>2.6), {pattern= pattern.collect({arg val,i; if((i.odd), {if( ((val>0.5) && ((pattern[i-1])<0.5)), 0, val);}, {val}); }); });
		
		//FILTER/REWRITE STEPS TO AVOID AWKWARD OFFSETS
		//count phase offset of 1 and 3 SQ versus 2 and 4: add in difference into 2 and 4 to try and lighten up
	
		pattern= this.filterawkward1(pattern);
		
		//revisable if insufficient events? (allow one recursive recall?)
		if(numperc<3,{3.do {pattern[(8.rand)*2]=1;} });

		^pattern;
	}

	//just play a bar's worth from the rhythm
	//playBar {}
	
	
	//easy and consistent!
	offbeatperc {
	 sixteenths= Array.fill(64, {arg i; if(i%4==2, {if(0.95.coin,1,0)}, 0)})
	}
	
	sixteenbeatperc {
	var chance;
	
	chance= [1,rrand(0.75,1), exprand(0.75,1),rrand(0.68,1)].choose;
	 sixteenths= Array.fill(64, {arg i; if(chance.coin, 1, 0)});
	 
	 sixteenths= this.filterawkward1(sixteenths);
		
	}
	

}