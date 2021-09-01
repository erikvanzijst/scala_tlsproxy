# TLS  (HTTPS) Proxy Server in Scala

Very simple HTTPS proxy server written in Scala 2.12 with no external
dependencies beyond `scala-logging` and low resource overhead.

Can be used as a library, or as a standalone program.


## Installation

Add as a dependency.

sbt users may add this to their `build.sbt`:

```scala
libraryDependencies += "io.github.erikvanzijst" % "scala-tlsproxy_2.12" % "0.4.2"
```

Maven:

```xml
<dependency>
    <groupId>io.github.erikvanzijst</groupId>
    <artifactId>scala-tlsproxy_2.12</artifactId>
    <version>0.4.2</version>
</dependency>
```


## Usage

Instantiate the proxy:

```scala
import io.github.erikvanzijst.scalatlsproxy.TlsProxy

new TlsProxy(3128).run()
```

The `run()` method does not create any threads and runs the entire proxy on
the calling thread. It does not return until closed.

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
23:16:25.375 ERROR i.g.e.s.TlsProxyHandler - /0:0:0:0:0:0:0:1:49603 -> unconnected connection failed: IOException: Malformed request
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
