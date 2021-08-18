# TLS  (HTTPS) Proxy Server in Scala

Very simple HTTPS proxy server written in Scala 2.12 with no external
dependencies beyond `scala-logging`.

Can be used as a library, or as a standalone program.


## Standalone

For a quick test, spin up the proxy using `sbt test`:

```
$ sbt test
[info] Loading global plugins from /Users/erik/.sbt/1.0/plugins
[info] Loading project definition from /Users/erik/work/tlsproxy/project
[info] Loading settings for project tlsproxy from build.sbt ...
[info] Set current project to tlsproxy (in build file:/Users/erik/work/tlsproxy/)
21:49:37.534 INFO  i.g.e.s.ServerHandler - Listening on port 3128...
21:49:46.411 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:57870 -> f5.secure.force.com/13.110.3.182:443 finished (up: 8301, down: 7954)
21:49:46.413 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:57870 -> f5.secure.force.com:443 finished (up: 8301, down: 7954)
21:49:48.948 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:57882 -> bam.nr-data.net/162.247.242.18:443 finished (up: 1560, down: 3604)
21:49:48.949 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:57836 -> 4dcz.la1-c2-ph2.salesforceliveagent.com/13.110.3.5:443 finished (up: 1635, down: 3834)
21:49:52.175 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:57978 cannot resolve dpm.demdex.net
21:49:52.373 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:57949 -> match.prod.bidr.io/52.12.161.87:443 finished (up: 1348, down: 5675)
21:49:52.373 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:57924 -> mktg.biz.f5.com/152.199.2.76:443 finished (up: 1875, down: 37423)
21:49:55.653 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:57979 -> www.facebook.com/157.240.220.35:443 finished (up: 2795, down: 1453)
21:49:55.661 ERROR i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58006 -> f5.secure.force.com/13.110.3.182:443 (up: 6253 down: 47027) connection failed: IOException: Broken pipe
21:49:55.989 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58001 -> bam.nr-data.net/162.247.242.18:443 finished (up: 2941, down: 3779)
21:49:55.998 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58026 -> 653-smc-783.mktoresp.com/192.28.144.124:443 finished (up: 1566, down: 483)
21:49:58.056 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58059 -> bam.nr-data.net/162.247.242.18:443 finished (up: 1761, down: 3570)
21:49:58.202 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58079 -> d.la1-c2-ph2.salesforceliveagent.com/13.110.1.133:443 finished (up: 1352, down: 764)
21:50:04.671 ERROR i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58043 -> match.prod.bidr.io/52.12.161.87:443 (up: 1230 down: 484) connection failed: IOException: Connection reset by peer
21:50:04.671 ERROR i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58039 -> pixel.sitescout.com/216.65.25.160:443 (up: 1260 down: 430) connection failed: IOException: Connection reset by peer
21:50:04.672 ERROR i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58037 -> pixel.sitescout.com/216.65.25.160:443 (up: 1159 down: 440) connection failed: IOException: Connection reset by peer
21:50:05.347 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58114 -> d.la1-c2-ph2.salesforceliveagent.com/13.110.1.133:443 finished (up: 1352, down: 764)
21:50:06.488 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58124 cannot resolve dpm.demdex.net
21:50:19.381 ERROR i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58125 -> 4dcz.la1-c2-ph2.salesforceliveagent.com/13.110.3.5:443 (up: 4709 down: 1217) connection failed: IOException: Broken pipe
21:50:36.432 ERROR i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58298 -> unconnected connection failed: IOException: Malformed request
21:52:22.311 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58634 -> bam.nr-data.net/162.247.242.19:443 finished (up: 1571, down: 3604)
21:52:22.311 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58374 -> r3---sn-5hne6nsy.googlevideo.com/172.217.132.104:443 finished (up: 31807, down: 2165985)
21:54:54.869 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58309 -> www.gstatic.com/142.250.65.67:443 finished (up: 1252, down: 1287)
21:54:54.870 INFO  i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:58287 -> www.youtube.com/142.251.45.110:443 finished (up: 2690, down: 579159)
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

Now log in to https://s01.oss.sonatype.org, click on "Staging Repositories",
select ours, then click `Close`, provide a short string and hit refresh to
check if the deployment gets promoted successfully.

When it closes successfully, hit `Release`. This makes the artifacts publicly
available (and they can now be found with the artifact-search).

Sonatype has a [video](https://www.youtube.com/watch?v=dXR4pJ_zS-0).

Troubleshooting:

* https://github.com/sbt/sbt-pgp#sbt-pgp
* https://gist.github.com/danieleggert/b029d44d4a54b328c0bac65d46ba4c65
* https://www.scala-sbt.org/release/docs/Using-Sonatype.html
