//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//monophonic lead; overloaded play method
//also see InfnoMonoBass; can't derive from common source unless move pitch creation methods of Lead into base class etc common functions are in InfnoPitched

InfnoMonoLead : InfnoLead {
	
	*new {|infno, harmony, rhythm, parts,index|
	
		^super.new(infno, harmony, rhythm, parts,index).initMonoLead(infno, harmony, rhythm, parts,index);
		}
	
	//can have alternative functions here later
	initMonoLead {|infno, harmony, rhythm, parts,index|
	var method, temp; 
	 
	 predelay=0.8.coin;
	 
	//create lag amounts and required timings with respect to note onset; effectively gives array of onset times for a given tempo
	
	temp= [rrand(0.0,1.0), rrand(0.0,0.8), rrand(0.0,0.4), exprand(0.001,0.2)].normalizeSum;
	
	method={[-1,rrand(0.01,0.1),exprand(0.005,0.2),exprand(0.05,0.5)].wchoose(temp)}; 
	
	//{[-1,rrand(0.01,0.1),exprand(0.005,0.2),exprand(0.05,0.5)].wchoose([0.2,0.3,0.15,0.05])};
	
	//-1 = reset (create new synth or send trig to current), otherwise lag time
	//could quantise lags to semiquaver grid but let's do without for now!
	lags= Array.fill(64,{method.value;});
	
	defaulttimings= this.precalculatelagtimings(sixteenths, infno.tempo); //pass infno for tempo
	
	}
	
	//functions in pitched base class as helper functions for the derived classes of bass and lead!
	
	play {|infno, index, mix,n=64, indexing| 
	
	this.monoplay(infno, index, mix,n, indexing);
	
	}


}