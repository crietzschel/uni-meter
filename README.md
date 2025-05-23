# uni-meter

uni-meter is a small tool that emulates a Shelly Pro3EM device for the usage with the Hoymiles MS-A2, the Marstek
Venus storage and the EZHI hybrid inverter. It is not a full implementation of a Shelly Pro3EM device, currently only 
the parts that are needed by the storages are implemented.

The real electrical meter data currently can be gathered from the following devices:

- Fronius smart meter
- Generic HTTP (configurable HTTP interface, usable for many devices)
- Home Assistant sensors
- ioBroker datapoints (via simple API adapter)
- MQTT
- Shelly 3EM
- SHRDZM smartmeter interface module (UDP)
- SMA energy meter / Sunny Home Manager (UDP protocol)
- SMD120 modbus energy meter (via Protos PE11) (SMD630 could be added, I have no test device)
- Solaredge
- Tasmota IR read head (via HTTP)
- Tibber Pulse (local API) 
- VzLogger webserver

The idea is to further enhance the tool in the future by adding more input and output devices to get a universal 
converter between different electrical meters, inverters and storage systems.

## Download

The release versions can be downloaded from the  [GitHub releases](https://github.com/sdeigm/uni-meter/releases).
You just have to download the `uni-meter-<version>.tgz` archive. 

## Building

The project can be build using Java 17 and Maven 3.8.5 or later by simply typing

```shell
mvn install
```

within the project's root directory. 

Afterward you will find a `uni-meter-<version>.tgz` archive in the `target` 
directory which can be used to deploy the tool to the target system which most likely is a Raspberry Pi.

Since some people asked, it is not necessary to build the tool yourself. For a normal user, that does not want to
modify the source code, the provided prebuild releases are sufficient.

## Preliminary remarks

Not all the steps mentioned in this documentation are necessary for every setup. A lot of configuration options like
throttling of the sampling frequency and default power values are optional and not necessary for most of the users. Only
for fine-tuning the system, these options may be specified.

For a Marstek Venus storage you just have to enable JSON RPC over UDP port 1010. It is not necessary to change the
device id of the virtual Shelly and announce the tool via mDNS using the Avahi daemon. These steps can just be skipped.

For a Hoymiles MS-A2 storage you have to announce the tool via mDNS using the Avahi daemon. You also have to change the
device id of the virtual Shelly to a unique value. Otherwise, the Hoymiles app refuses to attach the Shelly to your
Hoymiles system. Enabling JSON RPC over UDP is not necessary for the Hoymiles storage.

If you want to use an ESP32 or a similar system which does not support Java 17, there is another project called
[Energy2Shelly](https://github.com/TheRealMoeder/Energy2Shelly_ESP) which is written in C++ and can be used as an 
alternative on such systems.

## Installation

To install the tool on a Raspberry Pi, you need to have a Java 17 runtime installed. To check if this is the case, type

```shell
java -version
```

If the output shows a version 17 or later, you are good to go. If not, you can install the OpenJDK 17 using the following
commands:

```shell    
sudo apt update
sudo apt install openjdk-17-jre
```

Afterward, you can copy the `uni-meter-<version>.tgz` archive to the Raspberry Pi. In theory, you can extract the archive
to any location you like, but all the scripts and configuration files included assume an installation in the `/opt` 
directory. So preferably you should extract it to the `/opt` directory using the following commands:

```shell
sudo tar xzvf uni-meter-<version>.tgz -C /opt
```
```shell
sudo ln -s /opt/uni-meter-<version> /opt/uni-meter
```

## Announcing the tool via mDNS

To make the tool discoverable by the Hoymiles storage via mDNS, the `avahi-daemon` is used. On recent Raspbian versions,
the `avahi-daemon` is already installed and running. To check if this is the case, type

```shell
sudo systemctl status avahi-daemon
```

If you see an output like `active (running)`, you are good to go. If not, you can install the `avahi-daemon` using the

```shell
sudo apt install avahi-daemon
```

and enable it using the following command:

```shell
sudo systemctl enable avahi-daemon
sudo systemctl start avahi-daemon
```

Starting with `uni-meter` version 1.1.5, it is now not necessary anymore to provide the avahi services files manually.
The `uni-meter` will create all necessary files automatically and delete them when stopped again. The old services 
files that had been installed manually can be deleted from the `/etc/avahi/service`

The provided service files announce the Shelly Pro3EM emulator running on port 80. Even if I used another port within the
service file, the Hoymiles storage does not seem to evaluate the port information and still connects to port 80. Maybe
future Hoymiles firmware version also will honor the port information.

In the meantime this has the disadvantage that the `uni-meter` tool has to bind to port 80 which requires root privileges.
Using `nginx` or using `setcap` to run the tool as a non-root user is possible but not covered in this documentation. 

## Configuration

The configuration is done using a configuration file in the [HOCON format](https://github.com/lightbend/config/blob/main/HOCON.md). 

The provided start script assumes the configuration file to be located in the `/etc` directory. To do so, copy the
provided configuration file to that location using:

```shell
sudo cp /opt/uni-meter/config/uni-meter.conf /etc/uni-meter.conf
```

Then use your favorite editor to adjust the configuration file to your needs as described in the following sections.

## Using the Home Assistant addon

1. **Add this github repository to Home Assistant**

[![Open your Home Assistant instance and show the add add-on repository dialog with a specific repository URL pre-filled.](https://my.home-assistant.io/badges/supervisor_add_addon_repository.svg)](https://my.home-assistant.io/redirect/supervisor_add_addon_repository/?repository_url=https%3A%2F%2Fgithub.com%2Fsdeigm%2Funi-meter)

2. **Install the addon in Home Assistant**
- After refreshing your browser window, there should be a ``uni-meter`` addon which can be installed 
 
3. **Configure the uni-meter**
- Create a ``uni-meter.conf`` configuration in the ``/addon_configs/663b81ce_uni_meter`` directory of your Home Assistant instance and configure the input and output devices as needed
5. **mDNS support (only necessary when using the Hoymiles storage)**
- If the virtual shelly shall be announced via mDNS from the Home Assistant instance, you have to install the Pyscript addon
- If that is done, copy the provided [uni-meter-mdns.py](https://github.com/sdeigm/uni-meter/blob/main/ha_addon/uni-meter-mdns.py) into the `/config/pyscript` directory on your instance
- When the `uni-meter` finds this script, it will automatically announce the virtual shelly via mDNS without further configuration
4. **Start the uni-meter addon**


## Configuring the output device

### Configuring the Shelly device id

Starting from version 1.1.5 on it is not necessary anymore to configure the Shelly device id. It will be automatically
set based on the first detected hardware mac address on the host machine. 

For older versions it had been necessary to configure the device id as described below. These configuration can now be
removed.

```hocon
uni-meter {
  # ...
  output-devices {
    shelly-pro3em {
      device {
        mac = "B827EB364242"
        hostname = "shellypro3em-b827eb364242"
      }
    }
  }
  #...
}
```

### Enabling JSON RPC over UDP

As a default the JSON RPC over UDP interface of the Shelly Pro3EM emulator is disabled. To enable it, configure the 
`udp-port` and optionally the `udp-interface` in the `/etc/uni-meter.conf` file:

```hocon
uni-meter {
  # ...
  output-devices {
    shelly-pro3em {
      #...
      udp-port = 1010
      udp-interface = "0.0.0.0" # default, can be omitted
      #...
    }
  }
  #...
}
```  

### Throttling the sampling frequency of the Shelly device

In some setups with a higher latency until the real electrical meter readings are available on the output side, it might
be necessary to throttle the sampling frequency of the output data. Otherwise, it might be possible that the storage
oversteer the power production and consumption values and that they are fluctuating too much around 0 (see the comments
and findings to this [issue](https://github.com/sdeigm/uni-meter/issues/12)).

To throttle the sampling frequency you can configure a `min-sample-period` in the `/etc/uni-meter.conf` file. This 
configuration value specifies the minimum time until the next output data is delivered to the storage.

```hocon
uni-meter {
  #...
  output-devices {
    shelly-pro3em {
      #...
      min-sample-period = 5000ms
      #...
    }
  }
  #...
}
```

### Configuring a static power offset

In some setups, it might be necessary to add a static offset to the power values. This can be the case if the real
electrical meter readings are not 100% accurate to your household's electrical meter readings.

You can either configure a power offset for the single phases or a total power offset. The phase power offsets take
precedence over the total power offset. If at least one phase power offset is configured, the total power offset is
ignored.

Setting the power offset is done in the `/etc/uni-meter.conf` file:

```hocon
uni-meter {
  #...
  output-devices {
    #...
    shelly-pro3em {
      #...
      power-offset-total =0
      
      power-offset-l1 = 0
      power-offset-l2 = 0
      power-offset-l3 = 0
    }
  }
}
```

### Configuring a default power value

If the physical input device is not reachable and no power values are available for a certain time, the uni-meter will 
fall back to default power values. Without any configuration that happens after one minute and the power will default to 
0 watts. If you need a different behaviour, you can configure this using the following configuration below. Single phase
power values take precedence over the total power value. If at least one phase power value is configured, the total power
value is ignored.

```hocon
uni-meter {
  #...
  output-devices {
    #...
    shelly-pro3em {
      #...
      
      # These are the defaults used without any configuration: 
      forget-interval = 1m

      default-power-total = 0

      default-power-l1 = 0
      default-power-l2 = 0
      default-power-l3 = 0
    }
  }
}
```

### Changing the HTTP server port

In its default configuration, the uni-meter listens on port 80 for incoming HTTP requests. That port can be changed to
for instance port 4711 by adding the following parts to your `/etc/uni-meter.conf` file:

```hocon
uni-meter {
  # ...
  http-server {
    port = 4711
  }
  
  output-devices {
    shelly-pro3em {
      # ...
      port = 4711
    }
  }
}
```

## Configuring the input sources

### Using a Fronius smart meter as input source

A Fronius smart meter can be accessed using the generic-http input source. To access the Fronius smart meter, use the
following configuration in the `/etc/uni-meter.conf` file:

```hocon
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"

  input = "uni-meter.input-devices.generic-http"

  input-devices {
    generic-http {
      url = "http://192.168.x.x/solar_api/v1/GetMeterRealtimeData.cgi?Scope=System"

      power-phase-mode = "tri-phase"

      channels = [{
        type = "json"
        channel = "power-l1"
        json-path = "$.Body.Data.0.PowerReal_P_Phase_1"
      },{
        type = "json"
        channel = "power-l2"
        json-path = "$.Body.Data.0.PowerReal_P_Phase_2"
      },{
        type = "json"
        channel = "power-l3"
        json-path = "$.Body.Data.0.PowerReal_P_Phase_3"
      }]
    }
  }
}
```

### Using the generic HTTP input source

The generic HTTP input source can be used to gather the electrical meter data from any device providing the data via
an HTTP get request as JSON value. You have to configure the complete URL where the data is gathered from, including
the entire path and query parameters.

The input source can operate in two modes. Either in a `mono-phase` mode, where the power and/or the energy data is
provided as a single value for all three phases, or in a `tri-phase` mode, where the power and/or the energy data is
provided as a separate value for each phase.

The input data is gathered through channels. The following channels exist for the different energy and power phase modes:
* Power `mono-phase`
  * `power-total` - total current power
* Power `tri-phase`
  * `power-l1` - current power phase 1
  * `power-l2` - current power phase 2
  * `power-l3` - current power phase 3
* Energy `mono-phase`
  * `energy-consumption-total` - total energy consumption
  * `energy-production-total` - total energy production
* Energy `tri-phase`
  * `energy-consumption-l1` - energy consumption phase 1
  * `energy-consumption-l2` - energy consumption phase 2
  * `energy-consumption-l3` - energy consumption phase 3
  * `energy-production-l1` - energy production phase 1
  * `energy-production-l2` - energy production phase 2
  * `energy-production-l3` - energy production phase 3

For each channel to be read, you have to configure where the data is gathered from and what type it is. Currently only 
the `json` type is supported, but in the future, other types might be added. 

For channels in JSON format, an additional JSON path has to be provided which specifies which part of the JSON data
contains the actual value.

Additionally, each channel has a `scale` property which can be used to scale the data. The default scale is 1.0 and can
be omitted.

So a `/etc/uni-meter.conf` file reading the data from a VzLogger webserver could look like this:

```hocon    
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"

  input = "uni-meter.input-devices.generic-http"

  input-devices {
    generic-http {
      url = "http://vzlogger-server:8088"
      #username = "username"
      #password = "password"

      power-phase-mode = "mono-phase"
      energy-phase-mode = "mono-phase"

      channels = [{
        type = "json"
        channel = "energy-consumption-total"
        json-path = "$.data[0].tuples[0][1]"
        scale = 0.001
      },{
        type = "json"
        channel = "energy-production-total"
        json-path = "$.data[1].tuples[0][1]"
        scale = 0.001
      },{
        type = "json"
        channel = "power-total"
        json-path = "$.data[2].tuples[0][1]"
      }]
    }
  }
}
```
### Using Home Assistant sensors as input source

Home Assistant sensors can be used as input source. To access the sensors you have to create an access token in
Home Assistant and configure the uni-meter with that ``access-token`` and the URL of your system.

Also with this input, the input source can operate in two modes. 
Either in a `mono-phase` mode, where the power and/or the energy data is provided as a single value for all three 
phases, or in a `tri-phase` mode, where the power and/or the energy data is provided as a separate value for each phase.

Depending on the chosen phase mode the sensors to be used can be configured by the following properties:

* Power `mono-phase`
  * `power-sensor` - sensor providing the total current power
* Power `tri-phase`
  * `power-l1-sensor` - sensor providing current power phase 1
  * `power-l2-sensor` - sensor providing current power phase 2
  * `power-l3-sensor` - sensor providing current power phase 3
* Energy `mono-phase`
  * `energy-consumption-sensor` - sensor providing total energy consumption
  * `energy-production-sensor` - sensor providing total energy production
* Energy `tri-phase`
  * `energy-consumption-l1-sensor` - sensor providing energy consumption phase 1
  * `energy-consumption-l2-sensor` - sensor providing energy consumption phase 2
  * `energy-consumption-l3-sensor` - sensor providing energy consumption phase 3
  * `energy-production-l1-sensor` - sensor providing energy production phase 1
  * `energy-production-l2-sensor` - sensor providing energy production phase 2
  * `energy-production-l3-sensor` - sensor providing energy production phase 3

If you have a setup where the power values are split up between power production and power consumption, you can 
additionally specify the sensors for the production.

* Power `mono-phase`
  * `power-production-sensor` - sensor providing the current production power
* Power `tri-phase`
  * `power-production-l1-sensor` - sensor providing current production power phase 1
  * `power-production-l2-sensor` - sensor providing current production power phase 2
  * `power-production-l3-sensor` - sensor providing current production power phase 3

The current power values are then calculated as

  ``current power = power-sensor - power-production-sensor``

or 
 
  ``current power Lx = power-Lx-sensor - power-production-lx-sensor``

So the simplest `/etc/uni-meter.conf` file reading the data from a Home Assistant sensors could look like this:

```hocon
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"

  input = "uni-meter.input-devices.home-assistant"

  input-devices {
    home-assistant {
      url = "http://192.168.178.51:8123"
      access-token = "eyJhbGciOiJIUzI1Ni...."

      power-phase-mode = "mono-phase"
      energy-phase-mode = "mono-phase"

      power-sensor = "sensor.current_power"
      energy-consumption-sensor = "sensor.energy_imported"
      energy-production-sensor = "sensor.energy_exported"
    }
  }
}
```



### Using ioBroker as input source

Reading ioBroker datapoints as input source can be done using the generic http interface on uni-meter side and using
the [simpleAPI](https://github.com/ioBroker/ioBroker.simple-api) adapter on ioBroker side. When the simpleAPI adapter
is installed and configured on the ioBroker, you can use the following configuration in the `/etc/uni-meter.conf` file:

```hocon
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"

  input = "uni-meter.input-devices.generic-http"

  input-devices {
    generic-http {
      # Adjust the IP address of the ioBroker and the datapoints to read according to your needs
      url = "http://192.168.x.x:8082/getBulk/smartmeter.0.1-0:1_8_0__255.value,smartmeter.0.1-0:2_8_0__255.value,smartmeter.0.1-0:16_7_0__255.value/?json"

      # sample ioBroker output: [
      #  {"id":"smartmeter.0.1-0:1_8_0__255.value","val":16464.7379,"ts":1740054549023,"ack":true},
      #  {"id":"smartmeter.0.1-0:2_8_0__255.value","val":16808.0592,"ts":1740054549029,"ack":true},
      #  {"id":"smartmeter.0.1-0:16_7_0__255.value","val":4.9,"ts":1740054549072,"ack":true}
      # ]

      power-phase-mode = "mono-phase"
      energy-phase-mode = "mono-phase"

      channels = [{
        type = "json"
        channel = "energy-consumption-total"
        json-path = "$[0].val"
        scale = 0.001
      },{
        type = "json"
        channel = "energy-production-total"
        json-path = "$[1].val"
        scale = 0.001
      },{
        type = "json"
        channel = "power-total"
        json-path = "$[2].val"
      }]
    }
  }
}
```

### Using MQTT as input source

The MQTT input source can operate in two modes. Either in a `mono-phase` mode, where the power and/or the energy data is
provided as a single value for all three phases, or in a `tri-phase` mode, where the power and/or the energy data is
provided as a separate value for each phase. 

The input data is gathered through channels. Each channel has a unique identifier and a topic where the data is gathered
from. The following channels exist for the different energy and power phase modes:
* Power `mono-phase`
    * `power-total` - total current power
* Power `tri-phase`
    * `power-l1` - current power phase 1
    * `power-l2` - current power phase 2
    * `power-l3` - current power phase 3
* Energy `mono-phase`
    * `energy-consumption-total` - total energy consumption 
    * `energy-production-total` - total energy production
* Energy `tri-phase`
    * `energy-consumption-l1` - energy consumption phase 1
    * `energy-consumption-l2` - energy consumption phase 2
    * `energy-consumption-l3` - energy consumption phase 3
    * `energy-production-l1` - energy production phase 1
    * `energy-production-l2` - energy production phase 2
    * `energy-production-l3` - energy production phase 3 

Each channel is linked to a topic where the data is gathered from and has a type which specifies how the data is
stored within the MQTT topic. Currently, two types are supported: `value` and `json`. Use the `value` type for data 
stored as a number string within the topic. Use the `json` type for data stored as JSON within the topic. For
channels in JSON format, an additional JSON path has to be provided which specifies which part of the JSON data
contains the actual value.

Additionally, each channel has a `scale` property which can be used to scale the data. The default scale is 1.0 and can
be omitted.

So a `/etc/uni-meter.conf` file for a MQTT input source could look like this:

```hocon    
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"

  input = "uni-meter.input-devices.mqtt"

  input-devices {
    mqtt {
      url = "tcp://127.0.0.1:1883"
      #username = "username"
      #password = "password"

      power-phase-mode = "mono-phase"
      energy-phase-mode = "mono-phase"

      channels = [{
        type = "json"
        topic = "tele/smlreader/SENSOR"
        channel = "power-total"
        json-path = "$..power"
        scale = 1.0 # default, can be omitted
      },{
        type = "json"
        topic = "tele/smlreader/SENSOR"
        channel = "energy-consumption-total"
        json-path = "$..counter_pos"
      },{
        type = "json"
        topic = "tele/smlreader/SENSOR"
        channel = "energy-production-total"
        json-path = "$..counter_neg"
      }]
    }
  }
}
```

### Using Shelly 3EM as input source

To use a Shelly 3EM as an input source, set up the `/etc/uni-meter.conf` file as follows

```hocon
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"
  
  input = "uni-meter.input-devices.shelly-3em"

  input-devices {
    shelly-3em {
      url = "<shelly-3em-url>"
    }
  }
}
```

Replace the `<shelly-3em-url>` placeholder with the actual URL of your Shelly 3EM device.

This input device is currently totally untested, as I have no test device. If you have a Shelly 3EM device, please provide
feedback if it works or not via the GitHub issues.

### Using SHRDZM smartmeter interface as input source

To use a SHRDZM smartmeter interface providing the smart meter readings via UDP, set up the `/etc/uni-meter.conf` file
as follows

```hocon
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"
  
  input = "uni-meter.input-devices.shrdzm"

  input-devices {
    shrdzm {
      port = 9522
      interface = "0.0.0.0"
    }
  }
}
```

The above configuration shows the default values for the ShrDzm device which are used, if nothing is provided. If you
want to use a different port or interface, you have to adjust the values accordingly.

### Using SMA energy meter as input source

To use a SMA energy meter or a Sunny Home Manager as an input source, set up the `/etc/uni-meter.conf` file as follows

```hocon
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"
  
  input = "uni-meter.input-devices.sma-energy-meter"

  input-devices {
    sma-energy-meter {
      port = 9522   
      group = "239.12.255.254"
      //susy-id = 270  
      //serial-number = 1234567
      network-interfaces =[
        "eth0"
        "wlan0"
        // "192.168.178.222"
      ]
    }
  }
}
```

The above configuration shows the default values which are used, if nothing is provided. If your `port` and `group` are
different, you have to adjust the values accordingly.

If no `susy-id` and `serial-number` are provided, the first detected device will be used. Otherwise, provide the values
of the device you want to use.

The network interfaces to use are provided as a list of strings. Either specify the names or the IP addresses of the 
interfaces you want to use.

### Using SMD120 modbus energy meter as input source

To use a SMD120 modbus energy meter via a Protos PE11 as an input source, set up the `/etc/uni-meter.conf` file as 
follows:

```hocon
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"
  
  input = "uni-meter.input-devices.smd120"

  input-devices {
    smd120 {
      port = 8899
    }
  }
}
```

### Using Solaredge electrical meter input source

To use a Solaredge electrical meter as an input source, set up the `/etc/uni-meter.conf` file as
follows:

```hocon
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"

  input = "uni-meter.input-devices.solaredge"
  
  input-devices {   
    solaredge {
      address = "192.168.178.125"
      port = 502
      unit-id = 1
    }
  }  
}
```

### Using Tasmota IR read head as input source

To use a Tasmota IR read head as an input source, set up the `/etc/uni-meter.conf` file as follows:

```hocon
uni-meter {
  uni-meter {
    output = "uni-meter.output-devices.shelly-pro3em"

    input = "uni-meter.input-devices.tasmota"

    input-devices {
      tasmota {
        url = "http://<tasmota-ir-read-head-ip>"
        # username=""
        # password=""
        power-json-path = "$..curr_w"
        power-scale = 1.0 # default, can be omitted
        energy-consumption-json-path = "$..total_kwh"
        energy-consumption-scale = 1.0 # default, can be omitted
        energy-production-json-path = "$..export_total_kwh"
        energy-production-scale = 1.0 # default, can be omitted
      }
    }
  }
}
```

Replace the `<tasmota-ir-read-head-ip>` placeholder with the actual IP address of your Tasmota IR read head device.
If you have set a username and password for the device, you have to provide them as well.

Additionally, you have to configure the JSON paths for the power, energy consumption and energy production values to
access the actual values within the JSON data. If you have to scale these values, you can provide a scale factor which
is 1.0 as a default.

### Using Tibber Pulse as input source

The Tibber Pulse local API can be used as an input source. To use this API, the local HTTP server has to be enabled on 
the Pulse bridge. How this can be done is described for instance here 
[marq24/ha-tibber-pulse-local](https://github.com/marq24/ha-tibber-pulse-local).

If this API is enabled on your Tibber bridge, you should set up the `/etc/uni-meter.conf` file as follows

```hocon
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"
  
  input = "uni-meter.input-devices.tibber-pulse"

  input-devices {
    tibber-pulse {
      url = "<tibber-device-url>"
      node-id = 1
      user-id = "admin"
      password = "<tibber-device-password>"
    }
  }
}
```

Replace the `<tibber-device-url>` and `<tibber-device-password>` placeholders with the actual values from your environment.  
The `node-id` and `user-id` are optional and can be omitted if the default values from above are correct. Otherwise,
adjust the values accordingly.

### Using VzLogger webserver as input source

To use the VzLogger webserver as an input source set up the `/etc/uni-meter.conf` file as follows and replace the
`<vzlogger-host>` and `<vzlogger-port>` placeholders with the actual host and port of your VzLogger webserver.
Additionally provide the channel UUIDs of your system.

```hocon
uni-meter {
  output = "uni-meter.output-devices.shelly-pro3em"

  input = "uni-meter.input-devices.vz-logger"

  input-devices {
    vz-logger {
      url = "http://<vzlogger-host>:<vzlogger-port>"
      energy-consumption-channel = "5478b110-b577-11ec-873f-179XXXXXXXX"
      energy-production-channel = "6fda4300-b577-11ec-8636-7348XXXXXXXX"
      power-channel = "e172f5b5-76cd-42da-abcc-effeXXXXXXXX"
    }
  }
}
```

You will find that information in the VzLogger configuration file. As a
default, the VzLogger is configured in the `/etc/vzlogger.conf` file. Make sure that the VzLogger provides its
readings as Web service and extract the needed information from that file:

```hocon
{
  // ...
    
  // Build-in HTTP server
  "local": {
    "enabled": true,    // This has to be enabled to provide the readings via HTTP
    "port": 8088,       // Port used by the HTTP server
    
    // ... 
  }
  // ...  
    
  "meters": [
    {
      // ...
    
      "channels": [{
        "uuid" : "5478b110-b577-11ec-873f-179bXXXXXXXX",  // UUID of the energy consumption channel
        "middleware" : "http://localhost/middleware.php",
        "identifier" : "1-0:1.8.0", // 1.8.0 is the energy consumption channel
        "aggmode" : "MAX"
      },{
        "uuid" : "6fda4300-b577-11ec-8636-7348XXXXXXXX",  // UUID of the energy production channel
        "middleware" : "http://localhost/middleware.php",
        "identifier" : "1-0:2.8.0", // 2.8.0 is the energy production channel
        "aggmode" : "MAX"
      },{
        "uuid" : "e172f5b5-76cd-42da-abcc-effef3b895b2", // UUID of the power channel
        "middleware" : "http://localhost/middleware.php",
        "identifier" : "1-0:16.7.0", // 16.7.0 is the power channel
      }]
    }
  ]
}
````

### First test

After you have adjusted the configuration file, you can start the tool using command

```shell
sudo /opt/uni-meter/bin/uni-meter.sh
```

If everything is set up correctly, the tool should start up, and you should see an output like

```shell
24-12-04 07:29:08.006 INFO  uni-meter                - ##################################################################
24-12-04 07:29:08.030 INFO  uni-meter                - # Universal electric meter converter 0.9.0 (2024-12-04 04:58:16) #
24-12-04 07:29:08.031 INFO  uni-meter                - ##################################################################
24-12-04 07:29:08.033 INFO  uni-meter                - initializing actor system
24-12-04 07:29:10.902 INFO  org.apache.pekko.event.slf4j.Slf4jLogger - Slf4jLogger started
24-12-04 07:29:11.707 INFO  uni-meter.controller     - creating Shelly3EM output device
24-12-04 07:29:11.758 INFO  uni-meter.controller     - creating VZLogger input device
24-12-04 07:29:16.254 INFO  uni-meter.http.port-80   - HTTP server is listening on /[0:0:0:0:0:0:0:0]:80
```

Now you should be able to connect your Hoymiles storage to the emulator using the Hoymiles app.

## Automatic start using systemd

To start the tool automatically on boot, you can use the provided systemd service file. To do so, create a symlink 
within the `/etc/systemd/system` directory using the following command:

```shell
sudo ln -s /opt/uni-meter/config/systemd/uni-meter.service /etc/systemd/system/uni-meter.service
```

Afterward, you can enable the service using the following command so that it will be automatically started on boot:

```shell
sudo systemctl enable uni-meter
```

To start and stop the service immediately run

```shell
sudo systemctl start uni-meter
sudo systemctl stop uni-meter
```
The status of the service can be checked using

```shell
sudo systemctl status uni-meter
```

## Troubleshooting

If you start the tool directly from the command line, all error messages will be printed to the console. If you start the
tool using the systemd service, you can check the log messages in `/var/log/uni-meter.log`.

As a default the log level is just set to `INFO`. For debugging purposes you can set the log level to `DEBUG` or even 
`TRACE` within the `/opt/uni-meter/config/logback.xml` file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yy-MM-dd HH:mm:ss.SSS} %-5level %-24logger - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>

    <logger name="uni-meter"  level="TRACE" additivity="false">
        <appender-ref ref="STDOUT" />
    </logger>
</Configuration>
```

A restart is necessary for these changes to take effect.





