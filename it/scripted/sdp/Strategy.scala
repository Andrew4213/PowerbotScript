package it.scripted.sdp

/**
 * Created by Andrew on 13/05/14.
 */
abstract case class Strategy(task: String) {
  def active:Boolean
  def execute
}
