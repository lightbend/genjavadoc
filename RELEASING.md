## Creating a regular release

1. Create a [new release](https://github.com/lightbend/genjavadoc/releases/new) with:
  * `git tag -a -m 'Release v0.16' v0.16`
  * title and release description including notable changes
2. This triggers a Github Action build that should test, build and publish to sonatype, and then close and release the repository to Maven Central.

## Back-releasing for a new Scala version

It is often the case when this compiler plugin needs to be released for a newly released Scala version. For this the process is the following:

1. Checkout the version that is to be back-released, which would be
  * `main`, if no features or bug fixes were merged to the main branch since the latest release. In this case create a tag recording you released from this commit, e.g. `git tag -a -m 'Release 0.16 for Scala 2.12.13' v0.16_2.12.13; git push --tags`.
  * tag, if the main branch has unreleased features or bug fixes. In this case you will need to cherry pick the commit that adds support for the new Scala version.
1. Create a file `version.sbt` containing `version in ThisBuild := "0.16"` which sets the version to be back-released. This will override the automatic version derivation from the git history. Alternatively you can `set version in ThisBuild := ...` in the command line.
1. Publish the release by running `SONATYPE_USERNAME=xxx SONATYPE_PASSWORD=xxx sbt clean ++2.12.15 publishSigned sonatypeBundleRelease` (with the appropriate credentials and scala version). You will need Sonatype OSS repository rights to publish under `com.typesafe` organisation.
