package wordCount

package com.wordcount

import scala.io.Source
import akka.pattern._
import akka.actor._
import akka.actor.ActorRef
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
/**
 * This is parent class which is used for file processing and accumulate number of words in file
 */
class Parent extends Actor {

  var fileSender: Option[ActorRef] = None
  val child = context.actorOf(Props[Child], "child")
  var totalWordCount = 0
  def receive = {
    case "EOF" =>
      fileSender.get ! totalWordCount
    case filePath: String =>
      fileSender = Some(sender)
      for (line <- Source.fromFile(filePath).getLines()) {
        child ! line
      }
      child ! "EOF"
    case countPerLine: Int => {
      totalWordCount = totalWordCount + countPerLine
    }
  }
}

/**
 * This is Child class which is used for count number words per line.
 */
class Child extends Actor {
  var wordCountPerLine = 0
  def receive = {
    case "EOF" =>
      sender ! "EOF"
    case line: String =>
      {
        wordCountPerLine = line.split(" ").size
        sender ! wordCountPerLine
      }
  }
}

object WordCount extends App {
  implicit val timeoutcustom = Timeout(10000)
  val system = ActorSystem("HelloSystem")
  val parent = system.actorOf(Props(new Parent()), "parent")
  val file = "test_file.txt"
  val result = parent ? file
  result onComplete {
    case Success(msg) =>
      println("\n\nNumber of words in given file " + file + s" is $msg\n\n")
      system.shutdown
    case Failure(f) =>
      println("fail: " + f)
      system.shutdown
  }
}
