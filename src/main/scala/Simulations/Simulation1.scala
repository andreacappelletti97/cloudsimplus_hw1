package Simulations
import Examples.BasicCloudSimPlusExample
import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.sun.jdi.Value
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModel, UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudsimplus.builders.tables.CsvTable
import org.cloudsimplus.listeners.EventListener
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.schedulers.vm.{VmScheduler, VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple
import org.cloudbus.cloudsim.power.models.PowerModel
import org.cloudbus.cloudsim.power.models.PowerModelHost
import org.cloudbus.cloudsim.power.models.PowerModelHostSimple

import scala.collection.JavaConverters.*
import collection.JavaConverters.*
import java.io.PrintStream

class Simulation1

object Simulation1 :
  //Init the config file to get static params
  val config = ObtainConfigReference("simulation1") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  //Init the logger
  val logger = CreateLogger(classOf[BasicCloudSimPlusExample])
  //Define the base config reference in the Json config file
  val configReference = "simulation1."

  def populateDataCenter(cloudsim: CloudSim, dcList : Seq[Datacenter], n : Integer, dcNumber : Integer) : Seq[Datacenter] = {
    if(n == dcNumber )
      logger.info(s"Created $dcNumber datacenters: $dcList")
      return dcList
    else
      logger.debug("Creating datacenters ...")
      val pesNumber : Integer = config.getInt(configReference + "dc" + n + ".pesNumber")
      val hostNumber : Integer = config.getInt(configReference + "dc" + n +".host.number")
      val newPesList: Seq[PeSimple] = Seq.empty[PeSimple]
      val newHostList: Seq[HostSimple] = Seq.empty[HostSimple]

      //Recursively build the pesList
      val pesList : Seq[PeSimple] = populatePes(newPesList, pesNumber, n)
      logger.info(s"Created $pesNumber processing element: $pesList")

      //Recursively build the hostList
      val hostList: Seq[HostSimple] = populateHost (newHostList ,hostNumber, pesList, n)
      logger.info(s"Created $hostNumber host: $hostList")
      //Create datacenter
      val dc = DatacenterSimple(cloudsim, hostList.asJava, new VmAllocationPolicySimple)
        .setSchedulingInterval(config.getDouble(configReference + "dcSchedulingInterval"));
      logger.info(s"Created a new datacenter: $dc")

      return populateDataCenter(cloudsim,
        dcList :+ dc,
        n + 1,
        dcNumber
      )
  }

  //Recursive function to populate PEs
  def populatePes(pesList : Seq[PeSimple],n : Integer, dcId: Integer) : Seq[PeSimple] = {
    logger.debug("Creating PEs ...")
    if(n==0) return pesList
    else return populatePes(pesList :+ PeSimple(
      config.getLong(configReference + "dc" + dcId + ".host.mipsCapacity")), n-1, dcId)
  }

  //Recursive function to populate Hosts
  def populateHost(hostList: Seq[HostSimple], n : Integer, pesList: Seq[PeSimple], dcId:Integer) : Seq[HostSimple] = {
    logger.debug("Creating Hosts ...")
    if(n==0) return hostList
    else
      val newHostSimple = HostSimple(
        config.getLong(configReference + "dc" + dcId + ".host.RAMInMBs"),
        config.getLong(configReference + "dc" + dcId + ".host.BandwidthInMBps"),
        config.getLong(configReference + "dc" + dcId + ".host.StorageInMBs")
        ,pesList.asJava)
      if(config.getBoolean(configReference + "dc" + dcId + ".host.timeSharedPolicy")){
        newHostSimple.setVmScheduler(new VmSchedulerTimeShared)
      } else {
        newHostSimple.setVmScheduler(new VmSchedulerSpaceShared)
      }
      //Set resources policies
      val ramProvisioner = new ResourceProvisionerSimple();
      val bwProvisioner = new ResourceProvisionerSimple();
      newHostSimple.setRamProvisioner(ramProvisioner)
      .setBwProvisioner(bwProvisioner)
      return populateHost(hostList :+ newHostSimple, n-1, pesList, dcId)
    }

  //Recursive function to populate VMs
  def populateVms(vmList : Seq[Vm], n : Integer) : Seq[Vm] = {
    logger.debug("Creating VMs ...")
    if(n==0) return vmList
    else
      val newVm = VmSimple(
        config.getDouble(configReference +"vm.mipsCapacity"),
        config.getLong(configReference +"vm.vmPes"))
        .setRam(config.getLong(configReference +"vm.RAMInMBs"))
        .setBw(config.getLong(configReference +"vm.BandwidthInMBps"))
        .setSize(config.getLong(configReference +"vm.imageSize"))
        if(config.getBoolean(configReference +"vm.timeSharedPolicy")) {
          newVm.setCloudletScheduler(new CloudletSchedulerTimeShared)
        } else {
          newVm.setCloudletScheduler(new CloudletSchedulerSpaceShared)
        }
      return populateVms(vmList :+ newVm, n-1)
  }

  def populateCloudlets(cloudletsList : Seq[Cloudlet], n : Integer, utilizationModel : UtilizationModel) : Seq[Cloudlet] = {
    logger.debug("Creating Cloudlets ...")
    if(n==0) return cloudletsList
    else return populateCloudlets(
      cloudletsList :+
        CloudletSimple(
          config.getLong(configReference + "cloudlet.length"),
          config.getInt(configReference+ "cloudlet.pesNumber"))
          //Set input and output sizes
          .setFileSize(config.getLong(configReference +"cloudlet.inputSize"))
          .setOutputSize(config.getLong(configReference +"cloudlet.outputSize"))
          //Set utilization models
          .setUtilizationModelCpu(utilizationModel)
          .setUtilizationModelRam(utilizationModel)
          .setUtilizationModelBw(utilizationModel)
      , n - 1,
      utilizationModel
    )
  }


  def Start() =
    logger.debug("Running the simulation...")
    val cloudsim = CloudSim()
    //Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets
    val broker0 = DatacenterBrokerSimple(cloudsim)
    //Init the data from the config
    val dcNumber : Integer = config.getInt(configReference + "dcNumber")
    val vmNumber : Integer = config.getInt(configReference + "vm.number")
    val cloudletsNumber : Integer = config.getInt(configReference + "cloudlet.number")

    // First time the list must be empty
    val newDcList: Seq[Datacenter] = Seq.empty[Datacenter]
    val newVmList : Seq[Vm] = Seq.empty[Vm]
    val newCloudletsList : Seq[Cloudlet] = Seq.empty[Cloudlet]

    //Populate the datacenters list
    val dcList : Seq[Datacenter] = populateDataCenter(cloudsim, newDcList, 0, dcNumber)

    //Recursively build the vmList
    val vmList : Seq[Vm] = populateVms(newVmList, vmNumber)
    logger.info(s"Created $vmNumber virtual machine: $vmList")

    //UtilizationModel defining the Cloudlets use 100% of any resource all the time
    val utilizationModel = new UtilizationModelDynamic(
      config.getDouble(configReference +  "utilizationRatio"));

    //Recursively build the cloudletsList
    val cloudletsList : Seq[Cloudlet] = populateCloudlets(
      newCloudletsList, cloudletsNumber, utilizationModel)
    logger.info(s"Created a list of cloudlets: $cloudletsList")

    //Submit the VMs and Cloudlets to the broker
    broker0.submitVmList(vmList.asJava)
    broker0.submitCloudletList(cloudletsList.asJava)

    //Starting cloud simulation
    logger.info("Starting cloud simulation...")
    cloudsim.start()

    //Get the CloudLets finished list from the broker
    val finishedCloudlets = broker0.getCloudletFinishedList()
    CloudletsTableBuilder(finishedCloudlets).build()


