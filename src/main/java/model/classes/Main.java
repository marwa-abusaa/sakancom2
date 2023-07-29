package model.classes;
import code.classes.AddAdvertisement;
import code.classes.AdminPage;
import code.classes.Login;
import code.classes.OwnerControlPanel;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getName());
    private static final String IN_VALID_INPUT = "Please enter valid input";
    static String url = "jdbc:mysql://localhost:3306/sakancom";
    static String user = "root";
    static String p = "memesa32002@";
    private static String price = "price";
    private static String services = "services";
    private static String username;
    public static void displayAllHouses() throws SQLException {
        Connection con = DriverManager.getConnection(url,user,p);
        Statement stmt = con.createStatement();
        ResultSet result = stmt.executeQuery("select * from house");
        logger.info("All houses in the system: ");
        logger.info("idHouse\t location\t\t services\t\t price\t\t idOwner");
        while (result.next()) {
            String output=result.getInt("idhouse")+"\t"+result.getString("location")+"\t"+result.getString(services)+"\t"+ result.getDouble(price)+" JD\t"+ result.getInt("id_owner");
            logger.info(output);
        }

    }

    public static void displayHouses(int ownerID) throws SQLException {

        Connection con = DriverManager.getConnection(url,user,p);
        Statement stmt = con.createStatement();
        ResultSet result = stmt.executeQuery("select * from house where id_owner='" + ownerID + "'");
        logger.info("idHouse\t location\t\t services\t\t price");
        while (result.next()) {
            String output=result.getInt("idhouse") + "\t" + result.getString("location") + "\t\t" + result.getString(services) + "\t\t" + result.getDouble(price) + " JD";
            logger.info(output);
        }

    }
    public static void main(String[] args) throws URISyntaxException, IOException, SQLException {
        logger.info("------* Welcome to SAKANCOM system *------");

        Scanner scan = new Scanner(System.in);

        while (true) {
            logger.info("-> Please, choose how you want to login to the system:");
            logger.info("1- Admin");
            logger.info("2- Tenant");
            logger.info("3- Owner");
            String choice = scan.nextLine();

            if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
                logger.info("The choice is not valid");
                continue;
            }

            Login log = performLogin(choice);
            if (log != null && log.isLogIn()) {
                logger.info("logged in successfully");

                if (choice.equals("1")) {
                    handleAdminTasks(log, scan);
                } else if (choice.equals("3")) {
                    handleOwnerTasks(log, scan);
                } else if (choice.equals("2")) {
                    handleTenantTasks();
                }
            }
        }
    }

    private static Login performLogin(String choice) throws SQLException {
        Scanner scan = new Scanner(System.in);
        Login log = new Login();

        while (true) {
            logger.info("Enter username: ");
             username = scan.nextLine();
            logger.info("Enter password: ");
            String password = scan.nextLine();
            log.logInCheck(username, password, choice);

            if (log.isLogIn()) {
                return log;
            } else {
                log.reasonFalseLogin();
            }
        }
    }

    private static void handleAdminTasks(Login log, Scanner scan) throws URISyntaxException, IOException, SQLException {
        String hello = "Hello " + username;
        logger.info(hello);
        AdminPage adminPage;
        House updateHouse = new House();

        while (true) {
            displayAdminOptions();
            String adminChoice = scan.nextLine();

            switch (adminChoice) {
                case "1":
                    handleAdvertisementRequests(scan);
                    break;
                case "2":
                    displayReservations();
                    break;
                case "3":
                    updateHouseInformation(scan, updateHouse);
                    break;
                case "4":
                    log.logout();
                    return;
                default:
                    logger.warning(IN_VALID_INPUT);
                    break;
            }
        }
    }

    private static void displayAdminOptions() {
        logger.info("1- See the requests of advertisement to accept or reject it");
        logger.info("2- See the reservations of houses");
        logger.info("3- Update house information");
        logger.info("4- Log out");
    }

    private static void handleAdvertisementRequests(Scanner scan) throws URISyntaxException, IOException, SQLException {
        /*open web page*/
        Desktop d = Desktop.getDesktop();
        String uri = "http://localhost/sakancom/table.php";
        d.browse(new URI(uri));

        while (true) {
            logger.info("1- Enter house id you want to accept its advertisement");
            logger.info("2- <-Back");
            String advChoice = scan.nextLine();
            if (advChoice.equals("1")) {
                logger.info("house id: ");
                String houseID = scan.nextLine();

                logger.info("acceptance (yes/no): ");
                String acceptance = scan.nextLine();
                AdminPage adminPage = new AdminPage(Integer.parseInt(houseID), acceptance);
                AdminPage.acceptReject(adminPage);
                logger.info("DONE");
            } else if (advChoice.equals("2")) {
                return;
            }
        }
    }

    private static void displayReservations() throws SQLException {
        List<Tenant> tenantList = AdminPage.seeReservations();
        AdminPage.displayReservations(tenantList);
    }

    private static void updateHouseInformation(Scanner scan, House updateHouse) throws SQLException {
        displayAllHouses();
        logger.info("Enter the ID of the house for updating: ");
        String idhouse = scan.nextLine();
        if (!House.findHouseId(Integer.parseInt(idhouse))) {
            updateHouse.unupdatedMsg();
            return;
        }

        while (true) {
            displayUpdateOptions();
            String updateOption = scan.nextLine();

            if (updateOption.equals("1")) {
                logger.info("The new services of the house you want to update: ");
                String services = scan.nextLine();
                updateHouse.updateInfo(Main.services, services, Integer.parseInt(idhouse));
                updateHouse.updateMsg();
            } else if (updateOption.equals("2")) {
                logger.info("The new price of the house you want to update: ");
                String price = scan.nextLine();
                updateHouse.updateInfo(Main.price, Double.parseDouble(price), Integer.parseInt(idhouse));
                updateHouse.updateMsg();
            } else if (updateOption.equals("3")) {
                logger.info("The new ownerId of the house you want to update: ");
                String ownerid = scan.nextLine();
                updateHouse.updateInfo("ownerId", Integer.parseInt(ownerid), Integer.parseInt(idhouse));
                updateHouse.updateMsg();
            } else if (updateOption.equals("4")) {
                return;
            } else {
                logger.warning(IN_VALID_INPUT);
            }
        }
    }

    private static void displayUpdateOptions() {
        logger.info("Choose what do you want to update: ");
        logger.info("1- Change Services");
        logger.info("2- Change Price");
        logger.info("3- Change OwnerID");
        logger.info("4- Back");
    }


    private static void handleOwnerTasks(Login log, Scanner scan) throws SQLException {
        String hello = "Hello " + log.getOwnerName();
        logger.info(hello);
        AddAdvertisement advertisement;
        while (true) {
            logger.info("1- Add advertisement for an existing house");
            logger.info("2- See all housing with all details");
            logger.info("3- Add a new house");
            logger.info("4- Log out");
            String ownerChoice = scan.nextLine();

            if (ownerChoice.equals("1")) {
                displayHouses(log.getOwnerID());
                logger.info("Enter house id: ");
                String houseID=scan.nextLine();
                logger.info("Add photos: ");
                String photo=scan.nextLine();
                logger.info("Enter your name: ");
                String name=scan.nextLine();
                logger.info("Enter your contact info.: ");
                String contact=scan.nextLine();
                logger.info("Enter location: ");
                String location=scan.nextLine();
                logger.info("Enter services: ");
                String services=scan.nextLine();
                logger.info("Enter monthly rent: ");
                String rent=scan.nextLine();
                logger.info("Enter rent notes about inclusive of electricity and water or not: ");
                String rentNote=scan.nextLine();
                logger.info("Enter price: ");
                String price=scan.nextLine();
                advertisement=new AddAdvertisement(Integer.parseInt(houseID),photo,name,contact,location,services,Double.parseDouble(rent));
                advertisement.setRentNote(rentNote);
                advertisement.setPrice(Double.parseDouble(price));
                AddAdvertisement.addAdv(advertisement);
                if(!AddAdvertisement.isValidHouse()){
                    if(AddAdvertisement.getIsDuplicateHouse()){
                        advertisement.displayReasonSameHouse();
                    }
                    else advertisement.displayReasonHouseNotExist();
                }
                else logger.info("The advertisement is added, but waiting Administrator to accept it ");

            } else if (ownerChoice.equals("2")) {
                List<House> houseList=new ArrayList<>();
                List<Integer> apart=new ArrayList<>();
                List<HouseFloor> apartInfoList = new ArrayList<>();
                logger.info("Your Housing:");
                displayHouses(log.getOwnerID());
                logger.info("Enter house id to see number of tenant and floor of this house: ");
                String houseId=scan.nextLine();
                houseList=OwnerControlPanel.findHouse(Integer.parseInt(houseId));
                OwnerControlPanel.displayNOTenantAndFloors(houseList);
                ////////////////
                logger.info("Enter floor number you want to see its apartments: ");
                String floorId=scan.nextLine();
                apart=OwnerControlPanel.findFloor(Integer.parseInt(floorId));
                OwnerControlPanel.displayAparts(apart);
                ///////////////
                logger.info("Enter apart number that you want to see info. about it: ");
                String apartId=scan.nextLine();
                apartInfoList=OwnerControlPanel.findApart(Integer.parseInt(apartId));
                OwnerControlPanel.displayApartInformation(apartInfoList);

            } else if (ownerChoice.equals("3")) {
                // Perform actions for owner choice 3
            } else if (ownerChoice.equals("4")) {
                log.logout();
                break;
            } else {
                logger.warning(IN_VALID_INPUT);
            }
        }
    }

    private static void handleTenantTasks() {
        logger.info("Tenant functionality is not implemented yet.");
    }
}
