package Extentions;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

import javax.xml.crypto.Data;
import java.util.List;

public class BrokerSimpleExtension extends Extensions.DataCenterBrokerExtended {

        final boolean APPLY_LOCALITY_POLICY = true;
    /**
     * Index of the last VM selected from the {@link #getVmExecList()}
     * to run some Cloudlet.
     */
    private int lastSelectedVmIndex;

    /**
     * Index of the last Datacenter selected to place some VM.
     */
    private int lastSelectedDcIndex;

    /**
     * Creates a new DatacenterBroker.
     *
     * @param simulation the CloudSim instance that represents the simulation the Entity is related to
     */
    public BrokerSimpleExtension(final CloudSim simulation) {
        this(simulation, "");
    }

    /**
     * Creates a DatacenterBroker giving a specific name.
     *
     * @param simulation the CloudSim instance that represents the simulation the Entity is related to
     * @param name the DatacenterBroker name
     */
    public BrokerSimpleExtension(final CloudSim simulation, final String name) {
        super(simulation, name);
        this.lastSelectedVmIndex = -1;
        this.lastSelectedDcIndex = -1;
    }

    @Override
    public DatacenterBroker submitCloudletList(List<? extends Cloudlet> list) {
        return super.submitCloudletList(list);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>It applies a Round-Robin policy to cyclically select
     * the next Datacenter from the list. However, it just moves
     * to the next Datacenter when the previous one was not able to create
     * all {@link #getVmWaitingList() waiting VMs}.</p>
     *
     * <p>This policy is just used if the selection of the closest Datacenter is not enabled.
     * Otherwise, the {@link #closestDatacenterMapper(Datacenter, Vm)} is used instead.</p>
     *
     * @param lastDatacenter {@inheritDoc}
     * @param vm {@inheritDoc}
     * @return {@inheritDoc}
     * @see DatacenterBroker#setDatacenterMapper(java.util.function.BiFunction)
     * @see #setSelectClosestDatacenter(boolean)
     */

    @Override
    protected Datacenter defaultDatacenterMapper(final Datacenter lastDatacenter, final Vm vm) {
        if(getDatacenterList().isEmpty()) {
            throw new IllegalStateException("You don't have any Datacenter created.");
        }

        if (lastDatacenter != Datacenter.NULL) {
            return getDatacenterList().get(lastSelectedDcIndex);
        }

        /*If all Datacenter were tried already, return Datacenter.NULL to indicate
         * there isn't a suitable Datacenter to place waiting VMs.*/
        if(lastSelectedDcIndex == getDatacenterList().size()-1){
            return Datacenter.NULL;
        }

        for(Datacenter datacenterWrapper : getDatacenterList()){
            if(datacenterWrapper instanceof DataCenterSimpleExtended){
                DataCenterSimpleExtended dataCenterSimpleExtended = (DataCenterSimpleExtended) datacenterWrapper;
            }
        }

        int  lastSelectedDcIndexReal = ++lastSelectedDcIndex;
        Datacenter datacenter = getDatacenterList().get(lastSelectedDcIndexReal);
        if(datacenter instanceof DataCenterSimpleExtended) {
            DataCenterSimpleExtended dataCenterSimpleExtended = (DataCenterSimpleExtended) getDatacenterList().get(lastSelectedDcIndexReal);
        }
        return datacenter;
    }


    private Vm selectVmLocality(List<Vm> vmList, Cloudlet cloudlet) {
        if (cloudlet instanceof CloudletExtension) {
            CloudletExtension cloudletExtension = (CloudletExtension) cloudlet;
            for (Vm vmWrapper : vmList) {
                //System.out.println(" VM list size is " + vmList.size());
                DataCenterSimpleExtended dataCenterSimpleExtendedddd = (DataCenterSimpleExtended) vmWrapper.getHost().getDatacenter();
                //System.out.println("MY DC is: " + vmWrapper.getHost().getDatacenter().getId());
                //System.out.println("MY DC locality is: " + dataCenterSimpleExtendedddd.getLocality());
               // System.out.println("MY Host is: " + vmWrapper.getHost().getId());
               // System.out.println("MY id is: " + vmWrapper.getId());
                Datacenter datacenter = vmWrapper.getHost().getDatacenter();
                if (datacenter instanceof DataCenterSimpleExtended) {
                    DataCenterSimpleExtended dataCenterSimpleExtended = (DataCenterSimpleExtended) datacenter;
                    if ((dataCenterSimpleExtended.getLocality() == cloudletExtension.getLocality())) {
                    //    System.out.println("GOT LOCALITY");
                        return vmWrapper;
                    }
                }


            } return null;

        } return null;
    }



    @Override
    protected Vm defaultVmMapper(final Cloudlet cloudlet) {
        if (cloudlet.isBoundToVm()) {
            return cloudlet.getVm();
        }

        if (getVmExecList().isEmpty()) {
            return Vm.NULL;
        }


        lastSelectedVmIndex = ++lastSelectedVmIndex % getVmExecList().size();
        Vm vm = null;
        if(APPLY_LOCALITY_POLICY) {
             vm = selectVmLocality(getVmCreatedList(), cloudlet);
            if (vm == null) {
                vm = getVmFromCreatedList(lastSelectedVmIndex);
            }
        } else {
             vm = getVmFromCreatedList(lastSelectedVmIndex);
        }

        if(vm instanceof VmSimple){
            VmSimple vmSimple = (VmSimple) vm;
        }

        /* Add a penality in seconds if the cloudlet is not executed in the same locality */
        if(cloudlet instanceof CloudletExtension){
            CloudletExtension cloudletExtension = (CloudletExtension) cloudlet;
            Datacenter datacenter = vm.getHost().getDatacenter();
            if(datacenter instanceof DataCenterSimpleExtended){
                DataCenterSimpleExtended dataCenterSimpleExtended = (DataCenterSimpleExtended) datacenter;
                if(( dataCenterSimpleExtended.getLocality() != cloudletExtension.getLocality())){
                   // System.out.println("NOT THE SAME locality ");
                    System.out.println(dataCenterSimpleExtended.getLocality());
                    System.out.println(cloudletExtension.getLocality());
                    cloudletExtension.setSubmissionDelay(120);
                }
            }
        }
        return vm;
    }
}
