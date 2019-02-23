
+ MultiPageLayout {
	recursiveResize {
		this.checkNotClosed.if({
			this.view.recursiveResize;
			this.resizeToFit;
		});
	}
}

+ SCViewHolder {
	findRightBottom { 
		var	out;
		out = view.findRightBottom;
		^out
	}
}

+ FlowView {
	resizeToFitContents {
			// need bounds relative to parent's bounds
		var new, maxpt, comparept, mybounds, used;
		mybounds = this.bounds;
		if(view.tryPerform(\relativeOrigin) ? false) {
			maxpt = Point(0, 0);
		} {
			maxpt = mybounds.leftTop;
		};
		this.children.do({ arg c;
			comparept = c.findRightBottom;
			maxpt = maxpt.max(comparept);
		});
		if(view.tryPerform(\relativeOrigin) ? false) {
			new = mybounds.resizeTo(maxpt.x + this.decorator.margin.x,
				maxpt.y + this.decorator.margin.y);
		} {
			new = mybounds.resizeTo(maxpt.x - mybounds.left + this.decorator.margin.x,
				maxpt.y - mybounds.top + this.decorator.margin.y);
		};
		this.bounds_(new, reflow: false);	// don't reflow unless asked
		^new
	}

	recursiveResize {
		this.children.do({ arg c;
			c.recursiveResize;
		});
		this.tryPerform(\reflowAll);
		this.tryPerform(\resizeToFitContents).isNil.if({
			this.tryPerform(\resizeToFit);
		});
	}
}

+ Object { isActive { ^false } }		// non-views should reply with false

+ StartRow {
	recursiveResize { ^nil }
	findRightBottom { ^Point(0, 0) }
}


+ Point {
	max { arg that;
		^Point(this.x.max(that.x), this.y.max(that.y))
	}
	
	min { arg that;
		^Point(this.x.min(that.x), this.y.min(that.y))
	}
}

// for debugging

+ Integer {
	reptChar { arg c = $\t;
		^(c ! this).as(String);
	}
}
