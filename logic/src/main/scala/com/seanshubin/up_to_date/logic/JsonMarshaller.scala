package com.seanshubin.up_to_date.logic

trait JsonMarshaller {
  def toJson[T](theObject: T): String

  def fromJson[T](json: String, theClass: Class[T]): T

  def fromJsonArray[T](json: String, theElementClass: Class[T]): Seq[T]

  def normalize(json: String): String

  def merge(aObject: AnyRef, bObject: AnyRef): AnyRef
}
