# Contributing to Jaunt

:+1::tada: First off, thanks for taking the time to contribute! :tada::+1:

The following is a set of guidelines for contributing to Jaunt which is hosted at
[jaunt-lang/jaunt](https://github.com/jaunt-lang/jaunt) on GitHub.  These are just guidelines, not rules,
use your best judgment and feel free to propose changes to this document in a pull request.

## What should I know before I get started?

### Code of Conduct

This project adheres to the Contributor Covenant [code of conduct](CODE_OF_CONDUCT.md).  By
participating, you are expected to uphold this code.  Please report unacceptable behavior to
[me+jaunt+coc@arrdem.com](mailto:me+jaunt+coc@arrdem.com).

### Development workflow & tools

This project follows the [git-flow](http://nvie.com/posts/a-successful-git-branching-model/)
branching model, making use of the
[git-flow (AVH Edition)](https://github.com/petervanderdoes/gitflow-avh) tooling for extended git
hooks among other things.

Jaunt is structured as a [Maven](https://maven.apache.org/) project, which makes use of
[Ant](https://ant.apache.org) for various build and testing tasks. Ant may be used without Maven by
running the `etc/bin/antsetup.sh` script.

Jaunt makes use of [Leiningen](https://github.com/technomancy/leiningen) for
[cljfmt](https://github.com/weavejester/cljfmt), and also uses
[astyle](http://astyle.sourceforge.net) for Java formatting. As Jaunt is rigorous about formatting
and linting, these are tools you'll need eventually.

## Issues

GitHub issues welcome!

## Pull requests

Pull requests welcome!

We do ask that before submitting a pull request you open an issue tracking the bug of enhancement
you'd like to fix or submit. This makes it easier to discuss changes in the abstract, before
focusing on a particular solution.

Furthermore, please be diligent about submitting pull requests which only make one essential change
at a time. While formatting changes and code cleanups are welcome, they should be separate from
features and a pull request should only introduce one logical feature at a time.

### Change Log

Pull requests are required to update the [changelog](CHANGELOG.md). Changelog entries should mention
and link to any issues or tickets involved in the change, and should provide a moderately technical
description of the particular changes of the patch.

An example of a changelog entry would be:

```
- [#76](https://github.com/jaunt-lang/jaunt/pull/76) Rename project to Jaunt (@arrdem).
  - Renames the project from `me.arrdem/clojarr` to `org.jaunt-lang/jaunt`.
```

Note the leading link to the pull request tracking that particular patch, and the username of the
person who submitted the change. Commentary on the change should appear as a nested, unordered list.

### Whitespace & Linting

Jaunt is maintained with fairly strict whitespace and style standards.

The script `etc/bin/whitespace.sh` serves to format all project sources.
- Java code is formatted using [astyle](astyle.sourceforge.net) and the `astylerc` style
  configurations.

The script `etc/bin/check-whitespace.sh` serves to check for whitespace style problems, running the
formatter(s) mentioned above in their linting modes.

Patches must whitespace lint cleanly before they will be accepted.
