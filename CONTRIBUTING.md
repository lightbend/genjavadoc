# Adding a new testcase

When adding a new testcase to `src/test/resources/input`, you can run the tests and find in
the test output the current result as a diff to `src/test/resources/expected_output`. Once
you've reproduced the problem you want to tackle and changed the genjavadoc code accordingly,
it is time to populate `src/test/resources/expected_output`.

genjavadoc is tested with many different versions of scala, and
`src/test/resources/expected_output` is expected to correspond to the oldest supported version
of scala (check `.github/workflows/validate.yml` or `build.sbt` for the supported scala version matrix).

When changes in Scala cause changes in the expected genjavadoc output, you add them as patches
in the respective patch in `src/test/resources/expected_output`. Remember to clean between
invocations of `sbt` with different versions.

Usually you can just copy-paste the patch from `sbt ++2.12.3 clean test` into,
`src/test/resources/patches/2.12.3.patch`, changing the `+++` line to look exactly like the
`---` line.
