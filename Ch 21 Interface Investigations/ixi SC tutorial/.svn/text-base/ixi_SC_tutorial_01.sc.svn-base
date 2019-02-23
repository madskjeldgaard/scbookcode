

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 01 - Language Basics

// =====================================================================
// - ixi audio tutorial - www.ixi-audio.net
// =====================================================================


/*		
		---------------------------------------------------------------
 		Copyright (c) 2005-2007, ixi audio.
 		This work is licensed under a Creative Commons 
		Attribution-NonCommercial-ShareAlike 2.0 England & Wales License.
 		http://creativecommons.org/licenses/by-nc-sa/2.0/uk/
		---------------------------------------------------------------
*/



// ========== Contents of this tutorial ==========

//	0) The semicolon and running a program
// 	1) Comments, posting and help
// 	2) Variables
// 	3) Functions
// 	4) Lists and Arrays
//	5) Dataflow
//	6) Looping and iterating
//	7) Peaking under the hood
// 	8) Creating classes





// 0) ========= The semicolon and running a program ==========

// First of all. Most computer languages start counting at 0. So does SC

/*
The semicolon (;) is what divides one instruction from the next. 
It is a line of code. After the semicolon, the interpreter looks at next line.
There has to be semicolon after each line of code. Otherwise you get lots of errors.
*/


// double click behind the first bracket and the whole code is highlighted
// what is between the brackets is a program. Hit ENTER (not RETURN) to run it.
(
"you ran the program and ".post; (44+77).post; " is the sum of 44 + 77".postln;
"and this is the next line - the interpreter posts it twice as it's the last line of the program.".postln;
)

// the following will not work. Why not?
(
(44+77).postln
55.postln;
)
// note that the ¥ sign is where the interpreter finds the error

// you can also place a cursor somewhere on the line where the code is and hit ENTER
(44+77).postln; 55.postln; "this line is posted twice".postln;




// 1) ========= Comments, posting and help ==========


// this is a comment

/*
And this is 
also a comment
*/

// Comments are red by default (in the Format menu choose syntax colorize)


// We try the post window (the output from the interpreter):
// (place the cursor on the line and hit the ENTER button (not RETURN))


"hello".post; // post something

"hello there".postln; // post something and make a line break

1+4;


// you can also use postf:

// a postf example inside a function (we will learn about them later):
(
f = {|a, b| 	
	"the first value is %,  and the second one is  % \n".postf(a, b);
};

f.value(33,44);
f.(777,888) // here we use a shortcut and skip writing "value" - same as the line above
)

// NOTE:  the "|a,b|" is the same as writing "arg a, b";
// and "\n" means RETURN in Unix speak

// if you are posting a long list you might not get the whole list using .postln;
// for that use the following:

Post << "hey"

// example

Array.fill(1000, {100.rand}).postln; // you see you get ...etc...

// whereas

Post << Array.fill(1000, {100.rand}) // you get the whole list


// HELP

// if you want to read the help file for Array, highlight the word and hit 
// Apple + d (for the documentation file)

// Also, if you want to read and browse all the documentation, you can open a help browser:
Help.gui



// 2) ========= Variables ==========

// SuperCollider is not strongly typed so you don't need to declare the data type of variables
// (data types (in other languages) include : integer, float, double, string, custom objects, etc...

a = 3; // we assign the number 3 to the variable "a"
a = "hello"; // we can also assign a string to it.
a = 0.333312; // or a floating point number;
a = [1, 34, 55, 0.1, "string in a list", \symbol, pi]; // or an array

a // hit this line and we see in the post window what "a" contains


// local variables - they are declared inside the scope of the brackets or a function.
(
var v, a;
v = 22;
a = 33;
)

v // hit this line and watch the post window 
a // hit this line and watch the post window - still our old "a" from above


// so a is a global variable (in SC by default a to z can be global).
// a variable with the name "myvar" could not be global. - only single letters


// For that we need environmental variables (using the ~ sign):

~myvar = 333;

~myvar // post it;



// 3) ========= Functions ==========

// Functions in SuperCollider are within curly brackets {}

f = {44.postln};
f.value // to call the function we need to get its .value

f = {	arg a, b; // arguments are inputs into a function
		c = a+b; 
		("c equals : " + c).postln;
		c+c // and just to show that the function always returns the last line of code
	}

f.value(3,4) // f is now especting two arguments 

x = f.value(22,34);
x // we have now stored the last value of the function above in our x variable

// arguments can also be defined inside two "|"s - example:

f = {|string| string.postln;}

f.("hi there") // and you can skip the .value and write just a .




// 4) ========= Lists and Arrays ==========


a = [1,2,3,4,5,6]; // an array

// the same as above
a = Array.fill(6, {arg i; i+1}); 

a.do({arg i; (i*i).postln;}); // we can perform actions on the array

// let's look at a
a

// we can now scramble the list:
a.scramble

// but a is still the original a
a

