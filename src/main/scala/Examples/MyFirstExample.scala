package Examples

import HelperUtils.{CreateLogger, ObtainConfigReference}
import Examples.BasicCloudSimPlusExample
import Examples.MyFirstExample.{config, logger}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.vms.VmSimple

import scala.collection.JavaConverters.*

object MyFirstExample:
  //Init the config file to get static params
  val config = ObtainConfigReference("simulation1") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  //Init the logger
  val logger = CreateLogger(classOf[BasicCloudSimPlusExample])

  def Start() =
    val cloudsim = new CloudSim()
    //What is a broker ? --> provider of the datacenter
    val broker0 = new DatacenterBrokerSimple(cloudsim)

    //Create a processor element (CORE)
    val hostPes = List(new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")))
    logger.info(s"Created one processing element: $hostPes")

    //Instanciate an host
      val hostList = List(new HostSimple(config.getLong("cloudSimulator.host.RAMInMBs"),
      config.getLong("cloudSimulator.host.StorageInMBs"),
      config.getLong("cloudSimulator.host.BandwidthInMBps"),
      hostPes.asJava))

    logger.info(s"Created one host: $hostList")
    //Create the new datacenter
      val dc0 = new DatacenterSimple(cloudsim, hostList.asJava);
    //Create VMs
    val vmList = List(
      new VmSimple(config.getLong("cloudSimulator.vm.mipsCapacity"), hostPes.length)
        .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
        .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
        .setSize(config.getLong("cloudSimulator.vm.StorageInMBs"))
    )
      logger.info(s"Created one virtual machine: $vmList")






class MyFirstExample
