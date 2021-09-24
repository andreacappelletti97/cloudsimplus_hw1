package Extentions;

import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.distributions.PoissonDistr;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.listeners.EventListener;
import scala.collection.Seq;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.distributions.ContinuousDistribution;

import java.util.List;

public final class DynamicCloudletGenerator {

    public DynamicCloudletGenerator() {
    }

    private static final ContinuousDistribution random  = new UniformDistr();

    public static EventListener<EventInfo> createRandomCloudlets(List<Cloudlet> cloudletList, DatacenterBrokerSimple broker) {
        return new EventListener<EventInfo>() {
            @Override
            public void update(EventInfo info) {
                if(random.sample() <= 0.3) {
                    //Dinamically generate new Cloudlets
                    System.out.println("Event Listener called...");
                    System.out.println("I'm going to dinamically generates cloudlets for the entire duration of the simulation.");
                    Cloudlet cloudlet = createCloudlet();
                    cloudletList.add(cloudlet);
                    broker.submitCloudlet(cloudlet);
                }
            }
        };
    }


    public static Cloudlet createCloudlet() {
        UtilizationModel um = new UtilizationModelDynamic(0.2);
        CloudletSimple cloudletSimple =  new CloudletSimple(1000, 1);
               cloudletSimple.setFileSize(1024);
        cloudletSimple.setOutputSize(1024);
        cloudletSimple.setUtilizationModelCpu(new UtilizationModelFull());
        cloudletSimple.setUtilizationModelRam(um);
        cloudletSimple.setUtilizationModelBw(um);
                cloudletSimple.setSubmissionDelay(1.0);
                return cloudletSimple;

    }




}
