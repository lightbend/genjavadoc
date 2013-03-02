package scala.javadoc

import org.scalatest.WordSpec
import org.scalatest.matchers.{ MustMatchers, Matcher, MatchResult }
import java.net.URLClassLoader
import java.io.File

class SignatureSpec extends WordSpec with MustMatchers {

  "The generated java files" must {

    "contain the same methods and classes as the original Scala files" in {
      val scalaCL = new URLClassLoader(Array(new File("tests/target/scala-2.10/test-classes/").toURI.toURL), classOf[List[_]].getClassLoader)

      val accProtLvl = Map(1 -> 1, 2 -> 3, 4 -> 2)

      def check(jc: Class[_]) {
        val sc = scalaCL.loadClass(jc.getName)

        def matchJava(j: Set[String]) = Matcher { (s: Traversable[String]) ⇒
          MatchResult(s == j, s"$s did not match $j (in $jc)", s"$s matched $j (in $jc)")
        }

        val jm = getMethods(jc)
        val sm = getMethods(sc)
        printIfNotEmpty(sm -- jm, "missing methods:")
        printIfNotEmpty(jm -- sm, "extraneous methods:")
        sm must matchJava(jm)

        val jsub = getClasses(jc)
        val ssub = getClasses(sc)
        printIfNotEmpty(ssub.keySet -- jsub.keySet, "missing classes:")
        printIfNotEmpty(jsub.keySet -- ssub.keySet, "extraneous classes:")
        ssub.keySet must matchJava(jsub.keySet)

        for (n ← ssub.keys) {
          val js = jsub(n)
          val ss = ssub(n)

          def beEqual[T](t: T) = Matcher { (u: T) ⇒ MatchResult(u == t, s"$u was not equal $t (in $n)", s"$u was equal $t (in $n)") }
          def beAtLeast(t: Int) = Matcher { (u: Int) ⇒ MatchResult(u >= t, s"$u was < $t (in $n)", s"$u was >= $t (in $n)") }

          (js.getModifiers & ~7) must beEqual(ss.getModifiers & ~7)
          accProtLvl(js.getModifiers & 7) must beAtLeast(accProtLvl(ss.getModifiers & 7))
          js.getInterfaces.toList.map(_.getName) must beEqual(ss.getInterfaces.toList.map(_.getName))
          js.getSuperclass.getName must beEqual(ss.getSuperclass.getName)
          check(js)
        }
      }

      def printIfNotEmpty(s: Set[String], msg: String): Unit = if (s.nonEmpty) {
        println(msg)
        s.toList.sorted foreach println
      }

      def getMethods(c: Class[_]): Set[String] = {
        import language.postfixOps
        c.getDeclaredMethods filterNot (_.getName contains '$') map (_.toGenericString) toSet
      }

      def getClasses(c: Class[_]): Map[String, Class[_]] = {
        import language.postfixOps
        Map() ++ c.getDeclaredClasses.filterNot(_.getName contains "anon").map(x ⇒ x.getName -> x)
      }

      check(classOf[akka.rk.buh.is.it.A])
    }

  }

}