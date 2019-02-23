// serialport setup
SerialPort.devicePattern = "/dev/tty.usbserial*"; // osx usb serial
SerialPort.devices;	// look if the device is there
(
~serial = SerialPort(
	SerialPort.devices.first,
	baudrate: 115200,
	crtscts: true
);
)


// minimal example
~serial.putAll("A120"); // red component
~serial.putAll("B255"); // green component
~serial.putAll("C100"); // blue component

// function example with values between 0..1
a = {|char, val, serial|
  serial.putAll(char ++ ((val*255).asInt + 1000).asString[1..3]);
}

// test it
a.("A", 0.1, ~serial)

// complete example
a = {|which, color, serial|
  // create an empty message
  var msg = "";

  // is there a color defined in the arguments?
  color = color ? Color.black;

  // convert color into an array
  color = color.asArray;

  // convert number of used orb into its corresponding ASCII character (determining colors)
  // convert light value [0..1] into a value between ["000" .. "255"]
  // collect al values in the message
  (65 + #[0, 1, 2] + (which * 3)).collect{|val, i|
    val.asAscii ++ ((color[i]*255).asInt + 1000).asString[1..3]
  }.do{|elem|
    msg = msg ++ elem;
  };

  // send it to the serial port
  serial.putAll(msg);
}