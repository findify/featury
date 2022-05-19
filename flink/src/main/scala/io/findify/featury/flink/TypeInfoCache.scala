package io.findify.featury.flink

import io.findify.featury.model.Key.Tenant
import io.findify.featury.model.{FeatureValue, Key, Scalar, State, StateKey, TimeValue, Write}
import io.findify.flinkadt.api._
import org.apache.flink.api.common.typeinfo.TypeInformation

object TypeInfoCache {
  implicit lazy val writeTI: TypeInformation[Write]       = deriveTypeInformation[Write]
  implicit lazy val tenantTI: TypeInformation[Tenant]     = deriveTypeInformation[Tenant]
  implicit lazy val keyTI: TypeInformation[Key]           = deriveTypeInformation[Key]
  implicit lazy val fvTI: TypeInformation[FeatureValue]   = deriveTypeInformation[FeatureValue]
  implicit lazy val scalarTI: TypeInformation[Scalar]     = deriveTypeInformation[Scalar]
  implicit lazy val stateTI: TypeInformation[State]       = deriveTypeInformation[State]
  implicit lazy val tvTI: TypeInformation[TimeValue]      = deriveTypeInformation[TimeValue]
  implicit lazy val stateKeyTI: TypeInformation[StateKey] = deriveTypeInformation[StateKey]
}
