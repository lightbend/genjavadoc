package akka.rk.buh.is.it;
// no position
public  class EWMA$ implements scala.Serializable {
  /**
   * Static reference to the singleton instance of this Scala object.
   */
  public static final EWMA$ MODULE$ = null;
  public   EWMA$ () { throw new RuntimeException(); }
  /**
   * Calculate the alpha (decay factor) used in {@link akka.cluster.EWMA}
   * from specified half-life and interval between observations.
   * Half-life is the interval over which the weights decrease by a factor of two.
   * The relevance of each data sample is halved for every passing half-life duration,
   * i.e. after 4 times the half-life, a data sample&amp;rsquo;s relevance is reduced to 6% of
   * its original relevance. The initial relevance of a data sample is given by
   * 1 &#x2013; 0.5 ^ (collect-interval / half-life).
   * @param halfLife (undocumented)
   * @param collectInterval (undocumented)
   * @return (undocumented)
   */
  public  double alpha (scala.concurrent.duration.FiniteDuration halfLife, scala.concurrent.duration.FiniteDuration collectInterval) { throw new RuntimeException(); }
  // not preceding
  public  akka.rk.buh.is.it.EWMA apply (double value, double alpha) { throw new RuntimeException(); }
  public  scala.Option<scala.Tuple2<java.lang.Object, java.lang.Object>> unapply (akka.rk.buh.is.it.EWMA x$0) { throw new RuntimeException(); }
  private  java.lang.Object readResolve () { throw new RuntimeException(); }
}
