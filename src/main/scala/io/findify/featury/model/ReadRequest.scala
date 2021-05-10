package io.findify.featury.model

case class ReadRequest(tenant: Int, group: String, keys: List[String], features: List[String]) {}
