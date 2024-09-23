DSE Demo Application
--------------------

Small desktop application for demoing our products.

![Alt text](doc/screenshot.png?raw=true "DSE Demo")

## Download

Find the [latest release](https://github.com/Danish-Sensor-Engineering/dse-demo/releases/latest) for your operating-system and download installer.

## Notes

Plug-in the sensor before starting the application.


### Windows

Installer is not signed, so Windows will give you warnings. 
The Demo application might need to run as Administrator, to get access to the serial ports.


### Linux

On Linux your need permissions to access the serial port (eg. */dev/ttyUSB0*). On most Linux distribution your account needs to be a member of a group called *dialout* or a similar name.

```shell    
$ ls -l /dev/ttyUSB0
crw-rw---- 1 root dialout 188, 0 Apr  6 12:42 /dev/ttyUSB0
```


## Development

Java SDK version 21 with JavaFX is required.

Information on how to build and package dse-demo:

```shell
./gradlew build jpackage
```

### Windows

Download and install:

- Microsoft .NET Framework 3.5
- Wix Toolset v3 (3.11.2 or later)


### Linux

On Debian/Ubuntu:

- ```apt install dpkg-dev rpm```


### MacOS

Install xcode command line tools:

- ```xcode-select --install```

