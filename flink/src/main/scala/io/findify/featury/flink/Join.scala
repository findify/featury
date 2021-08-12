package io.findify.featury.flink

import io.findify.featury.model.Key.Scope
import io.findify.featury.model.{FeatureValue, ScopeKey}

/** Defines on how a feature join should be performed. Feature join is a well-known CQL-like
  * inner join of a user-specified left part (for example, a session class for a customer or whatever) with a set of
  * computed feature values. Join is performed by a key defined in the `key` function.
  *
  * Note that the result of the join is also the left part type T. So it's like
  * join(left, List(feature_values)) => left
  *
  * @tparam L left part of the join
  */
trait Join[T] extends Serializable {

  /** Take the left part and all the corresponding feature values, and fold them together into the output type T
    * @param self
    * @param values
    * @return
    */
  def join(left: T, values: List[FeatureValue]): T

  /** build the left part join key: we need to know things like namespace, scope and so on,
    * which is unclear how it's defined in the type L
    * @param left the left part
    * @param scope current scope
    * @return
    */
  def key(left: T, scope: Scope): ScopeKey

}
