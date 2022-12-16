import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HubImpact {

    String hubIdentifier;

    float impactValue;

    public String getHubIdentifier() {
        return hubIdentifier;
    }

    public float getImpactValue() {
        return impactValue;
    }

    public void setHubIdentifier(String hubIdentifier) {
        this.hubIdentifier = hubIdentifier;
    }

    public void setImpactValue(float impactValue) {
        this.impactValue = impactValue;
    }

}
