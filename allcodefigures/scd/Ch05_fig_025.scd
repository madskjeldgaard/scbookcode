Figure 5.25
_.isPrime ! 10
_.squared ! 10
_@_.(30, 40) // equivalent to: { | a, b | Point(a, b) }.value(30, 40)
Array.rand(12, 0, 1000).clump(4) collect: Rect(*_)
(1..8).collect([\a, \b, _]);
(a: _, b: _, c: _, d: _, e: _).(*Array.rand(5, 0, 100));

