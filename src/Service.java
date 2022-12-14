import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

}
