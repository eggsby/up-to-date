package com.seanshubin.up_to_date.logic

import org.scalatest.FunSuite
import org.scalatest.mock.EasyMockSugar

class ConfigurationValidatorTest extends FunSuite with EasyMockSugar {
  test("at least one command line argument required") {
    val stubFileSystem: FileSystem = null
    val stubJsonMarshaller: JsonMarshaller = null
    val configurationValidator: ConfigurationValidator = new ConfigurationValidatorImpl(stubFileSystem, stubJsonMarshaller)
    val actual = configurationValidator.validate(Seq())
    val expected = Left(Seq("at least one command line argument required"))
    assert(expected === actual)
  }

  test("no more than one command line argument allowed") {
    val stubFileSystem: FileSystem = null
    val stubJsonMarshaller: JsonMarshaller = null
    val configurationValidator: ConfigurationValidator = new ConfigurationValidatorImpl(stubFileSystem, stubJsonMarshaller)
    val actual = configurationValidator.validate(Seq("too", "many"))
    val expected = Left(Seq("no more than one command line argument allowed"))
    assert(expected === actual)
  }

  test("configuration file must exist") {
    val stubJsonMarshaller: JsonMarshaller = null
    val fileSystem: FileSystem = mock[FileSystem]
    val configurationValidator: ConfigurationValidator = new ConfigurationValidatorImpl(fileSystem, stubJsonMarshaller)

    expecting {
      fileSystem.fileExists("file name").andReturn(false)
    }

    whenExecuting(fileSystem) {
      val actual = configurationValidator.validate(Seq("file name"))
      val expected = Left(Seq(s"file 'file name' does not exist"))
      assert(expected === actual)
    }
  }

  test("json must be in the correct shape") {
    val fileSystem: FileSystem = mock[FileSystem]
    val jsonMarshaller: JsonMarshaller = mock[JsonMarshaller]
    val configurationValidator: ConfigurationValidator = new ConfigurationValidatorImpl(fileSystem, jsonMarshaller)
    val malformedJsonString = "{  what! "
    val jsonError = new RuntimeException("Malformed json string")

    expecting {
      fileSystem.fileExists("file name").andReturn(true)
      fileSystem.loadFileIntoString("file name").andReturn(malformedJsonString)
      jsonMarshaller.fromJson(malformedJsonString, classOf[ConfigurationJson]).andThrow(jsonError)
    }

    whenExecuting(fileSystem, jsonMarshaller) {
      val actual = configurationValidator.validate(Seq("file name"))
      val expected = Left(Seq(s"Unable to read json from 'file name': Malformed json string"))
      assert(expected === actual)
    }
  }

  test("well formed json does not meet validation rules") {
    val fileSystem: FileSystem = mock[FileSystem]
    val jsonMarshaller: JsonMarshaller = mock[JsonMarshaller]
    val configurationValidator: ConfigurationValidator = new ConfigurationValidatorImpl(fileSystem, jsonMarshaller)
    val jsonString = "some json"
    val parsedFromJson = SampleData.configurationJsonComplete.copy(reportDirectory = None)

    expecting {
      fileSystem.fileExists("file name").andReturn(true)
      fileSystem.loadFileIntoString("file name").andReturn(jsonString)
      jsonMarshaller.fromJson(jsonString, classOf[ConfigurationJson]).andReturn(parsedFromJson)
    }

    whenExecuting(fileSystem, jsonMarshaller) {
      val actual = configurationValidator.validate(Seq("file name"))
      val expected = Left(List("reportDirectory must be specified"))
      assert(expected === actual)
    }
  }

  test("well formed json meets validation rules") {
    val fileSystem: FileSystem = mock[FileSystem]
    val jsonMarshaller: JsonMarshaller = mock[JsonMarshaller]
    val configurationValidator: ConfigurationValidator = new ConfigurationValidatorImpl(fileSystem, jsonMarshaller)
    val jsonString = "some json"
    val parsedFromJson = SampleData.configurationJsonComplete

    expecting {
      fileSystem.fileExists("file name").andReturn(true)
      fileSystem.loadFileIntoString("file name").andReturn(jsonString)
      jsonMarshaller.fromJson(jsonString, classOf[ConfigurationJson]).andReturn(parsedFromJson)
    }

    whenExecuting(fileSystem, jsonMarshaller) {
      val actual = configurationValidator.validate(Seq("file name"))
      val expected = Right(SampleData.validConfiguration)
      assert(expected === actual)
    }
  }
}