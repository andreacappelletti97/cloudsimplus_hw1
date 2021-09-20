package Simulations
import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.sun.jdi.Value
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModel, UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudsimplus.builders.tables.CsvTable
import org.cloudsimplus.listeners.EventListener
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.cloudbus.cloudsim.power.models.PowerModelHostSimple;

import scala.collection.JavaConverters.*
import collection.JavaConverters.*
import java.io.PrintStream


class PowerSimulation

object PowerSimulation:
  //Init the config file to get static params
  val config = ObtainConfigReference("cloudSimulator") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  //Init the logger
  val logger = CreateLogger(classOf[BasicCloudSimPlusExample])
  //Define the base config reference in the Json config file
  val configReference = "powerSimulation."

  //Recursive function to populate PEs
  def populatePes(pesList : Seq[PeSimple],n : Integer) : Seq[PeSimple] = {
    if(n==0) return pesList
    else return populatePes(pesList :+ PeSimple(
      config.getLong(configReference + "host.mipsCapacity")),
      n-1)
  }

  //Recursive function to populate Hosts
  def populateHost(hostList: Seq[HostSimple], n : Integer, pesList: Seq[PeSimple]) : Seq[HostSimple] = {
    if(n==0) return hostList
    else
      val newHostSimple = HostSimple(
        config.getLong(configReference + "host.RAMInMBs"),
        config.getLong(configReference + "host.BandwidthInMBps"),
        config.getLong(configReference + "host.StorageInMBs")
        ,pesList.asJava)

      val ramProvisioner = new ResourceProvisionerSimple();
      val bwProvisioner = new ResourceProvisionerSimple();
      val vmScheduler = new VmSchedulerTimeShared

      //Set resources policies
      newHostSimple.setRamProvisioner(ramProvisioner)
      .setBwProvisioner(bwProvisioner)
      .setVmScheduler(vmScheduler)
      //Create a power model
      val powerModel = new PowerModelHostSimple(
        config.getDouble(configReference + "host.maxPower"),
        config.getDouble(configReference + "host.staticPower"));
      powerModel.setStartupDelay(config.getDouble(configReference + "host.startUpDelay"))
      .setShutDownDelay(config.getDouble(configReference + "host.shutDownDelay"))
      .setStartupPower(config.getDouble(configReference + "host.startUpPower"))
      .setShutDownPower(config.getDouble(configReference + "host.shutDownPower"));
      newHostSimple.setPowerModel(powerModel)
      newHostSimple.setId(1)
      //Enable utilization stats
      newHostSimple.enableUtilizationStats()
      return populateHost(hostList :+ newHostSimple, n-1, pesList)
  }

  //Recursive function to populate VMs
  def populateVms(vmList : Seq[Vm], n : Integer) : Seq[Vm] = {
    if(n==0) return vmList
    else
      val newVm = VmSimple(
        config.getDouble(configReference +"vm.mipsCapacity"),
        config.getLong(configReference +"vm.vmPes"))
        .setRam(config.getLong(configReference +"vm.RAMInMBs"))
        .setBw(config.getLong(configReference +"vm.BandwidthInMBps"))
        .setSize(config.getLong(configReference +"vm.StorageInMBs"))
        .setCloudletScheduler(new CloudletSchedulerTimeShared)
        newVm.enableUtilizationStats()
      return populateVms(
      vmList :+ newVm
      ,
      n-1)
  }

  def populateCloudlets(cloudletsList : Seq[Cloudlet], n : Integer, utilizationModel : UtilizationModel) : Seq[Cloudlet] = {
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
          .setUtilizationModelCpu(new UtilizationModelFull())
          .setUtilizationModelRam(utilizationModel)
          .setUtilizationModelBw(utilizationModel)
      , n - 1,
      utilizationModel
    )
  }

  def printHostPowerConsumption(hostList: Seq[HostSimple], n : Integer): Unit ={
    System.out.println("*********")
    System.out.println("Power consumption computation")
    if(n<0) return
    else
      val host = hostList(n)
      val cpuStats = host.getCpuUtilizationStats();
      //The total Host's CPU utilization for the time specified by the map key
      val utilizationPercentMean = cpuStats.getMean();
      val watts = host.getPowerModel().getPower(utilizationPercentMean);
      System.out.printf(
      "Host %2d CPU Usage mean: %6.1f%% | Power Consumption mean: %8.0f W%n",
        host.getId(), utilizationPercentMean * 100, watts);
  }

  def printVmPowerConsumption(vmList : Seq[Vm], n : Integer): Unit ={
    System.out.println("*********")
    System.out.println("Power consumption computation")
    if(n<0) return
    else
      val vm = vmList(n)
      val myPower = vm.getHost.getPowerModel.getPower(1)
      val powerModel = vm.getHost.getPowerModel
      System.out.println("PowerModel "  + myPower)
      val hostStaticPower = 35
      val hostStaticPowerByVm = hostStaticPower / vm.getHost().getVmCreatedList().size()
      System.out.println("A: 35 "  + hostStaticPowerByVm)
      val vmRelativeCpuUtilization = vm.getCpuUtilizationStats().getMean() / vm.getHost().getVmCreatedList().size();
      System.out.println("B: "  + vmRelativeCpuUtilization)
      val vmPower = powerModel.getPower(vmRelativeCpuUtilization) - hostStaticPower + hostStaticPowerByVm; // W
      val cpuStats = vm.getCpuUtilizationStats();
      System.out.printf(
      "Vm   %2d CPU Usage Mean: %6.1f%% | Power Consumption Mean: %8.0f W%n",
      vm.getId(), cpuStats.getMean() *100, vmPower);
      return printVmPowerConsumption(vmList, n-1)
  }


  def Start() =

    logger.debug("Running the simulation...")
    val cloudsim = CloudSim()
    //Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets
    val broker0 = DatacenterBrokerSimple(cloudsim)

    //Init the data from the config
    val pesNumber : Integer = config.getInt(configReference + "pesNumber")
    val hostNumber : Integer = config.getInt(configReference + "host.number")
    val vmNumber : Integer = config.getInt(configReference + "vm.number")
    val cloudletsNumber : Integer = config.getInt(configReference + "cloudlet.number")

    // First time the list must be empty
    val newPesList: Seq[PeSimple] = Seq.empty[PeSimple]
    val newHostList: Seq[HostSimple] = Seq.empty[HostSimple]
    val newVmList : Seq[Vm] = Seq.empty[Vm]
    val newCloudletsList : Seq[Cloudlet] = Seq.empty[Cloudlet]

    //Recursively build the pesList
    val pesList : Seq[PeSimple] = populatePes(newPesList, pesNumber)
    logger.info(s"Created $pesNumber processing element: $pesList")

    //Recursively build the hostList
    val hostList: Seq[HostSimple] = populateHost (newHostList ,hostNumber, pesList)
    logger.info(s"Created $hostNumber host: $hostList")

    //Create datacenter
    val dc0 = DatacenterSimple(cloudsim, hostList.asJava, new VmAllocationPolicySimple)
      .setSchedulingInterval(config.getDouble(configReference + "dcSchedulingInterval"));
    logger.info(s"Created a new datacenter: $dc0")

    //Recursively build the vmList
    val vmList : Seq[Vm] = populateVms(newVmList, vmNumber)
    logger.info(s"Created $vmNumber virtual machine: $vmList")

    //UtilizationModel defining the Cloudlets use only 50% of any resource all the time
    val utilizationModel = new UtilizationModelDynamic(
      config.getDouble(configReference +  "utilizationRatio"));

    //Recursively build the cloudletsList
    val cloudletsList : Seq[Cloudlet] = populateCloudlets(
     newCloudletsList, cloudletsNumber, utilizationModel)
    logger.info(s"Created a list of cloudlets: $cloudletsList")

    broker0.submitVmList(vmList.asJava)
    broker0.submitCloudletList(cloudletsList.asJava)

    logger.info("Starting cloud simulation...")
    cloudsim.start()

    //Get the CloudLets finished list from the broker
    val finishedCloudlets = broker0.getCloudletFinishedList()
    CloudletsTableBuilder(finishedCloudlets).build()

    printHostPowerConsumption(hostList, hostNumber - 1)
