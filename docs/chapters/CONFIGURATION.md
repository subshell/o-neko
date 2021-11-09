# Configuring O-Neko

O-Neko can be configured by providing properties in an `application.yml` file. The path to `application.yaml` is configurable:

* via the argument `--spring.config.location=file://<path>/application.yaml` 
* or by setting `SPRING_CONFIG_LOCATION`.

Alternatively all properties may be configured using UPPER-CASE environment variables (e.g `O_NEKO.ACTIVITY.CLEANUP.MAX_AGE_HOURS`). 

## O-Neko Specific Defaults

You may override the following default properties if necessary. 

```yaml
o-neko:
  activity:
    cleanup:
      maxAgeHours: 720
      schedulerIntervalMillis: 3600000
  deployments:
    lifetime:
      endOfDay:
        hour: 23
        minute: 59
        dayOffset: 0
      lastDayOfTheWeek: friday
```

## Deployment Lifetime 

Any project/version in O-Neko has a configurable deployment lifetime. This lifetime tells O-Neko when to stop a deployment. 
The settings `until tonight` and `until weekend` point to a specific time. To be a bit more flexible you can configure this point in time with `o-neko.deployments.lifetime.endOfDay`.
By default, it points to:

* `until tonight`: same day, 23:59
* `until weekend`: next friday, 23:59

By setting `o-neko.deployments.lifetime.endOfDay.dayOffset` to `1`, `o-neko.deployments.lifetime.endOfDay.hour` to `3`, and `o-neko.deployments.lifetime.lastDayOfTheWeek` to `saturday` it will point to:

* `until tonight`: next day, 03:59
* `until weekend`: next sunday, 03:59

As you can see, `until tonight` always points to the current day + `dayOffset`, while `until weekend` points to the configurable last day of the week + `dayOffset`. 

**Note:** Deployments will stop on the next *possible* expiration date. So if you start a deployment shortly after the `until tonight` expiration date, it will be stopped on the next day.