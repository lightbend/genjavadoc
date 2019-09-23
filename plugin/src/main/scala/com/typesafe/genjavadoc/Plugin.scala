package com.typesafe.genjavadoc

import scala.tools.nsc
import nsc.Global
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import nsc.transform.Transform
import java.io.File
import java.util.Properties


object GenJavadocPlugin {

  val javaKeywords = Set("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
    "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
    "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
    "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
    "throw", "throws", "transient", "try", "void", "volatile", "while")

  private val defaultFilterString = "$$"

  def stringToFilter(s: String): Set[String] = s.split(",").toSet

  val defaultFilteredStrings = stringToFilter(defaultFilterString)
}

class GenJavadocPlugin(val global: Global) extends Plugin {
  import GenJavadocPlugin._

  val name = "genjavadoc"
  val description = ""
  val components: List[PluginComponent] =
    if (global.settings.isScaladoc) List.empty
    else List(MyComponent)

  override def init(options: List[String], error: String => Unit): Boolean = {
    myOptions = new Properties()
    options foreach { str =>
      str.indexOf('=') match {
        case -1 => myOptions.setProperty(str, "true")
        case n  => myOptions.setProperty(str.substring(0, n), str.substring(n + 1))
      }
    }
    true
  }

  private var myOptions: Properties = _

  lazy val outputBase = new File(myOptions.getProperty("out", "."))
  lazy val suppressSynthetic = java.lang.Boolean.parseBoolean(myOptions.getProperty("suppressSynthetic", "true"))
  lazy val filteredStrings: Set[String] = stringToFilter(myOptions.getProperty("filter", defaultFilterString))
  lazy val fabricateParams = java.lang.Boolean.parseBoolean(myOptions.getProperty("fabricateParams", "true"))
  lazy val strictVisibility = java.lang.Boolean.parseBoolean(myOptions.getProperty("strictVisibility", "false"))
  lazy val allowedAnnotations: Set[String] = stringToFilter(myOptions.getProperty("annotations", ""))

  private object MyComponent extends PluginComponent with Transform {

    import global._

    type GT = GenJavadocPlugin.this.global.type

    override val global: GT = GenJavadocPlugin.this.global

    private[this] val Version = s"""(\\d+)\\.(\\d+)\\.(\\d+).*""".r
    private[this] val (min, pat) = nsc.Properties.versionNumberString match {
      case Version("2", b, c) => (b.toInt, c.toInt)
      case v =>
        reporter.warning(NoPosition, s"Unexpected Scala version in GenJavadoc: $v")
        (-1, -1)
    }

    override val runsAfter: List[String] = List(if (min <= 11) "uncurry" else "fields")

    // This used to be `Nil`. In 2.12.9 and earlier, and also 2.13.0, the phase assembly algorithm
    // would in fact place the genjavadoc phase much later than `fields`, after `specialize`
    // (see https://github.com/scala/scala-dev/issues/647#issuecomment-525650681).
    //
    // This was never intended, so we tried adding `runsBefore tailcalls`. However, because the
    // compiler's own phase have ambiguous ordering, the 2.12.9 / 2.13.0 algorithm changed the order
    // of other phases (see https://github.com/lightbend/genjavadoc/pull/191#issuecomment-532185154).
    //
    // In 2.12.10 and 2.13.1, the algorithm was updated (scala/scala#8393), and setting
    // `runsBefore tailcalls` is now possible. The order of the compiler's own phases is made
    // unambiguous in scala/scala#8426 and 8427.
    override val runsBefore: List[String] =
      if (min == 12 && pat >= 10 || min == 13 && pat >= 1 || min > 13) List("tailcalls")
      else Nil

    val phaseName = "GenJavadoc"

    def newTransformer(unit: CompilationUnit) = new GenJavadocTransformer(unit)

    class GenJavadocTransformer(val unit: CompilationUnit) extends Transformer with TransformCake {

      override lazy val global: GT = MyComponent.this.global
      override val outputBase: File = GenJavadocPlugin.this.outputBase
      override val suppressSynthetic: Boolean = GenJavadocPlugin.this.suppressSynthetic
      override val fabricateParams: Boolean = GenJavadocPlugin.this.fabricateParams
      override val strictVisibility: Boolean = GenJavadocPlugin.this.strictVisibility

      override def superTransformUnit(unit: CompilationUnit) = super.transformUnit(unit)
      override def superTransform(tree: Tree) = super.transform(tree)
      override def transform(tree: Tree) = newTransform(tree)
      override def transformUnit(unit: CompilationUnit) = newTransformUnit(unit)
      override def javaKeywords = GenJavadocPlugin.javaKeywords
      override def filteredStrings = GenJavadocPlugin.this.filteredStrings

      override def allowedAnnotations: Set[String] = GenJavadocPlugin.this.allowedAnnotations
    }
  }
}
