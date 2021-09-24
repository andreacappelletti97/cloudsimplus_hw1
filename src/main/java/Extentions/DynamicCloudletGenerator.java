package Extentions;

import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.distributions.NormalDistr;
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

    private static final ContinuousDistribution random = new UniformDistr() ;

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

    //this algorithm proposed by D. Knuth
    //https://en.wikipedia.org/wiki/Poisson_distribution#Generating_Poisson-distributed_random_variables
/*
    private static int getPoissonRandom(double mean) {
        Random r = new Random();
        double L = Math.exp(-mean);
        int k = 0;
        double p = 1.0;
        do {
            p = p * r.nextDouble();
            k++;
        } while (p > L);
        return k - 1;
    }
*/


    public static Cloudlet createCloudlet() {
        UtilizationModel utilizationModel = new UtilizationModelDynamic(0.2);
        CloudletSimple cloudletSimple =  new CloudletSimple(1000, 1);
               cloudletSimple.setFileSize(1024);
        cloudletSimple.setOutputSize(1024);
        cloudletSimple.setUtilizationModelCpu(new UtilizationModelFull());
        cloudletSimple.setUtilizationModelRam(utilizationModel);
        cloudletSimple.setUtilizationModelBw(utilizationModel);
        cloudletSimple.setSubmissionDelay(1);
        return cloudletSimple;
    }




}
