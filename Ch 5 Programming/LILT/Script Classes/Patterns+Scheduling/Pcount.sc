/* IZ 060305
Short for Pseries(1, 1, n);

a = Pcount(3).asStream;
{ a.next } ! 5;

a = Pcount(1).asStream;
{ a.next } ! 5;

a = Pcount(0).asStream;
{ a.next } ! 5;

*/

Pcount : Pattern {
	*new { | n = 1 |
		^Pseries(1, 1, n)
	}
}

