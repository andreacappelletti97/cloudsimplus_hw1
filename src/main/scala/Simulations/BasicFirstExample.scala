package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModel, UtilizationModelDynamic}
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

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

  //Recursive function to populate VMs
  def populateVms(vmList : Seq[VmSimple], n : Integer) : Seq[VmSimple] = {
    if(n==0) return vmList
    else return populateVms(vmList :+ VmSimple(
      config.getLong("basicFirstExample.vm.mipsCapacity"),
        config.getLong("basicFirstExample.vm.vmPes"))
        .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
        .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
        .setSize(config.getLong("cloudSimulator.vm.StorageInMBs")),
      n-1
    )
  }

  def populateCloudlets(cloudletsList : Seq[Cloudlet], n : Integer, utilizationModel : UtilizationModel) : Seq[Cloudlet] = {
    if(n==0) return cloudletsList
    else return populateCloudlets(
      cloudletsList :+
        CloudletSimple(
          config.getLong("basicFirstExample.cloudlet.cloudletLength"),
          config.getInt("basicFirstExample.cloudlet.cloudletPes"),
          utilizationModel
        ).setSizes(config.getLong("basicFirstExample.cloudlet.cloudletSize"))
      , n - 1,
      utilizationModel
    )
  }


  def Start() =
    val cloudsim = CloudSim()
    //Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets
    val broker0 = DatacenterBrokerSimple(cloudsim)

    //Init the data from the config
    val pesNumber : Integer = config.getInt("basicFirstExample.pesNumber")
    val hostNumber : Integer = config.getInt("basicFirstExample.host.number")
    val vmNumber : Integer = config.getInt("basicFirstExample.vm.number")
    val cloudletsNumber : Integer = config.getInt("basicFirstExample.cloudlet.number")

    // First time the list must be empty
    val newPesList: Seq[PeSimple] = Seq.empty[PeSimple]
    val newHostList: Seq[HostSimple] = Seq.empty[HostSimple]
    val newVmList : Seq[VmSimple] = Seq.empty[VmSimple]
    val newCloudletsList : Seq[Cloudlet] = Seq.empty[Cloudlet]

    //Recursively build the pesList
    val pesList : Seq[PeSimple] = populatePes(newPesList, pesNumber)
    logger.info(s"Created $pesNumber processing element: $pesList")

    //Recursively build the hostList
    val hostList: Seq[HostSimple] = populateHost (newHostList ,hostNumber, pesList)
    logger.info(s"Created $hostNumber host: $hostList")

    //Create datacenter
    val dc0 = DatacenterSimple(cloudsim, hostList.asJava)

    //Recursively build the vmList
    val vmList : Seq[VmSimple] = populateVms(newVmList, vmNumber)
    logger.info(s"Created $vmNumber virtual machine: $vmList")

    //UtilizationModel defining the Cloudlets use only 50% of any resource all the time
    val utilizationModel = new UtilizationModelDynamic(
      config.getDouble("basicFirstExample.utilizationRatio"));

    //Recursively build the cloudletsList
    val cloudletsList : Seq[Cloudlet] = populateCloudlets(newCloudletsList, cloudletsNumber, utilizationModel)
    logger.info(s"Created a list of cloudlets: $cloudletsList")

    broker0.submitVmList(vmList.asJava)
    broker0.submitCloudletList(cloudletsList.asJava)

    logger.info("Starting cloud simulation...")
    cloudsim.start()

    CloudletsTableBuilder(broker0.getCloudletFinishedList()).build()







