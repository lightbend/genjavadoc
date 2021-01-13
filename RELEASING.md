## Creating a regular release

1. Create a [new release](https://github.com/lightbend/genjavadoc/releases/new) with:
  * the next tag version (e.g. `v0.11`)
  * title and release description including notable changes
  * link to the [milestone](https://github.com/lightbend/genjavadoc/milestones) showing an overview of closed issues for this release
2. This should push the release to sonatype from travis
3. Login to [Sonatype](https://oss.sonatype.org/) to close and release the repository.

## Back-releasing for a new Scala version

It is often the case when this compiler plugin needs to be released for a newly released Scala version. For this the process is the following:

1. Checkout the version that is to be back-released, which would be
  * master, if no features or bug fixes were merged to master since the latest release
  * tag, if the master has unreleased features or bug fixes. In this case you will need to cherry pick the commit that adds support for the new Scala version.
1. Create a file `version.sbt` containing `version in ThisBuild := "0.11"` which sets the version to be back-released. This will override the automatic version derivation from the git history. Alternatively you can `set version in ThisBuild := ...` in the command line.
1. Publish the release by running `SONATYPE_USERNAME=xxx SONATYPE_PASSWORD=xxx sbt ++2.12.13 publishSigned sonatypeBundleRelease` (with the appropriate credentials and scala version). You will need Sonatype OSS repository rights to publish under `com.typesafe` organisation.
