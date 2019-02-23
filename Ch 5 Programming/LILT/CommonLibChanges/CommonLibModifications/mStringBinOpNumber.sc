/* IZ Wednesday, January 2, 2008
	make 
	1 + "2" return 1
	instead of 
	"2 1"
*/

+ String {

	performBinaryOpOnSimpleNumber { arg aSelector, aNumber; 
		^aNumber.asString.perform(aSelector, this)
	}

}
