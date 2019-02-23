/* IZ 2007-01-20 {
Utility for reporting stuff from within a method, giving the methods name and the classes name.

thisMethod.report("some stuff taking place here is:", 2, "hello", path = "xxx");
} */

+ Method {
	report { | ... stuff |
		Post << ownerClass.name << ":" << name << " : " <<* stuff << "\n";
	}
}