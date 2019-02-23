//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//kick calculated first 

InfnoKick : InfnoInstrument {

	classvar <>fourfloorchance=0.2;

	*new {|infno, harmony, rhythm, parts|
	
		^super.new.initKick(infno, rhythm, parts)
		}
	

	//don't call init because will overload base class init
	initKick {|infno, rhythm, parts|
		
		//or chance of entirely different approach
		
		if(fourfloorchance.coin, {
		this.fourtothefloor
		},
		{this.makeSixteenths(infno, rhythm, parts)}); //can have fourth argument additional if want to pass extra stuff through; could also pass in bar method name! 
		
				
				//.wchoose([0.8,0.2]).value;
		
		}
		
		
	makeBar {|infno, rhythm, parts,indices, additional|

		var kicktemplate, numkicks; 
		var pattern;
		
		var agg, tempo, relation;
		
		agg=rhythm.aggregate;
		
		//preferably react to bass, then snare
		//[index, 0=support, 1=oppose] second argument actually chance of opposition
		relation= this.getRelations(parts, [[4,0],[1,1]], indices);
		
	
		kicktemplate= [[0.9,0.5,0.0,0.0,0.8,0.5,0.0, 0.3],Array.fill(8,{0})].lace(16);
		
		2.do {kicktemplate[4*(4.rand)+3]= rrand(0.0,0.5)};

		if(relation.notNil,{
		//only a small influence to avoid too many probabilities of 1
		kicktemplate= (kicktemplate+(0.25*relation)).min(1);
		});
		
		numkicks= 0;
		
		pattern= agg.collect({arg val,i; if(val.coin,{if(kicktemplate[i].coin,{numkicks= numkicks+1; 1},{0})},{0}); });
		
		//clean some out if too many and higher tempo
		if((infno.tempo>2.6) && (numkicks>4), {pattern= pattern.collect({arg val; if(val>0.5, {if(0.5.coin,1,0) },0);})});
		
		//revisable if insufficient events? (allow one recursive recall?)
		if(numkicks<2,{pattern[[0,8].choose]=1;});
		
		^pattern;	
	}

	//easy!
	fourtothefloor {
	 sixteenths= Array.fill(64, {arg i; if(i%4==0, 1, 0)})
	}
	
	//more and more frantic...
	//rollfill 


}