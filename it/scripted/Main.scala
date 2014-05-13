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
  val Wine = 1993
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
    override def active = ctx.bank.opened || Wine.toItem.count == 0
    override def execute {
      if(Wine.toItem.count == 0)
        if(ctx.bank.opened())
          if(ctx.backpack.select.count == 0) if(Wine.toItem.poll.stackSize > 0) ctx.bank.withdraw(Wine, Bank.Amount.ALL) else ctx.controller.stop()
          else ctx.bank.depositInventory()
        else if(ctx.bank.open()) sleep(!ctx.bank.opened, 5000)
      else ctx.bank.close()
    }
  }

  class Drink extends Strategy("Drinking wine.") {
    override def active = Wine.toItem.count > 0 && !ctx.bank.opened
    override def execute {
      val wines = Wine.toItem.count
      if(wines >= 1) if(Wine.toItem.peek.interact("Drink")) sleep(wines == Wine.toItem.count, 1500, 600)
    }
  }

  def repaint(g: Graphics) {
    g.setColor(Color.BLACK)
    g.fillRect(0,0,100,30)
    g.setColor(Color.WHITE)
    g.drawString(task.task, 15, 15)
  }
}
