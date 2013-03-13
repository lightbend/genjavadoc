package com.typesafe.genjavadoc

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import nsc.transform.{ Transform, TypingTransformers }
import java.io.File
import java.util.Properties
import java.io.StringReader

class GenJavaDocPlugin(val global: Global) extends Plugin {
  import global._

  val name = "genjavadoc"
  val description = ""
  val components = List[PluginComponent](MyComponent)

  override def processOptions(options: List[String], error: String ⇒ Unit): Unit = {
    this.options = new Properties()
    options foreach { str ⇒
      str.indexOf('=') match {
        case -1 ⇒ this.options.setProperty(str, "true")
        case n  ⇒ this.options.setProperty(str.substring(0, n), str.substring(n + 1))
      }
    }
  }
  var options: Properties = _

  lazy val outputBase = new File(options.getProperty("out", "."))
  lazy val suppressSynthetic = java.lang.Boolean.parseBoolean(options.getProperty("suppressSynthetic", "true"))

  private object MyComponent extends PluginComponent with Transform {

    import global._
    import global.definitions._

    type GT = GenJavaDocPlugin.this.global.type

    override val global: GT = GenJavaDocPlugin.this.global

    override val runsAfter = List("uncurry")
    val phaseName = "GenJavaDoc"

    def newTransformer(unit: CompilationUnit) = new GenJavaDocTransformer(unit)

    class GenJavaDocTransformer(val unit: CompilationUnit) extends Transformer with TransformCake {

      override lazy val global: GT = MyComponent.this.global
      override val outputBase: File = GenJavaDocPlugin.this.outputBase
      override val suppressSynthetic: Boolean = GenJavaDocPlugin.this.suppressSynthetic

      override def superTransformUnit(unit: CompilationUnit) = super.transformUnit(unit)
      override def superTransform(tree: Tree) = super.transform(tree)
      override def transform(tree: Tree) = newTransform(tree)
      override def transformUnit(unit: CompilationUnit) = newTransformUnit(unit)

    }
  }
}
