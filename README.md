# GenJavaDoc

This project’s goal is the creation of real JavaDoc for Scala projects. While ScalaDoc—the native API documentation format of Scala—has several benefits over JavaDoc, Java programmers are very much used to having access to API documentation in a syntax matching their programming language of choice. Another motivating factor may be that javadoc-JARs are supported within IDE, e.g. showing tooltip help texts.

## How to Use It

GenJavaDoc is a Scala compiler plugin which emits structurally equivalent Java code for all Scala sources of a project, keeping the ScalaDoc comments (with a few format adaptions). Integration into an SBT build is quite simple:

~~~ scala
val JavaDoc = config("genjavadoc") extend Compile

val javadocSettings = inConfig(JavaDoc)(Defaults.configSettings) ++ Seq(
  libraryDependencies += compilerPlugin("com.typesafe.genjavadoc" %%
    "genjavadoc-plugin" % "0.4" cross CrossVersion.full),
  scalacOptions <+= target map (t => "-P:genjavadoc:out=" + (t / "java")),
  packageDoc in Compile <<= packageDoc in JavaDoc,
  sources in JavaDoc <<=
    (target, compile in Compile, sources in Compile) map ((t, c, s) =>
      (t / "java" ** "*.java").get ++ s.filter(_.getName.endsWith(".java"))),
  javacOptions in JavaDoc := Seq(),
  artifactName in packageDoc in JavaDoc :=
    ((sv, mod, art) =>
      "" + mod.name + "_" + sv.binary + "-" + mod.revision + "-javadoc.jar")
)
~~~

Adding `javadocSettings` to a `Project` will replace the packaging of the API docs to use the JavaDoc instead of the ScalaDoc (i.e. the `XY-javadoc.jar` will then contain JavaDoc). The ScalaDoc can still be generated using the normal `doc` task, whereas the JavaDoc can be generated using `genjavadoc:doc`.

### Translation of ScalaDoc comments

Comments found within the Scala sources are transferred to the corresponding Java sources including some modifications. These are necessary since ScalaDoc supports different mark-up elements than JavaDoc. The modifications are:

 * `{{{ ... }}}` is translated to `<pre><code> ... </code></pre>`, where within the pre-formatted text the following are represented by their HTML entities: `@`, `<`, `>`
 * typographic quotes (double as well as single) are translated to `&rdquo;` and friends
 * `@see [[ ... ]]` is translated to `@see ...`, but only if on a line on its own
 * `[[ ... ]]` is translated to `{@link ... }`
 * `<p>` tokens are placed between paragraphs, collapsing empty lines beforehand
 * words between backticks are placed between `<code> ... </code>` instead

## How it Works

ScalaDoc generation is done by a special variant of the Scala compiler, which can in principle emit different output, but the syntax parsed by the ScalaDoc code is the Scala one: the compiler phases which adapt the AST to be more Java-like (to emit JVM byte-code in the end) are not run. On the other hand source comments cannot easily be associated with method signatures parsed from class files, and generating corresponding Java code to be fed into the `javadoc` tool is also no small task.

The approach taken here is to use the Scala compiler’s support as far as possible and then generate mostly valid Java code corresponding to the AST—but only the class and method structur without implementations. Since the JavaDoc shall contain generic type information, and shall also not be confused by artifacts of Scala’s encoding of traits and other things, the AST must be inspected before the “erasure” phase; due to Java’s flat method parameter lists the other bound on where to hook into the transformation is that it should be after the “uncurry” phase (which transforms `def f(x: Int)(s: String)` into `def f(x: int, s: String)`). Luckily there is a gap between those two phases which is just wide enough to squeeze some code in.

One drawback of this choice is that the flattening of classes and companion objects or the mixing-in of method implementations from traits into derived classes happens much later in the transformation pipeline, meaning that the compiler plugin has to do that transformation itself; for this it has the advantage that it does not need to be 100% Scala-correct since the goal is just to document method signatures, not to implement all the intricacies of access widening and so on.

## Reporting Bugs

If you find errors in the generation process or have suggestions on how to improve the quality of the emitted JavaDoc contents, please report issues on this github project’s issue tracker.

## License

This software is licensed under the Apache 2 license.

### Sponsored by Typesafe

Responsible: Dr. Roland Kuhn
