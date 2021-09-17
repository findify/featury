package io.findify.featury.flink

import io.findify.featury.model.Key.{Tag, Tenant}
import io.findify.featury.model.FeatureValue

/** Defines on how a feature join should be performed. Feature join is like a well-known CQL-like
  * inner join of a user-specified left part (for example, a session class for a customer or whatever) with a set of
  * computed feature values. Join is performed by a key defined in the `key` function.
  *
  * Note that the result of the join is also the left part type T. So it's like
  * join(left, List(feature_values)) => left
  *
  * The join itself is happening twice:
  * 1. At first we join the input left-part event with features by JoinKey (so by ns+tenant). It's happening because
  * our state is partitioned by ns+merchant.
  * 2. Then for each event we get all the tags we need, pull it from the state store, and invoke the .join callback
  * method with everything we have.
  *
  * @tparam L left part of the join
  */
trait Join[T] extends Serializable {

  /** build the left part join key: we need to know things like namespace, scope and so on,
    * which is unclear how it's defined in the type L
    * @param left the left part
    * @return
    */
  def by(left: T): Tenant

  /** Feature value keys to load
    * @param left
    * @return
    */
  def tags(left: T): List[Tag]

  /** Take the left part and all the corresponding feature values, and fold them together into the output type T
    * @param self
    * @param values
    * @return
    */
  def join(left: T, values: List[FeatureValue]): T

}
