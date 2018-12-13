package akka.rk.buh.is.it;
/**
 * The exponentially weighted moving average (EWMA) approach captures short-term
 * movements in volatility for a conditional volatility forecasting model. By virtue
 * of its alpha, or decay factor, this provides a statistical streaming data model
 * that is exponentially biased towards newer entries.
 * <p>
 * http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average
 * <p>
 * An EWMA only needs the most recent forecast value to be kept, as opposed to a standard
 * moving average model.
 * <p>
 * param:  alpha decay factor, sets how quickly the exponential weighting decays for past data compared to new data,
 *   see http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average
 * <p>
 * param:  value the current exponentially weighted moving average, e.g. Y(n - 1), or,
 *             the sampled value resulting from the previous smoothing iteration.
 *             This value is always used as the previous EWMA to calculate the new EWMA.
 * <p>
 */
public final class EWMA implements scala.Product, scala.Serializable {
  // not preceding
  static public  akka.rk.buh.is.it.EWMA apply (double value, double alpha) { throw new RuntimeException(); }
  static public  scala.Option<scala.Tuple2<java.lang.Object, java.lang.Object>> unapply (akka.rk.buh.is.it.EWMA x$0) { throw new RuntimeException(); }
  public  double value () { throw new RuntimeException(); }
  public  double alpha () { throw new RuntimeException(); }
  // not preceding
  public   EWMA (double value, double alpha) { throw new RuntimeException(); }
  /**
   * Calculates the exponentially weighted moving average for a given monitored data set.
   * <p>
   * @param xn the new data point
   * @return a new EWMA with the updated value
   */
  public  akka.rk.buh.is.it.EWMA $colon$plus (double xn) { throw new RuntimeException(); }
  // not preceding
  public  akka.rk.buh.is.it.EWMA copy (double value, double alpha) { throw new RuntimeException(); }
  // not preceding
  public  double copy$default$1 () { throw new RuntimeException(); }
  public  double copy$default$2 () { throw new RuntimeException(); }
  // not preceding
  public  java.lang.String productPrefix () { throw new RuntimeException(); }
  public  int productArity () { throw new RuntimeException(); }
  public  Object productElement (int x$1) { throw new RuntimeException(); }
  public  scala.collection.Iterator<java.lang.Object> productIterator () { throw new RuntimeException(); }
  public  boolean canEqual (Object x$1) { throw new RuntimeException(); }
  public  int hashCode () { throw new RuntimeException(); }
  public  java.lang.String toString () { throw new RuntimeException(); }
  public  boolean equals (Object x$1) { throw new RuntimeException(); }
}
