// Figure 23.8

p = LazyEnvir.push;
~a = ~b * ~c;
~a.value; 	// => nil
~b = Pseq([1, 2, 3]).asStream;
~c = 10;
~a.value; 	// => 10
~a.value; 	// => 20
~b = [1, 2, 3];
~a.value; 	// => [10, 20, 30];
~a.postcs;	// => Maybe((Maybe([ 1, 2, 3 ]) * Maybe(10)))
p.pop


