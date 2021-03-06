package com.seanshubin.up_to_date.logic

import org.scalatest.{FunSuite, ShouldMatchers}

class DependencyTest extends FunSuite with ShouldMatchers {
  test("simple version comparison") {
    val later = Dependency(".", "joda-time", "joda-time", "2.3")
    val earlier = Dependency(".", "joda-time", "joda-time", "1.4")
    earlier should be < later
  }
}
