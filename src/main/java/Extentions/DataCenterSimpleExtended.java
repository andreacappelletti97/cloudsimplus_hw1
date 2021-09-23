package Extentions;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;

import java.util.List;

public class DataCenterSimpleExtended extends DatacenterSimple {

    private int locality;
    
    public DataCenterSimpleExtended(Simulation simulation, List<? extends Host> hostList, int locality) {
        super(simulation, hostList);
        this.locality = locality;
    }

    public int getLocality() {
        return locality;
    }

    public void setLocality(int locality) {
        this.locality = locality;
    }
}
