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

Usual cache works on objects - naive implementation is as simple as associate object V with key K and store it.
**SmartCache** operates on callable actions instead. When getting item from cache, you actually need to provide action
that can resolve it. If result of this action is already cached, it is returned without calling possibly long-running action.
Otherwise action is called and unless it exceeds configurable timeout or other exception is thrown, result of action
is returned and stored in cache.

**SmartCache** protects against calling multiple same actions simultaneously using action aggregation.


## How to use it?

```java
SmartCache cache = new SmartCache(Executors.newCachedThreadPool());
cache.registerRegion(new Region("region", new TimeExpirationPolicy(10000), 5));

T value = cache.get("key")
                .fromRegion("region")
                .withTimeout(Duration.ofMillis(1000))
                .invoke(() -> { /* action */ });
```


## License

SmartCache is published under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
