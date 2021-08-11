# TLS Proxy Server in Scala

Very simple HTTPS proxy server written in Scala 2.12.

Can be used as a library with no dependencies beyond `scala-logging`, or as a
standalone program.

```
$ sbt run
[info] Loading global plugins from /Users/erik/.sbt/1.0/plugins
[info] Loading project definition from /Users/erik/work/tlsproxy/project
[info] Loading settings for project tlsproxy from build.sbt ...
[info] Set current project to tlsproxy (in build file:/Users/erik/work/tlsproxy/)
[info] Running tlsproxy.Main 
14:31:46.707 [run-main-0] INFO tlsproxy.ServerHandler - Listening on port 3128...
```