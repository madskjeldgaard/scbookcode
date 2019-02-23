// brainfuck, urban mueller 1993
// for its universality, see also http://www.hevanet.com/cristofd/brainfuck/utm.b
/*
>	Move the pointer to the right
<	Move the pointer to the left
+	Increment the memory cell under the pointer
-	Decrement the memory cell under the pointer
.	Output the character signified by the cell at the pointer
,	Input a character and store it in the cell at the pointer
[	Jump past the matching ] if the cell under the pointer is 0
]	Jump back to the matching [
*/

(
var findBrack = { |str, offset|
		var count = 0, i = offset;
		while {
			if(str[i] == $[ /*]*/) { count = count + 1 };
			if(str[i] == /*[*/$]) { count = count - 1 };
			i < str.size and: { count > 0 }
		} {
			i = i + 1;
		};
		if(i == str.size and: { count > 0 }) { nil } { i }	
};

e = (
	'>': { ~ptr = ~ptr + 1 },
	'<': { ~ptr = ~ptr - 1 },
	'+': { ~mem[~ptr] = ~mem[~ptr] + 1;  },
	'-': { ~mem[~ptr] = ~mem[~ptr] - 1; },
	'.': { ~mem[~ptr].asAscii.post; },
	',': { |char| ~mem[~ptr] = char.ascii },
	'['/*]*/ : {   
			
			if(~mem[~ptr] <= 0) {
				~codePtr = findBrack.(~code, ~codePtr);
				if(~codePtr.isNil) { Error("syntax error!").throw };
				
			};
			~stack = ~stack.add(~codePtr);
		},
	/*[*/']': {
		~codePtr = ~stack.pop;
		if(~codePtr.isNil) { Error("syntax error closing br!").throw } { ~codePtr = ~codePtr - 1 };
	}
);

x = (
	reg: e,
	memsize: 1024,
	codePtr: 0,
	
	interpret: { |x, code, verbose = false, dt = 0.01|
		x.use {
			if(dt.notNil) {
				fork { x.pr_interpret(code, verbose, dt) }
			} {
				x.pr_interpret(code, verbose);
			}
		};"";
	},
	pr_interpret: { |x, code, verbose = false, dt|
			var func, sym, count = 0, max = 1e5, overrun;
			
			~code = code;
			~codePtr = 0;
			~ptr = 0;
			~mem = 0.dup(~memsize);
			~stack = nil;
			
			while {
				sym = ~code[~codePtr].asSymbol;
				func = ~reg[sym];
				
				if(verbose) { 
					postf("\nptr: % char: % stack: %\nmem: % \n", 
						~codePtr, sym, ~stack, ~mem.join($ )) 
				};
				count = count + 1;
				overrun = dt.isNil and: { count >= max };
				if(overrun) { "time overrun (t > %).\n".format(max).warn };
				func.notNil and: { ~codePtr < ~code.size } and: { overrun.not }
			} {
				func.value;
				if(~ptr >= ~memsize) { "memory overrun (i > %).\n".format(~memsize).throw };
				~codePtr = ~codePtr + 1;
				if(dt.notNil) { dt.yield };
			};
			
	}

);

);


x.interpret("++++----", true, 0.4); 
x.interpret("----++++", true, 0.4);
x.interpret(join($+ ! 100) ++ ".+.+.+.", false, 0.4);

x.interpret("++>+++><---<--", true);
x.interpret("+>+>>+>+>>+", true);

x.interpret("[+]>+", true); // jump first to +>

x.interpret("+[-]", true); // go in first first, then jump out
x.interpret("+[-]", true); // go in first first, then jump out


x.interpret("+[+-]+++", true);

x.interpret("++[++-.]+");

// hello world
(
x.interpret(">+++++++++[<++++++++>-]<.>+++++++[<++++>-]<+.+++++++..+++.>>>++++++++[<++++>-]
<.>>>++++++++++[<+++++++++>-]<---.<<<<.+++.------.--------.>>+."); "";
)




// quines????
// takes long..
(
x.interpret("->++>+++>+>+>++>>+>+>+++>>+>+>++>+++>+++>+>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>+>+>++>>>+++>>>>>+++>+>>>>>>>>>>>>>>>>>>>>>>+++>>>>>>>++>+++>+++>+>>+++>>>+++>+>+++>+>++>+++>>>+>+>+>+>++>+++>+>+>>+++>>>>>>>+>+>>>+>+>++>+++>+++>+>>+++>+++>+>+++>+>++>+++>++>>+>+>++>+++>+>+>>+++>>>+++>+>>>++>+++>+++>+>>+++>>>+++>+>+++>+>>+++>>+++>>+[[>>+[>]+>+[<]<-]>>[>]<+<+++[<]<<+]>>+[>]+++[++++++++++>++[-<++++++++++++++++>]<.<-<]", false, 0.001); 
)

(
x.interpret(">+++++>+++>+++>+++++>+++>+++>+++++>++++++>+>++>+++>++++>++++>+++>+++>+++++>+>+>++++>+++++++>+>+++++>+>+>+++++>++++++>+++>+++>++>+>+>++++>++++++>++++>++++>+++>+++++>+++>+++>++++>++>+>+>+>+>++>++>++>+>+>++>+>+>++++++>++++++>+>+>++++++>++++++>+>+>+>+++++>++++++>+>+++++>+++>+++>++++>++>+>+>++>+>+>++>++>+>+>++>++>+>+>+>+>++>+>+>+>++++>++>++>+>+++++>++++++>+++>+++>+++>+++>+++>+++>++>+>+>+>+>++>+>+>++++>+++>+++>+++>+++++>+>+++++>++++++>+>+>+>++>+++>+++>+++++++>+++>++++>+>++>+>+++++++>++++++>+>+++++>++++++>+++>+++>++>++>++>++>++>++>+>++>++>++>++>++>++>++>++>++>+>++++>++>++>++>++>++>++>++>+++++>++++++>++++>+++>+++++>++++++>++++>+++>+++>++++>+>+>+>+>+++++>+++>+++++>++++++>+++>+++>+++>++>+>+>+>++++>++++[[>>>+<<<-]<]>>>>[<<[-]<[-]+++++++[>+++++++++>++++++<<-]>-.>+>[<.<<+>>>-]>]<<<[>>+>>>>+<<<<<<-]>++[>>>+>>>>++>>++>>+>>+[<<]>-]>>>-->>-->>+>>+++>>>>+[<<]<[[-[>>+<<-]>>]>.[>>]<<[[<+>-]<<]<<]");
);
