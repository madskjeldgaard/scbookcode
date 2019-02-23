Figure 6.16.  The definition of Event’s play method.
	play {
		if (parent.isNil) { parent = defaultParentEvent };
		this.use { ~play.value };
	}


