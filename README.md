# CamBot

Main component of this project is the application, that works as a bridge between a browser (or any other WebSocket client) and bluetooth adapter,
connected to microcontroller (tested with HC-05). Browser client sends car control commands and receives image stream back.

Android application pretends to be a Web server, that hosts robot control page accessible by device's IP address and WebSocket server, that browser connects to.

Described communication algorithm was designed to work within localhost only.

![SystemWorkScheme](https://user-images.githubusercontent.com/50966785/179052321-a409bd7d-1658-44e1-a5d6-d24bd9fd6707.jpg)
