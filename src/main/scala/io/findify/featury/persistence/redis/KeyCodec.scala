package io.findify.featury.persistence.redis

import io.findify.featury.model.Key

object KeyCodec {
  implicit class KeyCodecExt(self: Key) {
    def toRedisKey(suffix: String): String = {
      s"${self.ns.value}/${self.tenant.value}/${self.group.value}/${self.featureName.value}/${self.id.value}/$suffix"
    }
  }
}
