package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.ReduceExample.{broker0, cloudsim, config, vmList}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import collection.JavaConverters.*

class ReduceExample

object ReduceExample:
  val config = ObtainConfigReference("cloudSimulator") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
    val logger = CreateLogger(classOf[BasicCloudSimPlusExample])

    def Start() =
  logger.info("Simulation has started...")
  //Init the cloudSim APIs
  val cloudsim = new CloudSim();
  //Creates a Broker that will act on behalf of a cloud user (customer).
  val broker0 = new DatacenterBrokerSimple(cloudsim);


  //Creates one Hosts with a specific list of CPU cores (PEs).
  logger.info("Retrieve mips from config file...")
  //System.out.println(config.getLong("reduceExample.host.mipsCapacity"))
  //Uses a PeProvisionerSimple by default to provision PEs for VMs
  val hostPes = List(
    new PeSimple(config.getLong("reduceExample.host.mipsCapacity"))
  )
  logger.info(s"Just created a PEs: $hostPes" )

  //Uses ResourceProvisionerSimple by default for RAM and BW provisioning
  //Uses VmSchedulerSpaceShared by default for VM scheduling
  val hostList = List(new HostSimple(config.getLong("reduceExample.host.RAMInMBs"),
        config.getLong("reduceExample.host.StorageInMBs"),
        config.getLong("reduceExample.host.BandwidthInMBps"),
      hostPes.asJava))

  val dc0 = new DatacenterSimple(cloudsim, hostList.asJava)

//Creates one Vm to run applications (Cloudlets).
  val vmList = List(
    new VmSimple(
      config.getLong("cloudSimulator.vm.mipsCapacity"), hostPes.length)
      .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
      .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
      .setSize(config.getLong("cloudSimulator.vm.StorageInMBs")))

//UtilizationModel defining the Cloudlets use only 50% of any resource all the time
  val utilizationModel = new UtilizationModelDynamic(config.getDouble("reduceExample.utilizationRatio"))


//Creates two Cloudlets that represent applications to be run inside a Vm. --> jobs
  val jobsList = List(
    new CloudletSimple(config.getLong("reduceExample.cloudlet.size"), 
      config.getInt("reduceExample.cloudlet.PEs"), 
      utilizationModel) ,
    new CloudletSimple(config.getLong("reduceExample.cloudlet.size"),
      config.getInt("reduceExample.cloudlet.PEs"),
      utilizationModel)
  )

  broker0.submitVmList(vmList.asJava);
  broker0.submitCloudletList(jobsList.asJava);

  cloudsim.start()
  new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();










