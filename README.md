ODS Explorer
-------------

Simple desktop application for testing/demoing [libsensor](https://github.com/Danish-Sensor-Engineering/libsensor).


## Usage

On Linux your need permissions to access the serial port (eg. */dev/ttyUSB0*). On most Linux distribution your account needs to be a member of a group called *dialout* or a similar name.

    $ ls -l /dev/ttyUSB0
    crw-rw---- 1 root dialout 188, 0 Apr  6 12:42 /dev/ttyUSB0

## Development

To build, test and run locally

    ./gradlew build
    ./gradlew run

On Windows use ```gradlew.bat``` in a terminal, or use an IDE that supports Gradle projects.
