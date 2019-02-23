/* IZ 041116
Adding basic index access capability to PointArray
*/

+ PointArray {
	at { |index| ^Point(x.at(index), y.at(index)); }
	xat { |index| ^x.at(index) }
	yat { |index| ^y.at(index) }
}
