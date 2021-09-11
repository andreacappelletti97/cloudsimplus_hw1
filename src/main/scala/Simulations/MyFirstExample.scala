package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.BasicCloudSimPlusExample.{logger}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}

object MyFirstExample:
  val config = ObtainConfigReference("cloudSimulator") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  val logger = CreateLogger(classOf[BasicCloudSimPlusExample])
  def Start() =
    val cloudsim = new CloudSim()
    //What is a broker ?
    val broker0 = new DatacenterBrokerSimple(cloudsim)

    val hostPes = List(new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")))
    //logger.info(s"Created one processing element: $hostPes")





class MyFirstExample
