**Akka-Streams**

## Synopsis

Reactive-Streams is an SPI(service provider interface), not an API. Which defines protocol about 
reactive-streams but we can use Akka streams API to implement, design async, backpressured streams.

### Prerequisites

#### Concepts ####
1. publisher = emits elements (asynchronously)
2. subsriber = receives elements
3. processor = transforms elements along the way
4. async = non-blocking operation
5. backpressure = Elements flow as response to demand from subsriber

## Modules
### __part2_primer__
This explains about basics principles of akka-streams, materializing values, operator fusion & basics
of most important concept backpressure

## References
1. [Reactive Streams](https://www.reactive-streams.org/)
