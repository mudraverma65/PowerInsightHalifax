import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PowerService {
    Service serviceHelp = new Service();
    JDBCConnection conn = new JDBCConnection();
    boolean addPostalCode(String postalCode, int population, int area) {
        PostalCode p1 = null;
        try {
            p1 = new PostalCode(postalCode, population, area);
            boolean status;
            status = p1.addPost();
            if (status == false){
                status = p1.updatePost();
            }
        } catch (SQLException e) {
           return false;
        }
        return true;
    }

    boolean addDistributionHub(String hubIdentifier, Point location, Set<String> servicedAreas) {
        DistributionHub d1 = new DistributionHub(hubIdentifier, location, servicedAreas);
        try {
            boolean status;
            status = d1.addHub();
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
            status = serviceHelp.addImpact(hubIdentifier, repairEstimate);
            if(status==false){
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
                timeNeeded = timeNeeded - repairTime;
                if (timeNeeded <= 0) {
                    inService = true;
                }
            }
            if (inService == true) {
                String query1 = "delete from hubimpact where hubIdentifier = ?";
                PreparedStatement state = conn.setupConnection().prepareStatement(query1);
                state.setString(1, hubIdentifier);
                state.execute();
            } else if (inService == false) {
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
                Integer peopleHub = rs.getInt(1);
                people = people + peopleHub;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return people;
    }

    List<DamagedPostalCodes> mostDamagedPostalCodes(int limit) {

        List<DamagedPostalCodes> m1 = new ArrayList<>();

        String queryDamage = "insert ignore into damagedpostalcodes (postalCode, repairEstimate)\n" +
                "select hubpostal.postalCode, sum(repairEstimate)\n" +
                "from hubimpact\n" +
                "left join hubpostal on hubpostal.hubIdentifier = hubimpact.hubIdentifier\n" +
                "group by postalCode;";


        String queryLDamage = "select * from damagedpostalcodes \n" +
                "order by repairEstimate desc ";

        try {
            PreparedStatement state1 = conn.setupConnection().prepareStatement(queryDamage);
            state1.execute();

            PreparedStatement statement = conn.setupConnection().prepareStatement(queryLDamage);
            ResultSet rs = statement.executeQuery();

            while (rs.next() && m1.size()<limit) {
                String postal1 = rs.getString(1);
                Float repair1 = rs.getFloat(2);
                DamagedPostalCodes d1 = new DamagedPostalCodes();
                d1.setPostalCode(postal1);
                d1.setRepairEstimate(repair1);
                m1.add(d1);
                while(m1.size() == limit){
                    rs.next();
                    if(rs.getFloat(2) == repair1){
                        String postal = rs.getString(1);
                        Float repair = rs.getFloat(2);
                        DamagedPostalCodes d2 = new DamagedPostalCodes();
                        d1.setPostalCode(postal);
                        d1.setRepairEstimate(repair);
                        m1.add(d1);
                        limit ++;
                    }
                }
            }

        } catch (SQLException e) {
            return m1;
        }
        return m1;
    }

    List<HubImpact> fixOrder(int limit) {

        List<HubImpact> h1 = new ArrayList<>();

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
            state1 = conn.setupConnection().prepareStatement(queryImpact);
            state1.execute();

            PreparedStatement statement = conn.setupConnection().prepareStatement(querylimitImpact);
            ResultSet rs = statement.executeQuery(querylimitImpact);

            while (rs.next() && h1.size()<limit) {
                String hub1 = rs.getString(1);
                Float impact1 = rs.getFloat(2);
                HubImpact hubImpact = new HubImpact();
                hubImpact.setHubIdentifier(hub1);
                hubImpact.setImpactValue(impact1);
                h1.add(hubImpact);
                while(h1.size() == limit ){
                    rs.next();
                    if(rs.getFloat(2) == impact1){
                        String hub = rs.getString(1);
                        Float impact = rs.getFloat(2);
                        HubImpact hubImpact1 = new HubImpact();
                        hubImpact.setHubIdentifier(hub);
                        hubImpact.setImpactValue(impact);
                        h1.add(hubImpact1);
                        limit ++;
                    }
                }
            }

        } catch (SQLException e) {
            return h1;
        }
        return h1;
    }

    List<Integer> rateOfServiceRestoration(float increment) {

        List<Integer> rate = new ArrayList<>();

        int totalPopulation = serviceHelp.peopleTotal();

        int totalHours = serviceHelp.hoursTotal();

        float timePerson = (float) totalHours / totalPopulation;

        float timePercent;

        int hoursEntry;

        increment = increment*100;

        float inc = increment;

        while (increment <= 1000) {

            float peoplePercent = (increment / 100) * totalPopulation;

            timePercent = peoplePercent * timePerson;

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

        String viewDistance = "alter view hubdistance as\n" +
                "select point.hubIdentifier, x, y, (abs(x-?) + abs(y-?))  as distance, impactvalue, repairEstimate\n" +
                "from point\n" +
                "left join hubimpact on point.hubIdentifier = hubimpact.hubIdentifier\n" +
                "where (abs(x-?) + abs(y-?)) < ? and impactvalue is not null\n" +
                "order by impactvalue desc ;";

        try {
            PreparedStatement statement = conn.setupConnection().prepareStatement(queryfindHub);
            statement.setString(1, startHub);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                startX = rs.getDouble(1);
                startY = rs.getDouble(2);
                int currentTime = rs.getInt(3);
                Float currentImpact = rs.getFloat(4);

                HubImpact h1 = new HubImpact();
                h1.setImpactValue(currentImpact);
                h1.setHubIdentifier(startHub);

                pathFollow.add(h1);

                totalTime = totalTime + currentTime;
                totalImpact = totalImpact + currentImpact;

            }

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

            PathImpact xPath = new PathImpact();
            xPath = serviceHelp.getXmono(totalTime,maxTime,distanceCover,maxDistance,pathFollow,totalImpact,startX,endX,startY,endY, startHub,endHub);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pathFollow;
    }

    List<String> underservedPostalByPopulation(int limit) {

        List<String> s1 = new ArrayList<>();

        String querybyPopulation = "select postalcode.postalCode, count(hubpostal.hubIdentifier)/population as served\n" +
                " from hubimpact\n" +
                " left join hubpostal on hubimpact.hubIdentifier = hubpostal.hubIdentifier\n" +
                " left join postalcode on hubpostal.postalCode = postalcode.postalCode\n" +
                " group by hubpostal.postalCode\n" +
                " order by served asc limit " + limit;

        PreparedStatement statement = null;
        try {
            statement = conn.setupConnection().prepareStatement(querybyPopulation);
            ResultSet rs = statement.executeQuery(querybyPopulation);

            while (rs.next()) {
                String postal1 = rs.getString(1);
                s1.add(postal1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return s1;
    }

    List<String> underservedPostalByArea(int limit) {
        List<String> s1 = new ArrayList<>();

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
                s1.add(postal1);
            }
        } catch (SQLException e) {
           e.printStackTrace();
        }
        return s1;
    }

}
