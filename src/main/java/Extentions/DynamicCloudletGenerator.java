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
import java.util.Random;

public final class DynamicCloudletGenerator {


    public DynamicCloudletGenerator() {
    }

    private static final Random random = new Random() ;
    private static final ContinuousDistribution continuosDistribution = new UniformDistr() ;
    private static final double poissonDistributionMean = 0.6;
    private static final int cloudletMips = 1000;
    private static final int cloudletSize = 612;
    private static final int cloudletPes = 4;
    private static final double utilizationRatio = 0.2;

    public static EventListener<EventInfo> createRandomCloudlets(List<Cloudlet> cloudletList, DatacenterBrokerSimple broker) {
        return new EventListener<EventInfo>() {
            @Override
            public void update(EventInfo info) {
                if(continuosDistribution.sample() <= 0.3) {
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
    //Compute the delay arrival with a Poisson distribution
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

    /* Gaussian distribution to select the Cloudlet params */
    private static int getGaussianRandom(int mean, int std) {
        return  (int)   random.nextGaussian() * std + mean;
    }

    private static int getIntervalSplit(int interval){
        return  (int) interval/2;
    }
    private static int getStdOctave(int std){
        return  (int)  std/8;
    }

    public static Cloudlet createCloudlet() {
        UtilizationModel utilizationModel = new UtilizationModelDynamic(utilizationRatio);
        CloudletSimple cloudletSimple =  new CloudletSimple(getGaussianRandom(getIntervalSplit(cloudletMips), getStdOctave(cloudletMips)), getGaussianRandom(getIntervalSplit(cloudletPes), getStdOctave(cloudletPes)));
               cloudletSimple.setFileSize(getGaussianRandom(getIntervalSplit(cloudletSize), getStdOctave(cloudletSize)));
        cloudletSimple.setOutputSize(getGaussianRandom(getIntervalSplit(cloudletSize), getStdOctave(cloudletSize)));
        cloudletSimple.setUtilizationModelCpu(utilizationModel);
        cloudletSimple.setUtilizationModelRam(utilizationModel);
        cloudletSimple.setUtilizationModelBw(utilizationModel);
        cloudletSimple.setSubmissionDelay(getPoissonRandom(poissonDistributionMean));
        return cloudletSimple;
    }




}
