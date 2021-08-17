# TLS  (HTTPS) Proxy Server in Scala

Very simple HTTPS proxy server written in Scala 2.12 with no dependencies
beyond `scala-logging` 

Can be used as a library, or as a standalone program.

## Standalone

```
$ sbt run
[info] Loading global plugins from /Users/erik/.sbt/1.0/plugins
[info] Loading project definition from /Users/erik/work/tlsproxy/project
[info] Loading settings for project tlsproxy from build.sbt ...
[info] Set current project to tlsproxy (in build file:/Users/erik/work/tlsproxy/)
[info] Running tlsproxy.Main 
14:31:46.707 [run-main-0] INFO tlsproxy.ServerHandler - Listening on port 3128...
18:03:15.466 [main] INFO tlsproxy.ServerHandler - Listening on port 3128...
18:03:22.651 [main] ERROR tlsproxy.TlsProxyHandler - /0:0:0:0:0:0:0:1:49672 -> google.com:443: error: connection closed: java.io.IOException: Connection reset by peer
18:04:22.806 [main] INFO tlsproxy.TlsProxyHandler - /0:0:0:0:0:0:0:1:49818 -> www.google.com/172.217.6.36:443 finished (up: 581, down: 4294)
18:04:56.131 [main] INFO tlsproxy.TlsProxyHandler - /0:0:0:0:0:0:0:1:49807 -> nginx.org/52.58.199.22:443 finished (up: 568, down: 187)
```

Now configure `localhost:3128` as proxy in your browser.

```
$ curl -I -x localhost:3128 https://woefdram.nl
HTTP/1.1 200 Connection Accepted
Proxy-Agent: TlsProxy/1.0 (github.com/erikvanzijst/scala_tlsproxy)
Content-Type: text/plain; charset=us-ascii
Content-Length: 0

HTTP/2 200 
server: nginx/1.18.0
date: Tue, 17 Aug 2021 16:19:04 GMT
content-type: text/html
content-length: 612
last-modified: Tue, 21 Apr 2020 14:09:01 GMT
etag: "5e9efe7d-264"
accept-ranges: bytes
``` 

## Library

To use it as a library in-process:

```scala
import tlsproxy.TlsProxy

new TlsProxy(3128).run()
```

The `run()` does not create any threads and run the entire proxy on the
calling thread. It does not return.

To move it to the background, pass it to a `Thread` or `Executor`:

```scala
import tlsproxy.TlsProxy
import java.util.concurrent.Executors

val executor = Executors.newSingleThreadExecutor()
executor.submit(new TlsProxy(3128))
```

## Caveat emptor

This is only implements the `CONNECT` method and can therefor only proxy HTTPS
requests. It does not support unencrypted proxy requests using `GET`.

Proxy requests for HTTP (non-TLS) `GET` requests result in an error and the
connection getting closed:

```
18:08:53.604 [main] ERROR tlsproxy.TlsProxyHandler - /0:0:0:0:0:0:0:1:51043 -> unconnected: error: connection closed: java.io.IOException: Malformed request
```

## Robustness (or lack thereof)

* This implementation is totally susceptible to all kinds of [slowloris attacks](https://en.wikipedia.org/wiki/Slowloris_(computer_security).
* It does not support client authentication
* Uses only 1 thread and cannot currently scale to multiple cores
* Does not restrict non-standard upstream ports
* Undoubtedly riddled with bugs
