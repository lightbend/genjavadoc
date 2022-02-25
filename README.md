# GenJavadoc

[![Test and publish](https://github.com/lightbend/genjavadoc/actions/workflows/validate.yml/badge.svg?branch=main)](https://github.com/lightbend/genjavadoc/actions/workflows/validate.yml)

This project’s goal is the creation of real Javadoc for Scala projects. While Scaladoc—the native API documentation format of Scala—has several benefits over Javadoc, Java programmers are very much used to having access to API documentation in a syntax matching their programming language of choice. Another motivating factor may be that javadoc-JARs are supported within IDE, e.g. showing tooltip help texts.

## Latest Version

[![Latest version](https://img.shields.io/maven-central/v/com.typesafe.genjavadoc/genjavadoc-plugin_2.13.8)]

## How to Use It

GenJavadoc is a Scala compiler plugin which emits structurally equivalent Java code for all Scala sources of a project, keeping the Scaladoc comments (with a few format adaptions). Integration into an SBT build is quite simple:

~~~ scala
lazy val Javadoc = config("genjavadoc") extend Compile

lazy val javadocSettings = inConfig(Javadoc)(Defaults.configSettings) ++ Seq(
  addCompilerPlugin("com.typesafe.genjavadoc" %% "genjavadoc-plugin" % "0.18" cross CrossVersion.full),
  scalacOptions += s"-P:genjavadoc:out=${target.value}/java",
  Compile / packageDoc := (Javadoc / packageDoc).value,
  Javadoc / sources :=
    (target.value / "java" ** "*.java").get ++
    (Compile / sources).value.filter(_.getName.endsWith(".java")),
  Javadoc / javacOptions := Seq(),
  Javadoc / packageDoc / artifactName := ((sv, mod, art) =>
    "" + mod.name + "_" + sv.binary + "-" + mod.revision + "-javadoc.jar")
)
~~~

To make it work, you must add the config and the settings to your
project.  One way to do this is to place the following line in your
`build.sbt` file:

~~~ scala
lazy val root = project.in(file(".")).configs(Javadoc).settings(javadocSettings: _*)
~~~

Adding `javadocSettings` to a `Project` this way will replace the
packaging of the API docs to use the Javadoc instead of the Scaladoc
(i.e. the `XY-javadoc.jar` will then contain Javadoc). The Scaladoc
can still be generated using the normal `doc` task, whereas the
Javadoc can now be generated using `genjavadoc:doc`.

GenJavadoc can also be integrated into a Maven build (inspired by [this answer on StackOverflow](http://stackoverflow.com/questions/12301620/how-to-generate-an-aggregated-scaladoc-for-a-maven-site/16288487#16288487)):

~~~ xml
<profile>
  <id>javadoc</id>
  <build>
    <plugins>
      <plugin>
        <groupId>net.alchim31.maven</groupId>
        <artifactId>scala-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>doc</id>
            <phase>generate-sources</phase>
            <goals>
              <goal>compile</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <args>
            <arg>-P:genjavadoc:out=${project.build.directory}/genjavadoc</arg>
          </args>
          <compilerPlugins>
            <compilerPlugin>
              <groupId>com.typesafe.genjavadoc</groupId>
              <artifactId>genjavadoc-plugin_${scala.binary.full.version}</artifactId>
              <version>0.11</version>
            </compilerPlugin>
          </compilerPlugins>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>build-helper-maven-plugin</artifactId>
        <executions>
          <execution>
            <phase>generate-sources</phase>
            <goals>
              <goal>add-source</goal>
            </goals>
            <configuration>
              <sources>
                <source>${project.build.directory}/genjavadoc</source>
              </sources>
            </configuration>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-javadoc-plugin</artifactId>
        <version>2.9</version>
        <configuration>
          <minmemory>64m</minmemory>
          <maxmemory>2g</maxmemory>
          <outputDirectory>${project.build.directory}</outputDirectory>
          <detectLinks>true</detectLinks>
        </configuration>
      </plugin>
    </plugins>
  </build>
</profile>
~~~

You can integrate genjavadoc with gradle build:

~~~ groovy
apply plugin: 'scala'

configurations {
  scalaCompilerPlugin
}

dependencies {
  // ...
  scalaCompilerPlugin "com.typesafe.genjavadoc:genjavadoc-plugin_${scalaFullVersion}:0.13"

}

tasks.withType(ScalaCompile) {
  scalaCompileOptions.with {
    additionalParameters = [
        "-Xplugin:" + configurations.scalaCompilerPlugin.asPath,
        "-P:genjavadoc:out=$buildDir/generated/java".toString()
    ]
  }
}

tasks.withType(Javadoc) {
  dependsOn("compileScala")
  source = [sourceSets.main.allJava, "$buildDir/generated/java"]
}
~~~

### Translation of Scaladoc comments

Comments found within the Scala sources are transferred to the corresponding Java sources including some modifications. These are necessary since Scaladoc supports different mark-up elements than Javadoc. The modifications are:

 * `{{{ ... }}}` is translated to `<pre><code> ... </code></pre>`, where within the pre-formatted text the following are represented by their HTML entities: `@`, `<`, `>`
 * typographic quotes (double as well as single) are translated to `&rdquo;` and friends
 * `@see [[ ... ]]` is translated to `@see ...`, but only if on a line on its own
 * `[[ ... ]]` is translated to `{@link ... }`
 * `<p>` tokens are placed between paragraphs, collapsing empty lines beforehand
 * words between backticks are placed between `<code> ... </code>` instead

### Additional transformations

Some additional transformations are applied in order to make the generated Javadoc more useful:

* if a Scala method or class was marked `@scala.annotation.deprecated` an equivalent Javadoc `@deprecated` comment line will be inserted to the resulting Javadoc comment.

## How it Works

Scaladoc generation is done by a special variant of the Scala compiler, which can in principle emit different output, but the syntax parsed by the Scaladoc code is the Scala one: the compiler phases which adapt the AST to be more Java-like (to emit JVM byte-code in the end) are not run. On the other hand source comments cannot easily be associated with method signatures parsed from class files, and generating corresponding Java code to be fed into the `javadoc` tool is also no small task.

The approach taken here is to use the Scala compiler’s support as far as possible and then generate mostly valid Java code corresponding to the AST—but only the class and method structur without implementations. Since the Javadoc shall contain generic type information, and shall also not be confused by artifacts of Scala’s encoding of traits and other things, the AST must be inspected before the “erasure” phase; due to Java’s flat method parameter lists the other bound on where to hook into the transformation is that it should be after the “uncurry” phase (which transforms `def f(x: Int)(s: String)` into `def f(x: int, s: String)`). Luckily there is a gap between those two phases which is just wide enough to squeeze some code in.

One drawback of this choice is that the flattening of classes and companion objects or the mixing-in of method implementations from traits into derived classes happens much later in the transformation pipeline, meaning that the compiler plugin has to do that transformation itself; for this it has the advantage that it does not need to be 100% Scala-correct since the goal is just to document method signatures, not to implement all the intricacies of access widening and so on.

## Known Limitations

* Many Scaladoc tags and features are simply not supported by the javadoc tool and genjavadoc does not reimplement them:

     * `@note`, `@example`, `@group` etc. do not work and are errors in Javadoc 8, so they cannot be used
     * links to methods that use the overload disambiguation syntax will not work

* Classes and objects nested inside nested objects are emitted by Scalac in such a form that there is no valid Java syntax to produce the same binary names. This is due to differences in name mangling (where javac inserts more dollar signs than scalac does). This means that while Javadoc is generated for these (in order to guarantee that Javadoc will find all the types it needs) the precise names are neither correct nor usable in practice.

* The Java code produced by genjavadoc is 'just correct enough' to be understood by javadoc. It is not a goal of this project to emit correct Java code. On recent version of the JDK you will have to pass the `--ignore-source-errors` flag to make the javadoc tool accept our output.

## Reporting Bugs

If you find errors in the generation process or have suggestions on how to improve the quality of the emitted Javadoc, please report issues on this GitHub repo’s issue tracker.

### Sponsored by Lightbend

Responsible: Dr. Roland Kuhn ([@rkuhn](https://github.com/rkuhn))

Maintained by [Akka Team](https://github.com/orgs/akka/teams/akka-team) (mostly by [@2m](https://github.com/2m), [@ktoso](https://github.com/ktoso))

Feel free to ping above maintainers for code review or discussions. 
Pull requests are very welcome–thanks in advance!
