ODS Explorer
-------------

Desktop application for testing/demoing our [libsensor](https://github.com/Danish-Sensor-Engineering/libsensor) Java library for communicating with our Optical Displacement Sensors.

![Alt text](doc/screenshot.png?raw=true "ODS Explorer")

## Download

Find the [latest release](https://github.com/Danish-Sensor-Engineering/explorer/releases/latest) for your operating-system and download installer.

## Notes


### Linux

On Linux your need permissions to access the serial port (eg. */dev/ttyUSB0*). On most Linux distribution your account needs to be a member of a group called *dialout* or a similar name.

    $ ls -l /dev/ttyUSB0
    crw-rw---- 1 root dialout 188, 0 Apr  6 12:42 /dev/ttyUSB0
