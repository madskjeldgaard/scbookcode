/* (IZ 2005-09-05) { 
	Make a window resize to new height and width while keeping its top where it was.
	(because of cocoa giving y from bottom this requires some calculation)
} */

+ SCWindow {
	resize { | height = 200, width |
		width = width ?? { this.bounds.width };
		this.bounds = this.bounds
			.top_(this.bounds.top + this.bounds.height - height)
			.height_(height)
			.width_(width)
	}
}
