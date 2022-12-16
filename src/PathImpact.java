import java.util.ArrayList;
import java.util.List;

public class PathImpact {
    List<HubImpact> path = new ArrayList<>();
    Double totalImpact;


    public List<HubImpact> getPath(){
        return path;
    }

    public Double getTotalImpact() {
        return totalImpact;
    }

    public void setPath(List<HubImpact> path) {
        this.path = path;
    }

    public void setTotalImpact(Double totalImpact) {
        this.totalImpact = totalImpact;
    }
}
