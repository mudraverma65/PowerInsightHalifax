import java.sql.PreparedStatement;
import java.sql.SQLException;

public class peopleServed {

    JDBCConnection conn = new JDBCConnection();

    void setpeopleServed(){
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

}
