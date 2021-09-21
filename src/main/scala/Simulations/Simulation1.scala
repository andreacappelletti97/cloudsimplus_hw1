package Simulations
import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.sun.jdi.Value
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.datacenters.Datacenter
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

class Simulation1

object Simulation1 :
  //Init the config file to get static params
  val config = ObtainConfigReference("cloudSimulator") match {
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
      logger.info("Creating ...")
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
    if(n==0) return pesList
    else return populatePes(pesList :+ PeSimple(
      config.getLong(configReference + "dc" + dcId + ".host.mipsCapacity")),
      n-1,
      dcId)
  }

  //Recursive function to populate Hosts
  def populateHost(hostList: Seq[HostSimple], n : Integer, pesList: Seq[PeSimple], dcId:Integer) : Seq[HostSimple] = {
    if(n==0) return hostList
    else
      val newHostSimple = HostSimple(
        config.getLong(configReference + "dc" + dcId + ".host.RAMInMBs"),
        config.getLong(configReference + "dc" + dcId + ".host.BandwidthInMBps"),
        config.getLong(configReference + "dc" + dcId + ".host.StorageInMBs")
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
      config.getDouble(configReference +"dc" + dcId + ".host.maxPower"),
      config.getDouble(configReference + "dc" + dcId + ".host.staticPower"));
      powerModel.setStartupDelay(config.getDouble(configReference +"dc" + dcId + ".host.startUpDelay"))
      .setShutDownDelay(config.getDouble(configReference +"dc" + dcId + ".host.shutDownDelay"))
      .setStartupPower(config.getDouble(configReference + "dc" + dcId + ".host.startUpPower"))
      .setShutDownPower(config.getDouble(configReference +"dc" + dcId + ".host.shutDownPower"));
      newHostSimple.setPowerModel(powerModel)
      //Enable utilization stats
      newHostSimple.enableUtilizationStats()
      return populateHost(hostList :+ newHostSimple, n-1, pesList, dcId)
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
        .setSize(config.getLong(configReference +"vm.imageSize"))
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
          .setUtilizationModelCpu(utilizationModel)
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
      System.out.println("Samples collected: " + cpuStats.count)
      System.out.println("Samples collected: " + cpuStats)
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
      val hostStaticPower = 35
      val hostStaticPowerByVm = hostStaticPower / vm.getHost().getVmCreatedList().size()
      val vmRelativeCpuUtilization = vm.getCpuUtilizationStats().getMean() / vm.getHost().getVmCreatedList().size();
      val vmPower = powerModel.getPower(vmRelativeCpuUtilization) - hostStaticPower + hostStaticPowerByVm; // W
      val cpuStats = vm.getCpuUtilizationStats();
      System.out.printf(
        "Vm   %2d CPU Usage Mean: %6.1f%% | Power Consumption Mean: %8.0f W%n",
        vm.getId(), cpuStats.getMean() *100, vmPower);
    return printVmPowerConsumption(vmList, n-1)
  }

  def mapNetworkNodes(networkTopology: BriteNetworkTopology, dcList : Seq[Datacenter], n: Integer): Unit = {
    if(n == dcList.length - 1) return
    networkTopology.mapNode(dcList(n), n);
    return mapNetworkNodes(networkTopology, dcList, n + 1)
  }


  def configureNetwork(): BriteNetworkTopology = {
      //load the network topology file
      val networkTopology = BriteNetworkTopology.getInstance(config.getString(configReference + "networkTopology"));
      return networkTopology;
  }




  def Start() =

    logger.debug("Running the simulation...")
    val cloudsim = CloudSim()
    //Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets
    val broker0 = DatacenterBrokerSimple(cloudsim)

    //Init the data from the config
    val dcNumber : Integer = config.getInt(configReference + "dcNumber")
    //val pesNumber : Integer = config.getInt(configReference + "pesNumber")
    //val hostNumber : Integer = config.getInt(configReference + "host.number")
    val vmNumber : Integer = config.getInt(configReference + "vm.number")
    val cloudletsNumber : Integer = config.getInt(configReference + "cloudlet.number")

    // First time the list must be empty
    val newDcList: Seq[Datacenter] = Seq.empty[Datacenter]
    //val newPesList: Seq[PeSimple] = Seq.empty[PeSimple]
    //val newHostList: Seq[HostSimple] = Seq.empty[HostSimple]
    val newVmList : Seq[Vm] = Seq.empty[Vm]
    val newCloudletsList : Seq[Cloudlet] = Seq.empty[Cloudlet]


    val dcList : Seq[Datacenter] = populateDataCenter(
      cloudsim,
      newDcList,
      0,
      dcNumber
    )

    /*
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
 */


    //Set network topology
    val networkTopology = configureNetwork()
    cloudsim.setNetworkTopology(networkTopology)
    logger.info("Network topoloty loaded...")
    networkTopology.mapNode(dcList(0), 0);
    networkTopology.mapNode(dcList(1), 2);
    //Broker will correspond to BRITE node 3
    networkTopology.mapNode(broker0, 3);


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

    //printHostPowerConsumption(hostList, hostNumber - 1)
    printVmPowerConsumption(vmList, vmNumber - 1)




