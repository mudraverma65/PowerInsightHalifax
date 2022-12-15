import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        PowerService p1 = new PowerService();
        p1.addPostalCode("B3J2M9", 1223, 1200);
        p1.addPostalCode("B3J2K9", 1223, 1200);
        p1.addPostalCode("B3J2P9", 1220, 1201);
        p1.addPostalCode("B2J2K9", 1103, 1200);
        p1.addPostalCode("MHJ2M9", 12900, 101);
        p1.addPostalCode("P9J2K9", 11103, 22200);
        p1.addPostalCode("FHJ2I9", 190, 11);
        p1.addPostalCode("O6J2K9", 703, 102);
        p1.addPostalCode("FHD2I9", 2003, 1500);
        p1.addPostalCode("A6P2K9", 79000, 11022);


        Point p2 = new Point(5,14);
        Point p3 = new Point (40,12);
        Point p4 = new Point (8,29);
        Point p5 = new Point (35,47);
        Point p6 = new Point (23,8);
        Point p7 = new Point (9,13);
        Point p8 = new Point (30,20);
        Point p9 = new Point (18,30);
        Point p10 = new Point (14,45);

        Set<String> setAreas = new HashSet<String>();
        setAreas.add("B3J2K9");
        setAreas.add("MHJ2M9");
        setAreas.add("O6J2K9");
        setAreas.add("B3J2M9");
        setAreas.add("FHJ2I9");

        p1.addDistributionHub("Hub1",p2,setAreas);

        Set<String> set2 = new HashSet<String>();
        set2.add("B3J2K9");
        set2.add("MHJ2M9");
        set2.add("FHJ2I9");

        p1.addDistributionHub("Hub2",p3,set2);

        Set<String> set1 = new HashSet<String>();
        set1.add("P9J2K9");
        set1.add("O6J2K9");

        p1.addDistributionHub("Hub3",p4,set1);

        Set<String> set8 = new HashSet<String>();
        set8.add("B3J2K9");
        set8.add("MHJ2M9");
        set8.add("B3J2P9");
        set8.add("FHD2I9");
        set8.add("A6P2K9");
        set8.add("P9J2K9");
        set8.add("B2J2K9");

        p1.addDistributionHub("Hub4",p5,set8);

        Set<String> set3 = new HashSet<String>();
        set3.add("B3J2M9");
        set3.add("MHJ2M9");
        set3.add("B2J2K9");

        p1.addDistributionHub("Hub5",p6,set3);

        Set<String> set4 = new HashSet<String>();
        set4.add("B3J2K9");
        set4.add("MHJ2M9");
        set4.add("A6P2K9");
        set4.add("B3J2P9");
        set4.add("FHD2I9");

        p1.addDistributionHub("Hub6",p7,set4);

        Set<String> set5 = new HashSet<String>();
        set5.add("FHJ2I9");

        p1.addDistributionHub("Hub7",p8,set5);

        Set<String> set6 = new HashSet<String>();
        set6.add("B3J2K9");
        set6.add("FHJ2I9");

        p1.addDistributionHub("Hub8",p9,set6);

        Set<String> set7 = new HashSet<String>();
        set7.add("B3J2K9");
        set7.add("B3J2P9");
        set7.add("O6J2K9");
        set7.add("P9J2K9");
        set7.add("FHJ2I9");
        set7.add("B3J2M9");
        p1.addDistributionHub("Hub9",p10,set7);

        p1.hubDamage("Hub1", 19);
        p1.hubDamage("Hub2", 20);
        p1.hubDamage("Hub3", 15);
        p1.hubDamage("Hub4", 12);
        p1.hubDamage("Hub5", 9);
        p1.hubDamage("Hub6", 11);
        p1.hubDamage("Hub7", 26);
        p1.hubDamage("Hub8", 7);
        p1.hubDamage("Hub9", 13);


        p1.hubRepair("Hub1", "mudra", 14, false);
        p1.hubRepair("Hub2", "mudra", 18, false);
        p1.hubRepair("Hub3", "mudra", 9, true);


        int p = p1.peopleOutOfService();

        List<DamagedPostalCodes> l1 = new ArrayList<>();
        l1 = p1.mostDamagedPostalCodes(2);

        List<HubImpact> h1 = new ArrayList<>();
        h1 = p1.fixOrder(2);

        List<String> u1 = new ArrayList<>();
        u1 = p1.underservedPostalByPopulation(2);

        List<String> u2 = new ArrayList<>();
        u2 = p1.underservedPostalByArea(2);
       // p1.addDistributionHub("mudra1", p2, 20);

        List<Integer> rate = new ArrayList<>();
        rate = p1.rateOfServiceRestoration(0.5f);

        p1.repairPlan("Hub1",50, 50f);

    }
}