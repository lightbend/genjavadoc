package scala.javadoc

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import java.net.URLClassLoader
import java.io.File

class SignatureSpec extends WordSpec with MustMatchers {
  
  "The generated java files" must {
    
    "contain the same methods and classes as the original Scala files" in {
      val scalaCL = new URLClassLoader(Array(new File("tests/target/scala-2.10/test-classes/").toURI.toURL), ClassLoader.getSystemClassLoader)
      
      def check(jc: Class[_]) {
        val sc = scalaCL.loadClass(jc.getName)
        val jm = jc.getDeclaredMethods.map(x => x.getName + x.toGenericString).toSet
        val sm = sc.getDeclaredMethods.map(x => x.getName + x.toGenericString).toSet
        printIfNotEmpty(sm -- jm, "missing methods:")
        printIfNotEmpty(jm -- sm, "extraneous methods:")
        if (sm != jm) fail("mismatch")
      }
      
      def printIfNotEmpty(s: Set[String], msg: String): Unit = if (s.nonEmpty) {
        println(msg)
        s.toList.sorted foreach println
      }
      
      check(classOf[akka.rk.buh.is.it.A])
    }
    
  }

}