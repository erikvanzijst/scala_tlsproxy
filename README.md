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

The project is published to Sonatype OSS:

    Group id / organization: io.github.erikvanzijst
    Artifact id / name: scala-tlsproxy

sbt users may add this to their `build.sbt`:

```
libraryDependencies += "io.github.erikvanzijst" %% "scala-tlsproxy_2.12" % "0.1.0"
```

To use it as a library in-process:

```scala
import io.github.erikvanzijst.scalatlsproxy.TlsProxy

new TlsProxy(3128).run()
```

The `run()` method does not create any threads and runs the entire proxy on
the calling thread. It does not return.

To move it to the background, pass it to a `Thread` or `Executor`:

```scala
import io.github.erikvanzijst.scalatlsproxy.TlsProxy
import java.util.concurrent.Executors

val executor = Executors.newSingleThreadExecutor()
executor.submit(new TlsProxy(3128))
```


## Caveat emptor

This only implements the `CONNECT` method and can therefore only proxy HTTPS
requests. It does not support unencrypted proxy requests using `GET`.

Proxy requests for HTTP (non-TLS) `GET` requests result in an error and the
connection getting closed:

```
18:08:53.604 [main] ERROR tlsproxy.TlsProxyHandler - /0:0:0:0:0:0:0:1:51043 -> unconnected: error: connection closed: java.io.IOException: Malformed request
```


## Robustness (or lack thereof)

