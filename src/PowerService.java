import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PowerService {
    Service serviceHelp = new Service();
    JDBCConnection conn = new JDBCConnection();
    boolean addPostalCode(String postalCode, int population, int area) {
        //To create tables in the database
        serviceHelp.setTables();
        PostalCode p1 = null;
        try {
            p1 = new PostalCode(postalCode, population, area);
            boolean status;
            status = p1.addPost();

            //postal code already exists
            if (status == false){
                //method to update values
                status = p1.updatePost();
            }
        } catch (SQLException e) {
           return false;
        }
        return true;
    }

    boolean addDistributionHub(String hubIdentifier, Point location, Set<String> servicedAreas) {
        //object created
        DistributionHub d1 = new DistributionHub(hubIdentifier, location, servicedAreas);
        try {
            boolean status;
            status = d1.addHub();

            //distribution hub already exists
            if(status==false){
                status = d1.updateHub();
            }
        } catch (RuntimeException e) {
            return false;
        }
        return true;
    }

    void hubDamage(String hubIdentifier, float repairEstimate) {
        try {
            boolean status;
            //method to add hub into database
            status = serviceHelp.addImpact(hubIdentifier, repairEstimate);

            //hub exists hence update
            if(status==false){
                //method to update hub repair value
                status = serviceHelp.updateImpact(hubIdentifier, repairEstimate);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    void hubRepair(String hubIdentifier, String employeeId, float repairTime, boolean inService) {
        Float timeNeeded = null;
        String query = "select hubIdentifier, repairEstimate from hubimpact where hubIdentifier = ?";
        try {
            PreparedStatement statement = conn.setupConnection().prepareStatement(query);
            statement.setString(1, hubIdentifier);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                timeNeeded = resultSet.getFloat(2);
                //Subtracting repair done from repair estimate
                timeNeeded = timeNeeded - repairTime;

                //hub is functional since no repairs needed
                if (timeNeeded <= 0) {
                    inService = true;
                }
            }
            //Hub is functional. Deleting records from hubimpact table
            if (inService == true) {
                String query1 = "delete from hubimpact where hubIdentifier = ?";
                PreparedStatement state = conn.setupConnection().prepareStatement(query1);
                state.setString(1, hubIdentifier);
                state.execute();
            }
            //Partial repairs done
            else if (inService == false) {
                hubDamage(hubIdentifier, timeNeeded);
            }
            conn.setupConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    int peopleOutOfService() {

        int people = 0;

        serviceHelp.setServed();

        String find_people = "select peopleServed " +
                "from hubimpact " +
                "left join distributionhub on distributionhub.hubIdentifier = hubimpact.hubIdentifier;";

        try {
            Statement state_people = conn.setupConnection().createStatement();
            ResultSet rs = state_people.executeQuery(find_people);
            while (rs.next()) {
                //Finding number of people served by each hub and adding it into total
                Integer peopleHub = rs.getInt(1);
                people = people + peopleHub;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return people;
    }

    List<DamagedPostalCodes> mostDamagedPostalCodes(int limit) {

        List<DamagedPostalCodes> damagedPostalCodes = new ArrayList<>();

        String queryDamage = "insert ignore into damagedpostalcodes (postalCode, repairEstimate)\n" +
                "select hubpostal.postalCode, sum(repairEstimate)\n" +
                "from hubimpact\n" +
                "left join hubpostal on hubpostal.hubIdentifier = hubimpact.hubIdentifier\n" +
                "group by postalCode;";


        String queryLDamage = "select * from damagedpostalcodes \n" +
                "order by repairEstimate desc ";

        try {
            //Query to find damaged postal codes and their repairEstimate
            PreparedStatement state1 = conn.setupConnection().prepareStatement(queryDamage);
            state1.execute();

            //Query to sort the table in descending order
            PreparedStatement statement = conn.setupConnection().prepareStatement(queryLDamage);
            ResultSet rs = statement.executeQuery();

            //Adding only limit values
            while (rs.next() && damagedPostalCodes.size()<limit) {
                String postal1 = rs.getString(1);
                Float repair1 = rs.getFloat(2);
                DamagedPostalCodes d1 = new DamagedPostalCodes();
                d1.setPostalCode(postal1);
                d1.setRepairEstimate(repair1);
                damagedPostalCodes.add(d1);
                //Check for a tie situation on last limit element
                while(damagedPostalCodes.size() == limit){
                    rs.next();
                    if(rs.getFloat(2) == repair1){
                        String postal = rs.getString(1);
                        Float repair = rs.getFloat(2);
                        DamagedPostalCodes d2 = new DamagedPostalCodes();
                        d1.setPostalCode(postal);
                        d1.setRepairEstimate(repair);
                        //Adding value to the list and incrementing limit
                        damagedPostalCodes.add(d1);
                        limit ++;
                    }
                }
            }

        } catch (SQLException e) {
            return damagedPostalCodes;
        }
        return damagedPostalCodes;
    }

    List<HubImpact> fixOrder(int limit) {

        List<HubImpact> hubImpact = new ArrayList<>();

        String queryImpact = "update hubimpact \n" +
                "inner join distributionhub on hubimpact.hubIdentifier = distributionhub.hubIdentifier\n" +
                "set impactValue = (\n" +
                "\tselect peopleServed/repairEstimate\n" +
                "\tgroup by hubimpact.hubIdentifier\n" +
                ");";

        String querylimitImpact = "select hubIdentifier, impactValue from hubimpact\n" +
                "order by impactValue desc ";

        PreparedStatement state1 = null;
        try {

            //Query to find impact on respective hubs
            state1 = conn.setupConnection().prepareStatement(queryImpact);
            state1.execute();

            //sorting hubs with their impact in descending order
            PreparedStatement statement = conn.setupConnection().prepareStatement(querylimitImpact);
            ResultSet rs = statement.executeQuery(querylimitImpact);

            //Adding only 'limit' values
            while (rs.next() && hubImpact.size()<limit) {
                String hub1 = rs.getString(1);
                Float impact1 = rs.getFloat(2);
                HubImpact currentImpact = new HubImpact();
                currentImpact.setHubIdentifier(hub1);
                currentImpact.setImpactValue(impact1);
                hubImpact.add(currentImpact);
                //Checking for tie break situation
                while(hubImpact.size() == limit ){
                    rs.next();
                    if(rs.getFloat(2) == impact1){
                        String hub = rs.getString(1);
                        Float impact = rs.getFloat(2);
                        HubImpact hubImpact1 = new HubImpact();
                        currentImpact.setHubIdentifier(hub);
                        currentImpact.setImpactValue(impact);

                        //Adding value and incrementing limit
                        hubImpact.add(hubImpact1);
                        limit ++;
                    }
                }
            }
        } catch (SQLException e) {
            return hubImpact;
        }
        return hubImpact;
    }

    List<Integer> rateOfServiceRestoration(float increment) {

        List<Integer> rate = new ArrayList<>();

        int totalPopulation = serviceHelp.peopleTotal();

        int totalHours = serviceHelp.hoursTotal();

        //Time needed per person
        float timePerson = (float) totalHours / totalPopulation;

        float timePercent;

        int hoursEntry;

        increment = increment*100;

        float inc = increment;

        //since increment has been multiplied by 100
        while (increment <= 1000) {

            //Finding number of people for 'increment'%
            float peoplePercent = (increment / 100) * totalPopulation;

            //Finding time for number of people
            timePercent = peoplePercent * timePerson;

            //Rounding value because return type is hours
            hoursEntry = Math.round(timePercent);

            rate.add(hoursEntry);

            increment = increment + inc;

        }
        return rate;
    }

    List<HubImpact> repairPlan(String startHub, int maxDistance, float maxTime) {

        Double startX = null;

        Double startY = null;

        Double endX = null;

        Double endY = null;

        String endHub = null;

        int distanceCover = 0;

        Double totalImpact = 0.0;

        int totalTime = 0;

        int diagonal =0;

        boolean xInc = true;

        boolean yInc = true;

        List <HubImpact> pathFollow = new ArrayList<>();

        String queryfindHub = "select x,y,repairEstimate, impactvalue \n" +
                "from point \n" +
                "left join hubimpact on point.hubIdentifier = hubimpact.hubIdentifier \n" +
                "where point.hubIdentifier = ? ";

        String viewDistance = "create view hubdistance as\n" +
                "select point.hubIdentifier, x, y, (abs(x-?) + abs(y-?))  as distance, impactvalue, repairEstimate\n" +
                "from point\n" +
                "left join hubimpact on point.hubIdentifier = hubimpact.hubIdentifier\n" +
                "where (abs(x-?) + abs(y-?)) < ? and impactvalue is not null\n" +
                "order by impactvalue desc ;";

        try {

            //Query to find values of startHub
            PreparedStatement statement = conn.setupConnection().prepareStatement(queryfindHub);
            statement.setString(1, startHub);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                startX = rs.getDouble(1);
                startY = rs.getDouble(2);
                int currentTime = rs.getInt(3);
                Float currentImpact = rs.getFloat(4);

                //creating variable of HubImpact class and setting its values
                HubImpact h1 = new HubImpact();
                h1.setImpactValue(currentImpact);
                h1.setHubIdentifier(startHub);

                //Adding the startHub object to final list
                pathFollow.add(h1);

                //Updating values of time and impact
                totalTime = totalTime + currentTime;
                totalImpact = totalImpact + currentImpact;

            }

            //Query to find enhub
            PreparedStatement statementEnd = conn.setupConnection().prepareStatement(viewDistance);
            statementEnd.setDouble(1, startX);
            statementEnd.setDouble(2, startY);
            statementEnd.setDouble(3, startX);
            statementEnd.setDouble(4, startY);
            statementEnd.setDouble(5, maxDistance);
            statementEnd.execute();

            String queryEndhub = "select * \n" +
                    "from hubdistance\n" +
                    "limit 1;";

            PreparedStatement statementEndHub = conn.setupConnection().prepareStatement(queryEndhub);
            ResultSet rs1 = statementEndHub.executeQuery();
            while(rs1.next()){
                 endHub = rs1.getString(1);
                 endX = rs1.getDouble(2);
                 endY = rs1.getDouble(3);
                 distanceCover = rs1.getInt(4);
                 Float currentImpact = rs1.getFloat(5);
                 int currentTime = rs1.getInt(6);

                 HubImpact h1 = new HubImpact();
                 h1.setImpactValue(currentImpact);
                 h1.setHubIdentifier(endHub);

                 pathFollow.add(h1);

                 totalTime = totalTime + currentTime;
                 totalImpact = totalImpact + currentImpact;
            }

            //creating multiple paths
            List<PathImpact> paths = new ArrayList<>();

            PathImpact xPath = new PathImpact();
            xPath = serviceHelp.getXmono(totalTime,maxTime,distanceCover,maxDistance,pathFollow,totalImpact,startX,endX,startY,endY, startHub,endHub);
            paths.add(xPath);

            PathImpact yPath = new PathImpact();
            yPath = serviceHelp.getYmono(totalTime,maxTime,distanceCover,maxDistance,pathFollow,totalImpact,startX,endX,startY,endY, startHub,endHub);
            paths.add(yPath);

            PathImpact distancePath = new PathImpact();
            distancePath = serviceHelp.getDistance(totalTime,maxTime,distanceCover,maxDistance,pathFollow,totalImpact,startX,endX,startY,endY, startHub,endHub);
            paths.add(distancePath);

            PathImpact impactPath = new PathImpact();
            impactPath = serviceHelp.getImpact(totalTime,maxTime,distanceCover,maxDistance,pathFollow,totalImpact,startX,endX,startY,endY, startHub,endHub);
            paths.add(impactPath);

            //Finding optimum path
            Iterator <PathImpact> it1 = paths.listIterator();
            while(it1.hasNext()){
                PathImpact p1 = new PathImpact();
                p1 = it1.next();
                Double impact = p1.getTotalImpact();
            }

            pathFollow = impactPath.getPath();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pathFollow;
    }

    List<String> underservedPostalByPopulation(int limit) {

        List<String> underPopulation = new ArrayList<>();

        String querybyPopulation = "select postalcode.postalCode, count(hubpostal.hubIdentifier)/population as served\n" +
                " from hubimpact\n" +
                " left join hubpostal on hubimpact.hubIdentifier = hubpostal.hubIdentifier\n" +
                " left join postalcode on hubpostal.postalCode = postalcode.postalCode\n" +
                " group by hubpostal.postalCode\n" +
                " order by served asc limit " + limit;

        PreparedStatement statement = null;
        try {
            //Finding postal code least served in terms of population
            statement = conn.setupConnection().prepareStatement(querybyPopulation);
            ResultSet rs = statement.executeQuery(querybyPopulation);

            while (rs.next()) {
                String postal1 = rs.getString(1);
                underPopulation.add(postal1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return underPopulation;
    }

    List<String> underservedPostalByArea(int limit) {
        List<String> underArea = new ArrayList<>();

        String querybyArea = "select postalcode.postalCode, count(hubpostal.hubIdentifier)/area as served\n" +
                " from hubimpact\n" +
                " left join hubpostal on hubimpact.hubIdentifier = hubpostal.hubIdentifier\n" +
                " left join postalcode on hubpostal.postalCode = postalcode.postalCode\n" +
                " group by hubpostal.postalCode\n" +
                " order by served asc limit " + limit;

        PreparedStatement statement = null;
        try {
            statement = conn.setupConnection().prepareStatement(querybyArea);
            ResultSet rs = statement.executeQuery(querybyArea);

            while (rs.next()) {
                String postal1 = rs.getString(1);
                underArea.add(postal1);
            }
        } catch (SQLException e) {
           e.printStackTrace();
        }
        return underArea;
    }

}
