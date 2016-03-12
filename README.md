[![License EPL 1.0](https://img.shields.io/badge/license-EPL_1-green.svg)](https://www.eclipse.org/legal/epl-v10.html)
[![Patreon Donate](https://img.shields.io/badge/patreon-donate-yellow.svg)](https://patreon.com/arrdem)
[![Join the chat at https://gitter.im/jaunt-lang/jaunt](https://badges.gitter.im/jaunt-lang/jaunt.svg)](https://gitter.im/jaunt-lang/jaunt?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Twitter Follow](https://img.shields.io/twitter/follow/jauntlang.svg?style=social)](https://twitter.com/jaunt-lang)

<p align="center">
  <img src="etc/img/gh-banner.png" alt="Jaunt logo" />
</p>

### Build Status

Branch | Status | Version
----------|--------|--------
`master` | [![Circle CI](https://circleci.com/gh/jaunt-lang/jaunt/tree/master.svg?style=svg)](https://circleci.com/gh/jaunt-lang/jaunt/tree/master) | ![Release Version](https://img.shields.io/github/release/jaunt-lang/jaunt.svg)
`develop` | [![Circle CI](https://circleci.com/gh/jaunt-lang/jaunt/tree/develop.svg?style=svg)](https://circleci.com/gh/jaunt-lang/jaunt/tree/develop) | [![Snapshot Version](https://img.shields.io/clojars/v/org.jaunt-lang/jaunt.svg)](https://clojars.org/org.jaunt-lang/jaunt)

## What

> The reasonable man adapts himself to the world: the unreasonable one
> persists in trying to adapt the world to himself. Therefore all
> progress depends on the unreasonable man.
>
> ~ George Bernard Shaw, _Maxims for Revolutionists_ 1903

Jaunt is a hard fork of Clojure. Rich Hickey wrote Clojure to be the language he wanted, and
administers it in the way that he wants. This is awesome, but I (Reid McKenzie) disagree with enough
of the choices made in the language and its administration that I've forked to go my own way.

See [the thesis blog post](https://www.arrdem.com/2016/02/22/clojarr_-_a_friendly_clojure_fork/) for
more on this.

## Goals

Jaunt is in no small part an experiment in what Clojure would look like if it had a different
contribution process, and was more willing to make bold/risky changes.

Unlike Clojure which is eminently stable, Jaunt is not guaranteed to be stable (yet). Some level of
breaking changes with Clojure will be accepted if there is a good reason for them.

## Administration

Currently, Jaunt is administered at my (Reid McKenzie's) sole discretion. Contributions, bug
reports, and feature requests are welcomed.

It must be stated this is a hobby project for me. I'll respond to issues and pull requests as I have
free time to consider and address them.

The EPL license reads

> THE PROGRAM IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
> EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR CONDITIONS OF ... FITNESS FOR
> A PARTICULAR PURPOSE.

And that is the extent of the guarantees I offer with regards to this software. It is my goal to
maintain Jaunt in such a way that [CIDER](https://github.com/clojure-emacs/cider) works, and that
most compatibility with Clojure is maintained but this is not a hard guarantee. If you want a
Clojure compatible language, you're gonna have to use Clojure.

In the future when there are other active contributors, it is my desire to hand off issues and patch
reviews to volunteer contributors as they may choose to donate time. The precise mechanism by which
contributors may approve changes is undecided, but the goal is for significant contributors who have
demonstrated that they share some degree of respect for compatibility will be able to approve and
merge changes without my involvement.

## Contributing

This project adheres to the Contributor Covenant [code of conduct](CODE_OF_CONDUCT.md).  By
participating, you are expected to uphold this code.  Please report unacceptable behavior to
[me+jaunt+coc@arrdem.com](mailto:me+jaunt+coc@arrdem.com). See [CONTRIBUTING.md](CONTRIBUTING.md)
for more information about this project's development process.

### Donations

You can support the development of Jaunt and related tooling via
[Patreon](https://www.patreon.com/arrdem). Note that choosing to contribute represents blanket
support for this and other projects, and does not constitute a contract for support or
[for a tee shirt](https://groups.google.com/forum/#!msg/seajure/GLqhj_2915A/E0crn6zHLi0J).

## Legal

Jaunt is (c) Reid McKenzie. All rights reserved. The use and distribution terms for this software
are covered by the Eclipse Public License 1.0 (EPL)
([/etc/licenses/epl-v10.txt](etc/licenses/epl-v10.txt)). By using this software in any fashion, you are
agreeing to be bound by the terms of this license.

You must not remove this notice, or any other, from this software.

Jaunt is derived directly from Clojure, (c) Rich Hickey, also distributed under the EPL.

This program uses the Guava Murmur3 hash implementation which is distributed under the Apache
License, in [/etc/licenses/apache.txt](etc/licenses/apache.txt)

This program uses the ASM bytecode engineering library which is distributed under the license in
[/etc/licenses/inria.txt](etc/licenses/inria.txt)

## Change Log

An extensive changelog is available [here](CHANGELOG.md).

## FAQ

> Will you support $LIBRARY/$PRODUCT?

No. They may work just fine, and you're welcome to offer changes continuing or preserving support as
well as to assist in preventing breakages but no degree of compatibility with Clojure, Cognitect
product or surrounding ecosystem is guaranteed or represented.

> Will you add $FEATURE/$PATCH from Clojure?

No. Under the EPL you may port changes from Clojure to Jaunt. However I make no promises of
merging or porting upstream changes.

> Will you add $FEATURE to the language?

Maybe. For the most part, it seems that `clojure.core` is shall we say "right sized". In the Scheme
tradition, the compiler works and the core is large enough that users can and have built just about
everything else you could want as a library. This leads me to believe that for the most part adding
features to the language isn't called for.

That said, I don't think the language is perfect. The Java implementation could use some spit and
shoeshine in my present estimation. The compiler could be smarter. The various "internal" APIs could
be reviewed, documented and published so that users can rely on them. There are some things which
clearly deserve to be added such as type predicates which are nearly universally defined by users,
but I expect this project to largely be one of refinement rather than one of novel development.
