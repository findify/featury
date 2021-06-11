package io.findify.featury.model

case class BackendError(msg: String) extends Throwable(msg)
