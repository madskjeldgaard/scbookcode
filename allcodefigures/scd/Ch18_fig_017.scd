Figure 18.17
NRT_TimeFrame {

	var >starttime, >duration;
	
	*new {arg starttime, duration;
		^super.newCopyArgs(starttime, duration);
	}

	starttime {
		^ starttime.value;
	}

	duration {
		^ duration.value;
	}

	endtime {
		^(this.starttime != nil).if({
				(this.duration != nil).if({
					//call the getter methods rather than accessing
					// the variables directly
					this.starttime + this.duration;
				}, {nil})
		}, {nil});

	}

}

