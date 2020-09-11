# Interconnecting Flights
Application returns a list of flights departing from a given departure airport not earlier than the specified departure dateTime and arriving to a given arrival airport not later than the specified arrival dateTime. 

The list consists of:

- direct flights (DUB - BCN)
- all interconnecting flights with a maximun of one stop (DUB-EDI-BCN). For these flights the difference between arrival and next departure is 2h or greater 


### Inputs Validations
- all parameters are required
- departure and arrival airport can not be the same
- departure date can not be after than arrival date
- dates in ISO format
- departure and arrival have to be 3 letters. Codes in lower case are allowed
- for all of these validations the application will respond with a bad request response

```aidl
- url without departure
{
    "timestamp": "2020-09-11T05:47:14.61",
    "status": 400,
    "error": "BAD_REQUEST",
    "message": "Required String parameter 'departure' is not present"
}

- url with departure 1MAD
{
    "timestamp": "2020-09-11T05:48:05.378",
    "status": 400,
    "error": "BAD_REQUEST",
    "message": "interconnections.departure: Invalid departure format"
}

```

URL example : 
http://localhost:8080/flights/interconnections?departure=DUB&departureDateTime=2020-10-10T18:50&arrivalDateTime=2020-10-11T18:20&arrival=BCN
```
[
    {
        "stops": 0,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T06:15:00",
                "arrivalDateTime": "2020-10-11T09:45:00"
            }
        ]
    },
    {
        "stops": 0,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T12:40:00",
                "arrivalDateTime": "2020-10-11T16:10:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "BGY",
                "departureDateTime": "2020-10-11T06:25:00",
                "arrivalDateTime": "2020-10-11T09:50:00"
            },
            {
                "departureAirport": "BGY",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T14:10:00",
                "arrivalDateTime": "2020-10-11T15:45:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "EDI",
                "departureDateTime": "2020-10-10T19:35:00",
                "arrivalDateTime": "2020-10-10T20:45:00"
            },
            {
                "departureAirport": "EDI",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T07:05:00",
                "arrivalDateTime": "2020-10-11T10:50:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "MAN",
                "departureDateTime": "2020-10-10T18:50:00",
                "arrivalDateTime": "2020-10-10T19:55:00"
            },
            {
                "departureAirport": "MAN",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T06:25:00",
                "arrivalDateTime": "2020-10-11T09:45:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "MAN",
                "departureDateTime": "2020-10-10T20:40:00",
                "arrivalDateTime": "2020-10-10T21:40:00"
            },
            {
                "departureAirport": "MAN",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T06:25:00",
                "arrivalDateTime": "2020-10-11T09:45:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "OPO",
                "departureDateTime": "2020-10-10T19:25:00",
                "arrivalDateTime": "2020-10-10T21:50:00"
            },
            {
                "departureAirport": "OPO",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T06:25:00",
                "arrivalDateTime": "2020-10-11T09:15:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "PMI",
                "departureDateTime": "2020-10-10T19:25:00",
                "arrivalDateTime": "2020-10-10T23:10:00"
            },
            {
                "departureAirport": "PMI",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T06:30:00",
                "arrivalDateTime": "2020-10-11T07:25:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "PMI",
                "departureDateTime": "2020-10-10T19:25:00",
                "arrivalDateTime": "2020-10-10T23:10:00"
            },
            {
                "departureAirport": "PMI",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T13:00:00",
                "arrivalDateTime": "2020-10-11T13:55:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "PMI",
                "departureDateTime": "2020-10-10T19:25:00",
                "arrivalDateTime": "2020-10-10T23:10:00"
            },
            {
                "departureAirport": "PMI",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T14:25:00",
                "arrivalDateTime": "2020-10-11T15:20:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "PMI",
                "departureDateTime": "2020-10-11T08:40:00",
                "arrivalDateTime": "2020-10-11T12:25:00"
            },
            {
                "departureAirport": "PMI",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T14:25:00",
                "arrivalDateTime": "2020-10-11T15:20:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "STN",
                "departureDateTime": "2020-10-10T20:20:00",
                "arrivalDateTime": "2020-10-10T21:40:00"
            },
            {
                "departureAirport": "STN",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T08:15:00",
                "arrivalDateTime": "2020-10-11T11:30:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "STN",
                "departureDateTime": "2020-10-10T20:20:00",
                "arrivalDateTime": "2020-10-10T21:40:00"
            },
            {
                "departureAirport": "STN",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T12:45:00",
                "arrivalDateTime": "2020-10-11T16:00:00"
            }
        ]
    },
    {
        "stops": 1,
        "legs": [
            {
                "departureAirport": "DUB",
                "arrivalAirport": "SXF",
                "departureDateTime": "2020-10-10T19:40:00",
                "arrivalDateTime": "2020-10-10T23:00:00"
            },
            {
                "departureAirport": "SXF",
                "arrivalAirport": "BCN",
                "departureDateTime": "2020-10-11T06:20:00",
                "arrivalDateTime": "2020-10-11T09:00:00"
            }
        ]
    }
]
```
