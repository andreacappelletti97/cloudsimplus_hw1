package Extentions;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletAbstract;

import java.util.Objects;

public class CloudletExtension extends CloudletAbstract {

    private int locality;

    public CloudletExtension(long length, int pesNumber, int locality) {
        super(length, pesNumber);
        System.out.println("Cloudlet created...");
        this.locality = locality;
    }

    public int getLocality() {
        return locality;
    }

    public void setLocality(int locality) {
        this.locality = locality;
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
        final CloudletExtension other = (CloudletExtension) o;
        return other.getId() == getId() && getBroker().equals(other.getBroker());
    }


}
