package wordCount

package com.wordcount
import akka.actor.{ ActorSystem, Actor, Props }
import akka.actor.ActorRef
import scala.io.Source

class Parent(child: ActorRef) extends Actor {

  var totalWordCount = 0
  def receive = {
    case "EOF" =>
      println(s"Number of words in given file  is $totalWordCount")
    case filePath: String =>
      for (line <- Source.fromFile(filePath).getLines()) {
        child ! line
      }
      child ! "EOF"
    case countPerLine: Int => {
      totalWordCount = totalWordCount + countPerLine
    }
  }
}

class child extends Actor {
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
  val system = ActorSystem("HelloSystem")
  val child = system.actorOf(Props[child], "child")
  val parent = system.actorOf(Props(new Parent(child)), "parent")
  val file = "test_file.txt"
  parent ! file
}
