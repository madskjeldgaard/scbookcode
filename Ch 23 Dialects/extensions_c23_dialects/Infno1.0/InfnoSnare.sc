//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//could have snare two, quieter and biased to offbeats

InfnoSnare : InfnoInstrument {

	*new {|infno, harmony, rhythm, parts|
	
		^super.new.initSnare(infno, rhythm, parts)
		}
	

	//don't call init because will overload base class init
	initSnare {|infno, rhythm, parts|

		this.makeSixteenths(infno, rhythm, parts);

		}

	makeBar {|infno, rhythm, parts,indices, additional|

		var snaretemplate, numsnares; 
		var pattern; 
		
		var agg, relation;
		
		agg=rhythm.aggregate;
		
		//oppose any kick
		relation= this.getRelations(parts, [[0,1]], indices);
		//if no kick to oppose, busiest, most admissible 
		if(relation.isNil,{relation= Array.fill(16,1)});
		
		snaretemplate= [[0.0,0.1,0.9,0.1,0.0,0.1,0.95, 0.3],Array.fill(8,{0.0})].lace(16);
		
		2.do {snaretemplate[[7,11,15].choose]= rrand(0.0,0.5)};

		numsnares= 0;
		
		//don't allow overlaps
		
		pattern= agg.collect({arg val,i; if(val.coin,{if(snaretemplate[i].coin && (relation[i]>0.5),{numsnares= numsnares+1; 1},{0})},{0}); });
		
		
		//revisable if insufficient events? (allow one recursive recall?)
		if(numsnares<2,{pattern[[4,12].choose]=1;});
		
		^pattern;
	}

	//just play a bar's worth from the rhythm
	//playBar {}
	

}