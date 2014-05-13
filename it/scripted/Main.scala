package it.scripted

import org.powerbot.script._
import org.powerbot.script.rt6._
import java.awt.{Color, Graphics}
import org.powerbot.script.rt6.ClientContext
import it.scripted.sdp.Strategy


/**
 * Created by Andrew on 12/05/14.
 */
@Script.Manifest(name = "Wine Drinker", description = "Drinks wine", properties = "")
class Main extends PollingScript[ClientContext] with PaintListener {
  val wine = 1993
  val tasks = Array(new Bank, new Drink)
  var task:Strategy = _

  def poll = for(t <- tasks if t.active; task = t) t.execute

  implicit class Context(id: Int) { def toItem = ctx.groundItems.select.id(id) }

  private def sleep(i: => Boolean, t: Long, p: Int = 0) = {
    val end = System.currentTimeMillis + t
    while(System.currentTimeMillis < end && i) Condition.sleep(150)
    Condition.sleep(p)
  }

  class Bank extends Strategy("Banking.") {
    override def active = ctx.bank.opened || wine.toItem.count == 0
    override def execute {
      if(wine.toItem.count == 0) {
        if(ctx.bank.opened())
          if(ctx.backpack.select.count == 0) {
            if(wine.toItem.poll.stackSize > 0) {
              println("Banking")
              ctx.bank.withdraw(wine, Bank.Amount.ALL)
            } else {
              println("Stopping.")
              ctx.controller.stop()
            }
          } else {
            ctx.bank.depositInventory()
          }
        else
          if(ctx.bank.open()) sleep(!ctx.bank.opened, 5000)
      } else {
        if(ctx.bank.opened) ctx.bank.close()
      }
    }
  }

  class Drink extends Strategy("Drinking wine.") {
    override def active = wine.toItem.count > 0 && !ctx.bank.opened
    override def execute {
      val wines = wine.toItem.count
      if(wines >= 1) {
        val win = wine.toItem.peek
        if(win.interact("Drink")) sleep(wines == wine.toItem.count, 1500, 600)
      }
    }
  }

  def repaint(g: Graphics) {
    g.setColor(Color.BLACK)
    g.fillRect(0,0,100,30)
    g.setColor(Color.WHITE)
    g.drawString(task.task, 15, 15)
  }
}
