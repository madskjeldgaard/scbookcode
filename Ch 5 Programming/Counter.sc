Counter { 
	// variables: maximum count, current count
	var <>max_count, <>current_count = 1;
	// class method for creating a new instance
	*new { | max_count = 10 |
		^super.new.max_count_(max_count)
	}
	// if maximum count not reached, increment count by 1
	count1 {
		if (current_count >= max_count) {
			this.changed(\max_reached)
		}{
			current_count = current_count + 1;
			this.changed(\count, current_count);
		}
	}
	// reset count
	reset {
		current_count = 1;
		this.changed(\reset);
	}
}