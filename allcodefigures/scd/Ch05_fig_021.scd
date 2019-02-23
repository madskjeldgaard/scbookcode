Figure 5.21
(
var window, button; 
window = Window("Server Button", Rect(400, 400, 200, 200));
button = Button(window, Rect(5, 5, 190, 190));
button.states = [["boot!"], ["quit!"]];
button.action = { |me| Server.default perform: [\quit, \boot][me.value] };
window.front;
)

