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

  override def processOptions(options: List[String], error: String => Unit): Unit = {
    myOptions = new Properties()
    options foreach { str =>
      str.indexOf('=') match {
        case -1 => myOptions.setProperty(str, "true")
        case n  => myOptions.setProperty(str.substring(0, n), str.substring(n + 1))
      }
    }
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

    val isPreFields =
      nsc.Properties.versionNumberString.startsWith("2.11.")
    override val runsAfter = List(if(isPreFields) "uncurry" else "fields")
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
