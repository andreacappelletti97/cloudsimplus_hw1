package Extentions;

import org.cloudbus.cloudsim.core.AbstractMachine;
import org.cloudbus.cloudsim.vms.AbstractResourceStats;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class AbstractResourceStatsExtension extends AbstractResourceStats {

    private Map<Double, Double> utilizationArray;


    public AbstractResourceStatsExtension(AbstractMachine machine, Function resourceUtilizationFunction) {
        super(machine, resourceUtilizationFunction);
        this.utilizationArray = new TreeMap<>();
    }



    public Map<Double, Double> getUtilizationArray() {
        return utilizationArray;
    }

    public void setUtilizationArray(Map<Double, Double> utilizationArray) {
        this.utilizationArray = utilizationArray;
    }
}
