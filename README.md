# SmartCache

SmartCache is reliable cache that can protect your application from 3rd party systems failure.

## Motivation

Big problem of integrating with external services is protecting against downtimes.
If external service is not mission critical or used deeply in backend, we can just wait. In other cases it is enough to
inform the user about problems and again wait for solution.

However, there is a group of systems that need to integrate with external services and at the same time 99.99999% accessibility
time is a must. Sometimes freshness of data doesn't matter, as long as system can stay operable during external vendor
blackout. **SmartCache** is a lightweight library that wants to help solve this problem.

## How does it work?

Hah, good question, have to code first :)

## License

SmartCache is published under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
