import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        PowerService p1 = new PowerService();
        p1.addPostalCode("B3J2M9", 1223, 1200);
        p1.addPostalCode("B3J2K9", 1223, 1200);

        p1.addPostalCode("B3J2M9", 1220, 1201);
        p1.addPostalCode("B2J2K9", 1103, 1200);



        Point p2 = new Point(10,20);
        Set<String> setAreas = new HashSet<String>();

        setAreas.add("B3J2K9");
        setAreas.add("B2J2K9");
        setAreas.add("B3J2M9");

        p1.addDistributionHub("Hub4",p2,setAreas);

        p1.addDistributionHub("Hub3",p2,setAreas);

        Set<String> set1 = new HashSet<String>();

        set1.add("B3J2K9");
        set1.add("B2J2K9");

        p1.addDistributionHub("Hub5",p2,set1);

        p1.hubDamage("Hub4", 12);
        p1.hubDamage("Hub3", 20);

        p1.hubDamage("Hub5", 15);



        p1.hubRepair("Hub3", "mudra", 10, false);

        Integer p = p1.peopleOutOfService();

        List<DamagedPostalCodes> l1 = new ArrayList<>();
        l1 = p1.mostDamagedPostalCodes(2);

        List<HubImpact> h1 = new ArrayList<>();
        h1 = p1.fixOrder(2);

        List<String> u1 = new ArrayList<>();
        u1 = p1.underservedPostalByPopulation(2);

        List<String> u2 = new ArrayList<>();
        u2 = p1.underservedPostalByArea(2);
       // p1.addDistributionHub("mudra1", p2, 20);
    }
}