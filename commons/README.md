# Profiling Commons

## Configuration File Status Output (19.3.0+)

### Snippet: 

```
{
    "configuration" : {
        "status-file" : "/var/lib/<service>/configuration_status.json"
    }
}
```

### Effect:

If the status-file for the configuration is set, the configuration loader
will track the parse errors of each configuration file it loads and write
a status report into the configured file ("/var/lib/\<service\>/configuration_status.json"
in this case). The file has the following format:

```
{
    "files" : [
        {
            "name" : "/etc/<service>/config_file_1",
            "parse_error" : "Missing closing } at line 42"
        },
        {
            "name" : "/etc/<service>/config_file_2",
        }
    ]
}
```

The file without a parse error was considered and loaded correctly. The file
which couldn't be parsed has it's parse error noted in this file.

This file is intended to be parsed, e.g. by a status page to notify
configuration suppliers about the state of their logs.
