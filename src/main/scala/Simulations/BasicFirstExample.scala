package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.vms.VmSimple

import collection.JavaConverters.*

class BasicFirstExample

object BasicFirstExample:
  //Init the config file to get static params
  val config = ObtainConfigReference("cloudSimulator") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  //Init the logger
  val logger = CreateLogger(classOf[BasicCloudSimPlusExample])


  //Recursive function to populate PEs
  def populatePes(pesList : Seq[PeSimple],n : Integer) : Seq[PeSimple] = {
    if(n==0) return pesList
    else return populatePes(pesList :+ PeSimple(
      config.getLong("basicFirstExample.host.mipsCapacity")),
      n-1)
  }

  //Recursive function to populate Hosts
  def populateHost(hostList: Seq[HostSimple], n : Integer, pesList: Seq[PeSimple]) : Seq[HostSimple] = {
  if(n==0) return hostList
  else return populateHost(hostList :+  HostSimple(
    config.getLong("basicFirstExample.host.RAMInMBs"),
    config.getLong("basicFirstExample.host.StorageInMBs"),
    config.getLong("basicFirstExample.host.BandwidthInMBps"),
    pesList.asJava),
    n-1,
    pesList)
  }


  /*
  //Recursive function to populate VMs
  def populateVms(vmList : Seq[VmSimple], n : Integer, pesList: Seq[PeSimple]) : Seq[VmSimple] = {
    if(n==0) return vmList
    else return populateVms(
      vmList :+ VmSimple(config.getLong("basicFirstExample.vm.mipsCapacity"), pesList.length)
        .setRam(config.getLong("basicFirstExample.vm.RAMInMBs"))
        .setBw(config.getLong("basicFirstExample.vm.BandwidthInMBps"))
        .setSize(config.getLong("basicFirstExample.vm.StorageInMBs"))
      ),
      n-1
    )
  }
*/

  def Start() =
    val cloudsim = CloudSim()
    //Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets
    val broker0 = DatacenterBrokerSimple(cloudsim)

    //Init the data from the config
    val pesNumber : Integer = config.getInt("basicFirstExample.pesNumber")
    val hostNumber : Integer = config.getInt("basicFirstExample.host.number")
    val vmNumber : Integer = config.getInt("basicFirstExample.vm.number")

    // First time the list must be empty
    val newPesList: Seq[PeSimple] = Seq.empty[PeSimple]
    val newHostList: Seq[HostSimple] = Seq.empty[HostSimple]
    val newVmList : Seq[VmSimple] = Seq.empty[VmSimple]

    //Recursively build the pesList and hostList
    val pesList : Seq[PeSimple] = populatePes(newPesList, pesNumber)
    logger.info(s"Created one processing element: $pesList")
    val hostList: Seq[HostSimple] = populateHost (newHostList ,hostNumber, pesList)
    logger.info(s"Created host list: $hostList")

    val dc0 = DatacenterSimple(cloudsim, hostList.asJava)

    //val vmList : Seq[VmSimple] = populateVms(newVmList, vmNumber)








