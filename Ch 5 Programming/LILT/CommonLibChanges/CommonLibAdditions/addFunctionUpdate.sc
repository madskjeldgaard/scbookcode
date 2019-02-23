/* IZ 03xxxx 
Make Function-update behave like Function-value. This lets one use functions as 
adapters that translate update messages from a model to a dependant (view or other).
so one can customize the objects reaction update message. 
*/
+ Function {
	 
	update { arg theChanged, theChanger;
		^this.value(theChanged, theChanger);
	}

}