* This implementation is totally susceptible to all kinds of [slowloris attacks](https://en.wikipedia.org/wiki/Slowloris_%28computer_security%29)
* It does not support client authentication
* Uses only 1 thread and cannot currently scale to multiple cores
* Does not restrict non-standard upstream ports
* Undoubtedly riddled with bugs


## Publishing

Publishing is done to the Sonatype Central Repository and requires gpg-signed
artifacts. For this, install gpg and (on Mac) `pin-entry-mac`:

```
$ brew install gnupg pinentry-mac
```

Add the pinentry program to `~/.gnupg/gpg-agent.conf`:

```
pinentry-program /usr/local/bin/pinentry-mac
```

Restart `gpg-agent`:

```
$ gpgconf --kill gpg-agent
```

Run `publishLocalSigned` to ensure signing from `sbt` works (this should pop
up a dialog to enter the private key's passphrase):

```
$ sbt publishLocalSigned
[info] Loading global plugins from /Users/erik/.sbt/1.0/plugins
[info] Loading settings for project tlsproxy-build from plugins.sbt ...
[info] Loading project definition from /Users/erik/work/tlsproxy/project
[info] Loading settings for project tlsproxy from build.sbt ...
[info] Set current project to tlsproxy (in build file:/Users/erik/work/tlsproxy/)
[info] Wrote /Users/erik/work/tlsproxy/target/scala-2.12/tlsproxy_2.12-0.1.pom
[info] :: delivering :: erikvanzijst#tlsproxy_2.12;0.1 :: 0.1 :: release :: Tue Aug 17 22:44:46 CEST 2021
[info] 	delivering ivy file to /Users/erik/work/tlsproxy/target/scala-2.12/ivy-0.1.xml
[info] gpg: using "E96DDAAB16804D86EFA2A08A4539ACC7B26D1005" as default secret key for signing
[info] gpg: using "E96DDAAB16804D86EFA2A08A4539ACC7B26D1005" as default secret key for signing
[info] gpg: using "E96DDAAB16804D86EFA2A08A4539ACC7B26D1005" as default secret key for signing
[info] gpg: using "E96DDAAB16804D86EFA2A08A4539ACC7B26D1005" as default secret key for signing
[info] 	published tlsproxy_2.12 to /Users/erik/.ivy2/local/erikvanzijst/tlsproxy_2.12/0.1/jars/tlsproxy_2.12.jar
[info] 	published tlsproxy_2.12 to /Users/erik/.ivy2/local/erikvanzijst/tlsproxy_2.12/0.1/docs/tlsproxy_2.12-javadoc.jar
[info] 	published tlsproxy_2.12 to /Users/erik/.ivy2/local/erikvanzijst/tlsproxy_2.12/0.1/srcs/tlsproxy_2.12-sources.jar
[info] 	published tlsproxy_2.12 to /Users/erik/.ivy2/local/erikvanzijst/tlsproxy_2.12/0.1/poms/tlsproxy_2.12.pom.asc
[info] 	published tlsproxy_2.12 to /Users/erik/.ivy2/local/erikvanzijst/tlsproxy_2.12/0.1/poms/tlsproxy_2.12.pom
[info] 	published tlsproxy_2.12 to /Users/erik/.ivy2/local/erikvanzijst/tlsproxy_2.12/0.1/jars/tlsproxy_2.12.jar.asc
[info] 	published tlsproxy_2.12 to /Users/erik/.ivy2/local/erikvanzijst/tlsproxy_2.12/0.1/srcs/tlsproxy_2.12-sources.jar.asc
[info] 	published tlsproxy_2.12 to /Users/erik/.ivy2/local/erikvanzijst/tlsproxy_2.12/0.1/docs/tlsproxy_2.12-javadoc.jar.asc
[success] Total time: 1 s, completed Aug 17, 2021 10:44:47 PM
```

Now publish to Sonatype:

```
$ sbt publishSigned
[info] Loading global plugins from /Users/erik/.sbt/1.0/plugins
[info] Loading settings for project tlsproxy-build from plugins.sbt ...
[info] Loading project definition from /Users/erik/work/tlsproxy/project
[info] Loading settings for project tlsproxy from build.sbt ...
[info] Set current project to scala-tlsproxy (in build file:/Users/erik/work/tlsproxy/)
[info] Wrote /Users/erik/work/tlsproxy/target/scala-2.12/scala-tlsproxy_2.12-0.1-SNAPSHOT.pom
[info] gpg: using "E96DDAAB16804D86EFA2A08A4539ACC7B26D1005" as default secret key for signing
[info] gpg: using "E96DDAAB16804D86EFA2A08A4539ACC7B26D1005" as default secret key for signing
[info] gpg: using "E96DDAAB16804D86EFA2A08A4539ACC7B26D1005" as default secret key for signing
[info] gpg: using "E96DDAAB16804D86EFA2A08A4539ACC7B26D1005" as default secret key for signing
[info] 	published scala-tlsproxy_2.12 to https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/erikvanzijst/scala-tlsproxy_2.12/0.1-SNAPSHOT/scala-tlsproxy_2.12-0.1-SNAPSHOT.jar
[info] 	published scala-tlsproxy_2.12 to https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/erikvanzijst/scala-tlsproxy_2.12/0.1-SNAPSHOT/scala-tlsproxy_2.12-0.1-SNAPSHOT-sources.jar
[info] 	published scala-tlsproxy_2.12 to https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/erikvanzijst/scala-tlsproxy_2.12/0.1-SNAPSHOT/scala-tlsproxy_2.12-0.1-SNAPSHOT-javadoc.jar
[info] 	published scala-tlsproxy_2.12 to https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/erikvanzijst/scala-tlsproxy_2.12/0.1-SNAPSHOT/scala-tlsproxy_2.12-0.1-SNAPSHOT.jar.asc
[info] 	published scala-tlsproxy_2.12 to https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/erikvanzijst/scala-tlsproxy_2.12/0.1-SNAPSHOT/scala-tlsproxy_2.12-0.1-SNAPSHOT.pom.asc
[info] 	published scala-tlsproxy_2.12 to https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/erikvanzijst/scala-tlsproxy_2.12/0.1-SNAPSHOT/scala-tlsproxy_2.12-0.1-SNAPSHOT-sources.jar.asc
[info] 	published scala-tlsproxy_2.12 to https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/erikvanzijst/scala-tlsproxy_2.12/0.1-SNAPSHOT/scala-tlsproxy_2.12-0.1-SNAPSHOT-javadoc.jar.asc
[info] 	published scala-tlsproxy_2.12 to https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/erikvanzijst/scala-tlsproxy_2.12/0.1-SNAPSHOT/scala-tlsproxy_2.12-0.1-SNAPSHOT.pom
[success] Total time: 9 s, completed Aug 17, 2021 11:29:22 PM
```

Troubleshooting:

* https://github.com/sbt/sbt-pgp#sbt-pgp
* https://gist.github.com/danieleggert/b029d44d4a54b328c0bac65d46ba4c65
* https://www.scala-sbt.org/release/docs/Using-Sonatype.html
