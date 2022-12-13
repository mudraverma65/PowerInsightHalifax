import javax.swing.plaf.nimbus.State;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PowerService {

    List<PostalCode> postalCodes = new ArrayList<PostalCode>();

    List<DistributionHub> distributionHubs = new ArrayList<DistributionHub>();

    JDBCConnection conn = new JDBCConnection();

    boolean addPostalCode ( String postalCode, int population, int area ){
        try{
            PostalCode p1 = new PostalCode(postalCode,population,area);
            p1.addPost();
//            int index =0;
//            while(index<postalCodes.size()){
//                PostalCode tempPostal = postalCodes.get(index);
//                if(postalCode == tempPostal.postalCode && population == tempPostal.population){
//                    return false;
//                } else if (postalCode == tempPostal.postalCode && population != tempPostal.population) {
//                    postalCodes.remove(index);
//                }
//            }
//            postalCodes.add(p1);
        }
        catch(Exception e){
            System.out.println(e);
            //return false;
        }
        return true;
    }

    boolean addDistributionHub ( String hubIdentifier, Point location, Set<String> servicedAreas ){
        try {
            DistributionHub d1 = new DistributionHub(hubIdentifier, location, servicedAreas);
            d1.addHub();
//            int index = 0;
//            while (index < distributionHubs.size()) {
//                DistributionHub tempHub = distributionHubs.get(index);
//                if (hubIdentifier == tempHub.hubIdentifier) {
//                    return false;
//                }
//                distributionHubs.add(d1);
//            }
        }
        catch(Exception e){
            System.out.println(e);
            //return false;
        }
        return true;
    }

    void hubDamage (String hubIdentifier, float repairEstimate ){
        String query = "Insert into hubimpact (hubIdentifier, repairEstimate) values (?,?) on duplicate key update repairEstimate = ? ";
        try {
            PreparedStatement statement = conn.setupConnection().prepareStatement(query);
            statement.setString(1,hubIdentifier);
            statement.setFloat(2,repairEstimate);
            statement.setFloat(3,repairEstimate);
            statement.executeUpdate();
            conn.setupConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void hubRepair( String hubIdentifier, String employeeId, float repairTime, boolean inService ){
        Float timeNeeded = null;
        //String query = "update hubimpact set repairEstimate = repairEstimate - `repairTime` where hubIdentifier = `hubIdentifier`;";
        String query = "select hubIdentifier, repairEstimate from hubimpact where hubIdentifier = ?";
        try {
            PreparedStatement statement = conn.setupConnection().prepareStatement(query);
            statement.setString(1,hubIdentifier);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                //String currentHub = resultSet.getString(1);
                timeNeeded = resultSet.getFloat(2);
                timeNeeded = timeNeeded - repairTime;
                if (timeNeeded <= 0){
                    inService = true;
                }
            }
            if (inService == true){
                String query1 = "delete from hubimpact where hubIdentifier = `hubIdentifier`;";
                PreparedStatement state = conn.setupConnection().prepareStatement(query1);
                state.execute();
            }
            else if (inService == false){
                hubDamage(hubIdentifier,timeNeeded);
            }
            conn.setupConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    int peopleOutOfService (){

        int people = 0;

        String query_view = "alter view peopleServed as " +
        "select distributionhub.hubIdentifier, population * (1/count(hubpostal.postalCode)) as peopleServed "+
        "from distributionhub " +
        "left join hubpostal on distributionhub.hubIdentifier = hubpostal.hubIdentifier " +
        "left join postalCode on hubpostal.postalCode = postalcode.postalCode " +
        "group by distributionhub.hubIdentifier;";

        String update_table = "update distributionhub " +
        "inner join peopleServed on distributionhub.hubIdentifier = peopleServed.hubIdentifier " +
        "set distributionhub.peopleServed = peopleserved.peopleServed;";

        String find_people = "select peopleServed " +
        "from hubimpact " +
        "left join distributionhub on distributionhub.hubIdentifier = hubimpact.hubIdentifier;";

        try {
            PreparedStatement state1 = conn.setupConnection().prepareStatement(query_view);
            state1.execute();

            PreparedStatement state2 = conn.setupConnection().prepareStatement(update_table);
            state2.execute();

            Statement state_people = conn.setupConnection().createStatement();
            ResultSet rs = state_people.executeQuery(find_people);
            while(rs.next()){
                Integer peopleHub = rs.getInt(1);
                people = people + peopleHub;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return people;
    }

    List<DamagedPostalCodes> mostDamagedPostalCodes ( int limit ){

        return null;
    }

    List<HubImpact> fixOrder ( int limit ){

        return null;
    }

    List<Integer> rateOfServiceRestoration ( float increment){
        return null;
    }

    List<HubImpact> repairPlan ( String startHub, int maxDistance, float maxTime ){
        return null;
    }

    List<String> underservedPostalByPopulation ( int limit ){
        return null;
    }

    List<String> underservedPostalByArea (int limit ){
        return null;
    }


}
