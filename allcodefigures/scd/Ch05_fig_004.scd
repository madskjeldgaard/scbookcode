Figure 5.4
((1 + 2).asString).interpret 		// = 3
"1" ++ "2". interpret			// 12: 2 is translated to string by ++
("1" ++ "2").interpret			// 12
(1.asString ++ 2.asString).interpret 	// 12
"1+2". interpret 				// 3
(1.asString ++ "+2"). interpret		// 3
(1 + 2).interpret				// error: interpret not understood by Integer 3