// if you want to change "a" to the result of the operation, you have to assign it to "a"
a = a.scramble 	// an Array has many methods such as scramble

a = a.mirror 		// mirror

a = a.stutter 	// stutter

a = a.pyramid 	// pyramid

a // how does it look?

a.sort // sort it 


// we can get at elements within the array
a[1]

a[1] = [22,33,44,3] // or add elements to the array

a.add(44) // cannot add more than few times (arrays cannot grow infinitly) 

// instead use this:

a = a.add(44);

// or just use List (which can grow)

a = List.new;
a.add(333); // run this line many times
a.add(33)
a.add("ss")
a.add(\sgg)

a.insert(0, 2222)


// see: ArrayedCollection helpfile for methods of both Array and List

// .add, .insert, .scramble, etc. are METHODS of the Array Class.
// TIP: to view all methods of Array, do:

Array.dumpInterface

// to view all inherited methods (yes! object orientated, see wikipedia)

Array.dumpFullInterface



// 5) ========= Dataflow (if-else & case) ==========

/* 
All programming languages have different ways of controlling data.
The most basic (but sufficient) are if-statements and case-statements.
*/

// -> if(condition, {then do this}, {else do this});

// the Boolean values true and false are keywords in SuperCollider language:
// (check the class "Boolean") 
if(true, {"condition is TRUE".postln;}, {"condition is FALSE".postln;})
if(false, {"condition is TRUE".postln;}, {"condition is FALSE".postln;})

// so:
if(3==3, {"condition is TRUE".postln;}, {"condition is FALSE".postln;})
if(3==4, {"condition is TRUE".postln;}, {"condition is FALSE".postln;})

// a tautology:
true.if({"condition is TRUE".postln;}, {"condition is FALSE".postln;})
// same as:
if(true, {"condition is TRUE".postln;}, {"condition is FALSE".postln;})

// and here we have an array that has two items and chooses either of them
[true,false].choose.if({"condition is TRUE".postln;}, {"condition is FALSE".postln;})
// how would that look in the if syntax above?


/*
// comparison operators are: 
 == (equals)
 < (less than)
 > (more than)
 != (not equals)
 <= (more than or equals to)
 >= (less than or equals to)
*/

// try to run the following lines and watch the post window:
3==4
3!=4
3>=4
3<=4
3<=3

// Now let's make an array with all the prime numbers up to 10000

(
p = List.new;
10000.do({arg i; // i is the iteration from 0 to 10000
	if(i.isPrime, {p.add(i)}); // there is no else condition here, we don't need it
});
Post << p;
)

// i.isPrime means that it's checking if the number is prime or not (true or false)


// a case statement is the same as if, just more efficient if checking many conditions.

/*
example:

case
{condition} {action}
{condition} {action}
etc.
*/

(
a = 2;
case
{a == 1} {"a equals 1".postln;}
{a == 2} {"a equals 2".postln;}
{a == 3} {"a equals 3".postln;}
{a == 4} {"a equals 4".postln;};
)
// note the semicolon only after the last testing condition.
// (so the line evaluation goes from "case...... to that semicolon" )




// 6) ========= Looping and iterating ==========


// looping is also an important dataflow technique 

// the argument is the iteration for each loop repitition:
10.do({arg counter; counter.postln;});

// you can call it anything:
10.do({arg num; num.postln;});

// a convention is to use the character "i" (for iteration):
10.do({arg i; i.postln;});

// when looping trough lists/arrays, there are 2 arguments (as opposed to above)
// the item in the list and the counter;
[11,22,33,44,55,66,77,88,99].do({arg item, counter; counter.post; " : ".post; item.postln;})


// for is also a looping technique
for(100, 130, {|i| i = i+10; i.postln;})

// or forBy where there iteration is more than 1
forBy(100, 130, 4, {|i| i = i+10; i.postln;})

// while is another one
i = 1;
while ({ i < 30 }, {  i = i + 1; i.postln; });



// 7) ========= Peaking under the hood ==========

/*

Each UGen or Class in SuperCollider has a class definition in a class file.
These files are compiled ever time SuperCollider is started and become application
environment we are using. SC is an "interpreted" language. (As opposed to a "compiled"
language like C or Java).

- For checking the sourcefile, type Apple + Y (or cmd + Y) - SinOsc
- For checking the implementations of a method (which classes support it), type Apple + Y - poll
- For checking references to a method (which classes support it), type Shift + Apple + Y - poll


UGen.dumpSubclassList // UGen is a class. Try dumping LFSaw for example

UGen.browse  // examine methods interactively in a GUI (OSX)

SinOsc.dumpFullInterface  // list all methods for the classhierarchically
SinOsc.dumpMethodList  // list instance methods alphabetically
SinOsc.openHelpFile

*/


// 7) ========= Creating Classes ==========


// ====== here is the TestClass (save as TestClass.sc in the SCClassLib folder of SC ) ======



