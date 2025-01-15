## Publishing

Creating a release on GitHub triggers an Actions build that should test, build, and publish to Sonatype, and then close and release the repository to Maven Central.

## Back-publishing for a new Scala version

sbt-ci-release supports this directly.

After checking (e.g. with `git diff`) that nothing user-detectable has changed since the last release, push a tag for with the Scala version appended, e.g. to back-publish version 0.19 for Scala 2.13.16, push the tag `v0.19@2.13.16`.
