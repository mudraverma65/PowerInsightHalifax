import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Service {

    JDBCConnection conn = new JDBCConnection();

    void setServed() {
        String hubsPostal = //"alter table postalcode\n" +
                //"add hubs int;\n" +
                //"\n" +
                "alter view hubpeoplepostal as \n" +
                        "select count(hubIdentifier) as hubs, postalCode\n" +
                        "from hubpostal\n" +
                        "group by postalCode;";

        String postalUpdate = "update postalcode\n" +
                "inner join hubpeoplepostal on postalcode.postalCode = hubpeoplepostal.postalCode\n" +
                "set postalcode.hubs = hubpeoplepostal.hubs ;";

        String query_view = "alter view peopleServed as\n" +
                "select sum(population/hubs) as peopleServed, hubpostal.postalCode, hubpostal.hubIdentifier\n" +
                "from hubpostal\n" +
                "left join postalcode on hubpostal.postalCode  = postalcode.postalCode\n" +
                "left join distributionhub on hubpostal.hubIdentifier = distributionhub.hubIdentifier\n" +
                "group by hubpostal.hubIdentifier;";

        String update_table = "update distributionhub\n" +
                "inner join peopleServed on distributionhub.hubIdentifier = peopleServed.hubIdentifier\n" +
                "set distributionhub.peopleServed = peopleserved.peopleServed;\n";

        try {
            PreparedStatement state1 = conn.setupConnection().prepareStatement(hubsPostal);
            state1.execute();

            PreparedStatement state2 = conn.setupConnection().prepareStatement(postalUpdate);
            state2.execute();

            PreparedStatement state3 = conn.setupConnection().prepareStatement(query_view);
            state3.execute();

            PreparedStatement state4 = conn.setupConnection().prepareStatement(update_table);
            state4.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    int peopletotal() {

        Integer totalPeople = null;
        String queryTotal = "select sum(peopleServed)\n" +
                "from distributionhub\n" +
                "inner join hubimpact on distributionhub.hubIdentifier = hubimpact.hubIdentifier;";

        try {
            PreparedStatement state1 = conn.setupConnection().prepareStatement(queryTotal);

            ResultSet rs = state1.executeQuery(queryTotal);
            while (rs.next()) {
                totalPeople = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return totalPeople;
    }

    int hourstotal() {
        Integer totalHours = null;
        String queryTotal = "select sum(repairEstimate)\n" +
                "from hubimpact;";

        try {
            PreparedStatement state1 = conn.setupConnection().prepareStatement(queryTotal);

            ResultSet rs = state1.executeQuery(queryTotal);
            while (rs.next()) {
                totalHours = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return totalHours;
    }

    void setTables() {

        String queryPostalCode = "create table PostalCode (\n" +
                "\tpostalCode varchar(250) NOT NULL,\n" +
                "    population int NOT NULL,\n" +
                "    area int NOT NULL,\n" +
                "    hubs int,\n" +
                "    primary key(postalCode)\n" +
                ");";

        String queryDistributionHub = "create table DistributionHub (\n" +
                "\thubIdentifier varchar(250) NOT NULL,\n" +
                "    location Point,\n" +
                "    peopleServed int,\n" +
                "    primary key(hubIdentifier)\n" +
                ");";

        String queryPoint = "create table Point(\n" +
                "\thubIdentifier varchar(250) NOT NULL,\n" +
                "    x int NOT NULL,\n" +
                "    y int NOT NULL,\n" +
                "    foreign key(hubIdentifier) references DistributionHub(hubIdentifier)\n" +
                ");";

        String queryHubPostal = "create table hubPostal (\n" +
                "\thubIdentifier varchar(250) NOT NULL,\n" +
                "    postalCode varchar(250) NOT NULL,\n" +
                "    foreign key(hubIdentifier) references DistributionHub(hubIdentifier),\n" +
                "    foreign key(postalCode) references PostalCode(postalCode)\n" +
                ");";

        String queryHubImpact = "create table HubImpact (\n" +
                "\thubIdentifier varchar(250) NOT NULL,\n" +
                "    impactValue float NOT NULL,\n" +
                "    repairEstimate float NOT NULL,\n" +
                "    foreign key(hubIdentifier) references DistributionHub(hubIdentifier)\n" +
                ");";

        String queryDamagedPostalCodes = "create table DamagedPostalCodes (\n" +
                "\tpostalCode varchar (250) not null,\n" +
                "    repairEstimate float NOT NULL,\n" +
                "    foreign key(postalCode) references PostalCode(postalCode)\n" +
                ");";

        PreparedStatement state1 = null;
        try {
            state1 = conn.setupConnection().prepareStatement(queryPostalCode);
            state1.execute();

            PreparedStatement state2 = conn.setupConnection().prepareStatement(queryDistributionHub);
            state2.execute();

            PreparedStatement state3 = conn.setupConnection().prepareStatement(queryPoint);
            state3.execute();

            PreparedStatement state4 = conn.setupConnection().prepareStatement(queryHubPostal);
            state4.execute();

            PreparedStatement state5 = conn.setupConnection().prepareStatement(queryHubImpact);
            state5.execute();

            PreparedStatement state6 = conn.setupConnection().prepareStatement(queryDamagedPostalCodes);
            state6.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    List<HubImpact> getXmono(float totalTime, float maxTime, int totalDistance, float maxDistance, List<HubImpact> XMono, Double startX, Double endX, Double startY, Double endY){

        String listHubs = "select * \n" +
                "from hubdistance \n" +
                "where x between ? and ? and y between ? and ?;";

        List<HubImpact> xMono = XMono;

        int diagonal =0;

        boolean xInc = true;

        boolean yInc = true;

        PreparedStatement statementList = null;
        try {
            statementList = conn.setupConnection().prepareStatement(listHubs);

            statementList.setDouble(1, startX);
            statementList.setDouble(2, endX);
            statementList.setDouble(3, startY);
            statementList.setDouble(4, endY);
            ResultSet resultList = statementList.executeQuery();

            while(resultList.next()) {
                float currentTime = totalTime + resultList.getFloat(6);
                int currentDistance = totalDistance + resultList.getInt(4);
                Double currentX = resultList.getDouble(2);
                Double currentY = resultList.getDouble(3);

                if(currentX > startX){
                    xInc = true;
                    startX = currentX;
                }
                else{
                    xInc = false;
                }

                if(currentY>startY){
                    yInc = true;
                    startY = currentY;
                }
                else{
                    yInc = false;
                }

                if((currentX - startX) > (endX - startX)/2 && (xInc == true || yInc == true)){
                    diagonal = diagonal + 1;
                }

                if((currentY - startY) > (endY - startY)/2 && (xInc == true || yInc == true)){
                    diagonal = diagonal + 1;
                }

                if (totalTime <= maxTime && currentDistance <= maxDistance && diagonal < 2 && ((xInc == true && yInc == false) || (xInc == true && yInc == true))) {
                    HubImpact h1 = new HubImpact();
                    h1.setHubIdentifier(resultList.getString(1));
                    h1.setImpactValue(resultList.getFloat(5));

                    xMono.add(h1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return xMono;

    }

}
