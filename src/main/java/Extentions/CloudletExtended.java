package Extensions;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.cloudlets.CloudletAbstract;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import java.util.Objects;

/**
 * Cloudlet implements the basic features of an application/job/task to be executed
 * by a {@link Vm} on behalf of a given user. It stores, despite all the
 * information encapsulated in the Cloudlet, the ID of the VM running it.
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 * @see DatacenterBroker
 */
public class CloudletExtended extends CloudletAbstract {

    public enum Locality{
        ITALY,
        UNITED_STATES,
        JAPAN
    }

    private Locality locality;

    public CloudletExtended(final long length, final int pesNumber, final UtilizationModel utilizationModel, Locality locality) {
        super(length, pesNumber, utilizationModel);
        this.locality = locality;
    }

    public CloudletExtended(final long length, final int pesNumber, final UtilizationModel utilizationModel) {
        super(length, pesNumber, utilizationModel);
    }

    public CloudletExtended(final long length, final int pesNumber) {
        super(length, pesNumber);
    }

    public CloudletExtended(final long length, final long pesNumber) {
        super(length, pesNumber);
    }

    public CloudletExtended(final long id, final long length, final long pesNumber) {
        super(id, length, pesNumber);
    }

    @Override
    public String toString() {
        return String.format("Cloudlet %d", getId());
    }



    @Override
    public int compareTo(final Cloudlet o) {
        if(this.equals(Objects.requireNonNull(o))) {
            return 0;
        }

        return Double.compare(getLength(), o.getLength()) +
                Long.compare(this.getId(), o.getId()) +
                this.getBroker().compareTo(o.getBroker());
    }

    @Override
    public boolean equals(final Object o) {
        //Appropriated hashCode() is implemented by superclass
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CloudletExtended other = (CloudletExtended) o;
        return other.getId() == getId() && getBroker().equals(other.getBroker());
    }

    public Locality getLocality() {
        return locality;
    }

    public void setLocality(Locality locality) {
        this.locality = locality;
    }

}