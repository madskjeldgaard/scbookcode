/* (IZ 2005-10-23) {
	Calculate the width and height to fill the remaining of the current row on a FlowLayout,
	from the current position to the bottom right margin.
	Return these as array for ease of use when creating new Rects for views
} */

+ FlowLayout {
	rest {
		^[bounds.width - left - margin.x, bounds.height - top - margin.y];
	}
}