# Clojarr

### Build Status

Branch | Status
----------|--------
`master` | [![Circle CI](https://circleci.com/gh/arrdem/clojarr/tree/master.svg?style=svg)](https://circleci.com/gh/arrdem/clojarr/tree/master) | 
`develop` | [![Circle CI](https://circleci.com/gh/arrdem/clojarr/tree/develop.svg?style=svg)](https://circleci.com/gh/arrdem/clojarr/tree/develop)

> The reasonable man adapts himself to the world: the unreasonable one
> persists in trying to adapt the world to himself. Therefore all
> progress depends on the unreasonable man.
> 
> ~ George Bernard Shaw, _Maxims for Revolutionists_ 1903

## What

Clojarr is a hard fork of Clojure. Rich Hickey wrote Clojure to be the
language he wanted, and administers it in the way that he wants. This is
awesome, but I (Reid McKenzie) disagree with enough of the choices made in the
language and its administration that I've forked to go my own way.

The name itself is a corruption of "Clojure" which I owe to Gregory
Cerna.

## Goals

Clojarr is in no small part an experiment in what Clojure would look
like if it had a different contribution process, and was more willing
to make bold/risky changes.

Unlike Clojure which is eminently stable, Clojarr is not guaranteed to be
stable (yet). Some level of breaking changes with Clojure will
be accepted if there is a good reason for them.

## Administration

Currently, Clojarr is administered at my (Reid McKenzie's) sole
discretion. Contributions, bug reports, and feature requests are
welcomed.

It must be stated this is a hobby project for me. I'll respond to
issues and pull requests as I have free time to consider and address
them.
 
The EPL license reads

> THE PROGRAM IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR
> CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING, WITHOUT
> LIMITATION, ANY WARRANTIES OR CONDITIONS OF ... FITNESS FOR A
> PARTICULAR PURPOSE.

And that is the extent of the guarantees I offer with regards to this
software. It is my goal to maintain Clojarr in such a way that
[CIDER](https://github.com/clojure-emacs/cider) works, and that most
compatibility with Clojure is maintained but this is not a hard
guarantee. If you want a Clojure compatible language, you're gonna
have to use Clojure.

In the future when there are other active contributors, it is my
desire to hand off issues and patch reviews to volunteer contributors
as they may choose to donate time. The precise mechanism by which
contributors may approve changes is undecided, but the goal is for
significant contributors who have demonstrated that they share some
degree of respect for compatibility will be able to approve and merge
changes without my involvement.

## Contributing

This project adheres to the Contributor Covenant
[code of conduct](CODE_OF_CONDUCT.md).  By participating, you are
expected to uphold this code.  Please report unacceptable behavior to
[me+clojarr+coc@arrdem.com](mailto:me+clojarr+coc@arrdem.com).

## Legal

Clojarr is (c) Reid McKenzie. All rights reserved. The use and
distribution terms for this software are covered by the Eclipse Public
License 1.0 (EPL) (/licenses/epl-v10.txt). By using this software in
any fashion, you are agreeing to be bound by the terms of this
license.

You must not remove this notice, or any other, from this software.

Clojarr is derived directly from Clojure, (c) Rich Hickey, also
distributed under the EPL.

This program uses the Guava Murmur3 hash implementation which is
distributed under the Apache License, in /licenses/apache.txt

This program uses the ASM bytecode engineering library which is
distributed under the license in /licenses/inria.txt
