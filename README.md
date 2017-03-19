# Fix The Build
[![Build Status](https://travis-ci.org/jhoekx/fixthebuild.svg?branch=master)](https://travis-ci.org/jhoekx/fixthebuild)

A web application to rotate a build fixer role.

## Usage

Test:

```
lein test
```

Compile:

```
lein uberjar
```

Run:

```
java -jar target/fixthebuild-*-standalone.jar
```

Develop:

```
[jeroen@island fixthebuild]$ lein repl
user=> (go)
2017-02-10 21:01:04.244:INFO:oejs.Server:nREPL-worker-0: jetty-9.2.10.v20150310
2017-02-10 21:01:04.260:INFO:oejs.ServerConnector:nREPL-worker-0: Started ServerConnector@24688f3a{HTTP/1.1}{0.0.0.0:3000}
2017-02-10 21:01:04.261:INFO:oejs.Server:nREPL-worker-0: Started @7657ms
:initiated
user=> (reset)
2017-02-10 21:01:08.978:INFO:oejs.ServerConnector:nREPL-worker-0: Stopped ServerConnector@24688f3a{HTTP/1.1}{0.0.0.0:3000}
:reloading (fixthebuild.api fixthebuild.app fixthebuild.handler fixthebuild.system fixthebuild.main fixthebuild.api-test user)
2017-02-10 21:01:09.177:INFO:oejs.Server:nREPL-worker-0: jetty-9.2.10.v20150310
2017-02-10 21:01:09.178:INFO:oejs.ServerConnector:nREPL-worker-0: Started ServerConnector@14554441{HTTP/1.1}{0.0.0.0:3000}
2017-02-10 21:01:09.179:INFO:oejs.Server:nREPL-worker-0: Started @12575ms
:resumed
```

`lein test-refresh` provides a convenient development experience. Use a leiningen profile like:

```
{:user {:plugins [[com.jakemccrary/lein-test-refresh "0.19.0"]
                  [venantius/ultra "0.5.1"]]
        :ultra   {:repl        false
                  :stacktraces false
                  :tests       true}}}
```

## License

Copyright © 2017 Jeroen Hoekx

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