TestClass {
	
	classvar <>myvar; // classvariables
	var <>addnr, >addnrSet, <addnrGet; // instance variables
	// this is a normal constructor method
	*new { arg argaddnr; 
		^super.new.initTest(argaddnr) 
	}
	
	initTest { arg argaddnr;
		addnr = argaddnr ? 3;
	     // do initiation here
	}
	
	calc {arg a, b;
		var c;
		c = a+b;
		^c // return
	}

}

TestClass2 : TestClass {
	calc { arg a, b;
		var c;
		c = a * b + addnr;
		^c;
	}
	
	setAddNr_ { arg newnr;
		addnr = newnr;
	}
	
	getAddNr {
		^addnr;
	}
}

// ===========================================================================


// and here we test the class

t = TestClass.new

t.calc(3,4)


t = TestClass.new(9)

t.addnr


v = TestClass2.new

v.calc(3,4)

v.addnr_(55)
v.addnr // our new class
t.addnr // the other of course still is just 9

v.addnrSet = 33 // we can set this number (because of > (a setter) )
v.addnrSet_(33) // another way of setting a variable (same as = )


v.addnrGet = 33 // Wrong! we cannot set this number ( because it's a getter < )


// proper object orientated programming uses setter and getter methods 
// (rather than accessing variables directly)


// here we use the setAddNr_ method to set our variable.
v.setAddNr_(333)
// and we can look at it:
v.addnr 
// but should really look at it with the getter method we made:
v.getAddNr



// =============================== another test class ========================


SonicArtsClass {
	
	var win, textfield, textfield2, rect; // get text but set text2
	var name, <>profession; // a getter and setter variable
	var friends;

	*new { arg name, rect, color; 
		^super.new.initSAClass(name, rect, color);
		}
	
	initSAClass { arg argname, argrect, color;
		var scramblebutton;
		
		rect = argrect;
		name = argname;
		win = SCWindow(name, rect, resizable:false).front;
		win.view.background_(color);
		textfield = SCStaticText(win, Rect(10, (rect.height/2)-30, rect.width, 30));
		textfield.string_("");
		textfield.font_(Font("Helvetica-Bold", 24));
		textfield2 = SCStaticText(win, Rect(10, (rect.height/2)+30, rect.width, 30));
		textfield2.string_("");
		textfield2.font_(Font("Helvetica-Bold", 14));
		scramblebutton = SCButton(win, Rect(10,10, 200, 30))
						.states_([
							["change friends color",Color.black,Color.clear]]
						)
						.action_({
							friends.do({arg friend; friend.changeColor(Color.rand)});
						});

		friends = List.new;
	}
	
	speak_{arg string;
		textfield.string_(string);
	}

	speak2_{arg string;
		textfield2.string_(string);
	}
	
	updateGUI {
		win.refresh;
	}
	
	addFriend {arg friend;
		friends.add(friend);
	}
	
	getName {
		^name; // note the return symbol
	}
	
	setName_ {arg newname; // note the underscore used when you are setting
		name = newname;
	}
	
	removeFriend {arg friend;
		var friendindex;
		friendindex = friends.indexOfEqual(friend);
		friends.remove(friendindex);
	}
	
	showFriends {
		var namesOfFriends;
		namesOfFriends = List.new;
		friends.do({arg friend; namesOfFriends.add(friend.getName)});
		textfield2.string_(namesOfFriends.asString);
	}
	
	getFriends {
		^friends
	}
	
	getFriendNames {
		var namesOfFriends;
		namesOfFriends = List.new;
		friends.do({arg friend; namesOfFriends.add(friend.getName)});
		^namesOfFriends;
	}
	
	changeColor {arg color;
		win.view.background_(color);
		win.update;
	}
}



// ================== and here is some code to try the class


a = SonicArtsClass("john", Rect(50, 800, 300, 200), Color.red)
a.speak_("Hi! I'm John")
a.profession = "singer"
a.speak2_("I am a" + a.profession)

b = SonicArtsClass("george", Rect(350, 800, 300, 200), Color.blue)
b.speak_("Hi! I'm george")
b.profession = "bass player"
b.speak2_("I am a" + b.profession)

c = SonicArtsClass("paul", Rect(650, 800, 300, 200), Color.green)
c.speak_("Hi! I'm paul")
c.profession = "guitarist"
c.speak2_("I am a" + c.profession)

// let's fix the roles

b.profession = "guitarist"
b.speak2_("I am a" + b.profession)
c.profession = "bass player"
c.speak2_("I am a" + c.profession)

a.addFriend(b)
a.addFriend(c)
a.showFriends

b.showFriends
c.showFriends

b.addFriend(a)
b.addFriend(c)
b.showFriends // check his friends

// what if john wants to change his name?

a.setName_("ringo");
a.speak_("Hi! I'm"+a.getName)
// we can get the name like this
a.getName
// but not like this:
a.name
// however, we can get the profession like this
a.profession
// WHY?
// the reason is the < (get) and > (set) properties of the profession variable

