package org.example

private[example] class Clazz {
  private[this] def x = ()
  private[Clazz] def y = ()
  private[example] def z = ()
  private def t = ()
  def u = ()
}

class PublicClazz {
  private[this] def x = ()
  private[PublicClazz] def y = ()
  private[example] def z = ()
  private def t = ()
  def u = ()

  /* private[this] val does not generate accessor method */
  private[this] val v1 = ()
  private[PublicClazz] val v2 = ()
  private[example] val v3 = ()
  private val v4 = ()
  val v5 = ()
}
