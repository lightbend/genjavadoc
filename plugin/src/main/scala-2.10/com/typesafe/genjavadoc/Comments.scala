package com.typesafe.genjavadoc

trait Comments extends BaseComments { this: TransformCake =>

  override def parseComments(): Unit =
    new parser.UnitParser(unit) {
      override def newScanner = new parser.UnitScanner(unit) {
        // This is for 2.10
        override def foundComment(text: String, start: Int, end: Int) {
          val pos = global.rangePos(source, start, start, end)
          comments += pos -> Comment(pos, text)
        }
      }
    }.parse()
}
