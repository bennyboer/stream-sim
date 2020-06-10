# Stream Simulator

Below is a screenshot of the graphical user interface used primarily to build simulation scenarios that can easily be simulated using either the GUI or CLI (more performance!).

![Screenshot](docs/res/screenshot.png)

## Getting started

The application used Gradle as build tool.
Thus make sure to have `JDK 14` installed.
If you're using IntelliJ please set the JVM for Gradle to use in `Settings > Build, Execution, Deployment > Build Tools > Gradle` to 14.

The application consists of three Java modules in the folder `cli`, `ui` and `sim`:
- `sim`: The actual simulation implementation
- `ui`: Graphical interface for the simulation
- `cli`: Command line interface for the simulation

### Start the graphical user interface

For Windows call (Or select the task in the IntelliJ Gradle task list):

```
.\gradlew.bat :ui:run
```

For Linux call:

```
./gradlew :ui:run
```

### Start the command line interface

For Windows call (Or select the task in the IntelliJ Gradle task list):

```
.\gradlew.bat :cli:run --args="<PATH TO CONFIGURATION>"
```

For Linux call:

```
./gradlew :cli:run --args="<PATH TO CONFIGURATION>"
```

A lot of command line options are available which makes calls like the following possible. Check out the `--help` flag to see all available options.

```
.\gradlew.bat :cli:run --args="../analysis/config/rimea4/range/rimea4_range_5.json -l -lf='../analysis/logs/rimea4/range/range_5' --log-simulation-time-change-delay=100"
```


## Building an executable

### Building the graphical user interface

Call `./gradlew.bat :ui:jlink` from the command line to build an executable for **your current platform**.
An executable will appear under `/ui/build/image/bin` called `stream_sim`.

### Building the command line interface

Call `./gradlew.bat :cli:jlink` from the command line to build an executable for **your current platform**.
An executable will appear under `/cli/build/image/bin` called `stream_sim_cli`.
