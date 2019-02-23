//Replace the NRT_TimeFrame in chapter24NRT_Classes.sc with the following implementation
NRT_TimeFrame {

	var >starttime, >duration;
	
	*new {arg starttime, duration;
		^super.newCopyArgs(starttime, duration);
	}

	starttime {arg ... args;
		^starttime.performList(\value, args);
	}

	duration {arg ... args;
		^duration.performList(\value, args);
	}

	endtime {arg startArgs, durArgs;
		var start, dur;
		//use performList to call this object's method "starttime"
		// and pass in the array of arguments for starttime
		startArgs.notNil.if(
			{start = this.performList(\starttime, startArgs);}, 
			{start = this.starttime;}
		);
		//do the same for this.duration
		durArgs.notNil.if(
			{dur = this.performList(\duration, durArgs);}, 
			{dur = this.duration}
		);
		^(start != nil).if({
			(dur != nil).if({
				(dur != inf).if({
					start + dur;
				//else dur is inf, so endtime is nil
				}, {nil})
			//else dur is nil
			}, {nil})
		//else start is nil
		}, {nil});
	}

}
