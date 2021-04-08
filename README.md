# blackbox-analytics

## How to run it

Generate native-image binaries with sbt:
```
$ sbt nativeImage
```

Display the help by running:
```
$ blackbox-analytics --help
Usage: blackbox-analytics --window <seconds>

Analytics for blackbox processes

Options and flags:
    --help
        Display this help text.
    --window <seconds>, -w <seconds>
        Set the time-window size in seconds for grouping the event data
```

Pipe `blackbox` stdout to `blackbox-analytics` stdin:
```
$ blackbox.sh | blackbox-analytics -w 10
```

Navigate to http://localhost:8080/metrics to see the real-time metrics.
