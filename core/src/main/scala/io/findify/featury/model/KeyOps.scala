package io.findify.featury.model

trait KeyOps { this: Key =>
  def fqdn = s"${tag.scope.name}/${name.value}"
}
