package io.findify.featury.model

trait KeyOps { this: Key =>
  def fqdn = s"${ns.value}/${tag.scope.name}/${name.value}"
}
