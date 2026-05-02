import java.util.Scanner;

public class Prog4 {
    private static Scanner input = new Scanner(System.in);
    private static String user;

    public static void main(String[] args) {
        printWelcomeMessage();
        //Present the user with the menu
        printMainMenu();
    }

    private static void printWelcomeMessage() {
        System.out.println("WELCOME, SYSTEM ADMIN, TO CHAT HUB of AMERICA TECHNOLOGIES (CHAT)");
        System.out.println("[name is WIP]");
        return;
    }

    private static void printMainMenu() {
        //Reset user
        user = "";
        System.out.println("\n===MAIN MENU===");
        System.out.println("1. Manage User Accounts");
        System.out.println("2. Handle Conversations & Messages");
        System.out.println("3. Workspace Organization");
        System.out.println("4. Persona Management");
        System.out.println("5. Prompt Library Management");
        System.out.println("6. Subscription Tracking");
        System.out.println("7. Billing Operations");
        System.out.println("8. Support Ticket Management");
        System.out.println("9. Query Menu");
        System.out.println("0. Exit");
        int mainMenuSelection = input.nextInt();
        input.nextLine();
        handleMainMenuSelection(mainMenuSelection);
    }

    /*Most of these willl require that a user be selected*/
    private static void handleMainMenuSelection(int mainMenuSelection) {
        switch(mainMenuSelection) {
            case 1:
                printUserAccountSubMenu();
                break;
            case 2:
                setUser();
                pirntConvoSubMenu();
                break;
            case 3:
                setUser();
                printWorkspaceOrgSubMenu();
                break;
            case 4:
                setUser();
                printPersonaManageSubMenu();
                break;
            case 5:
                setUser();
                printPromtLibManageSubMenu();
                break;
            case 6:
                setUser();
                printSubTrackingSubMenu();
                break;
            case 7:
                setUser();
                printBillingOpSubMenu();
                break;
            case 8:
                setUser();
                printSupportTicketSubMenu();
                break;
            case 9:
                printQueryMenu();
                break;
            case 0:
                System.exit(0);
                break;
            default:
                break;
        }
        return;
    }

    private static void setUser() {
        System.out.print("\nEnter username: ");
        user = input.nextLine();
        return;
    }

    /*
    Notes:
        -Change user Tier might be changed to "Update user" if there are more 
        ways that we can update a user

        Change user and delete user are one-off operations on a specific user.
        Therefore, we can propmt the System Admin for the specific User after 
        those selections have been made.
    */
    private static void printUserAccountSubMenu() {
        System.out.println("\n===User Account Management===");
        System.out.println("1. Add a User");
        System.out.println("2. Change User Tier");
        System.out.println("3. Delete User");
        System.out.println("0. Back to Main Menu");
        int menuSelection = input.nextInt();
        handleUserManageSelection(menuSelection);
        return;
    }

    /*
    Note: From "Continue Conversation", I guess we can then prompt the user to 
        1) search for User (to get their list of conversations), then
        2) present the user with a list of active convos for the User (requires a query)
    From there we can do things like add messages and update message feedback
    */
    private static void pirntConvoSubMenu() {
        System.out.println("\n===Conversations & Messages=== User: " + user);
        System.out.println("1. Start New Conversation");
        System.out.println("2. Continue Conversation");
        System.out.println("0. Back to Main Menu");
        int menuSelection = input.nextInt();
        input.nextLine();
        handleConvoMenuSelection(menuSelection);
        return;
    }

    /*
    Edit Workspace so far just means adding a convo to an existing workspace, I think
    */
    private static void printWorkspaceOrgSubMenu() {
        System.out.println("\n===Workspace Organization=== User: " + user);
        System.out.println("1. Make New Workspace");
        System.out.println("2. Edit Workspace");
        System.out.println("0. Back to Main Menu");
        int menuSelection = input.nextInt();
        input.nextLine();
        handleWorkspaceOrgSelection(menuSelection);
        return;
    }

    /*
    Note: The user is gonna be creating (and maybe deleting?) Personae when they 
    create convos.  So this might have to be modified or maybe even deleted.

    A Persona belongs to a Convo, and a Convo belongs to a User.
    Therefore we should ask 
    */
    private static void printPersonaManageSubMenu() {
        System.out.println("\n===Persona Management=== User: " + user);
        System.out.println("1. Create Persona");
        System.out.println("2. Delete Persona");
        System.out.println("0. Back to Main Menu");
        int menuSelection = input.nextInt();
        input.nextLine();
        handlePersonaManageSelection(menuSelection);
        return;
    }

    /*
    Note: Prompt Libraries belong to Workspaces, and Workspaces belong to Users.
    This will therefore need to ask for a User and for a Workspace

    Really, we should prompt the System Admin using this program for a User and 
    Workspace first before we present them with this menu so they dont have to re-enter
    that data every time they want to make a change to the same User's Prompt Library
    */
    private static void printPromtLibManageSubMenu() {
        System.out.println("\n===Prompt Library Management=== User: " + user);
        System.out.println("1. Add New Prompt");
        System.out.println("2. Edit Propmt");
        System.out.println("0. Back to Main Menu");
        int menuSelection = input.nextInt();
        input.nextLine();
        handlePromptLibSelection(menuSelection);
        return;
    }

    /*Maybe if we're gonna update the sub level, we display the user's current sub level.
    That'll involve a query*/
    private static void printSubTrackingSubMenu() {
        System.out.println("===Subscription Tracking=== User: " + user);
        System.out.println("1. Update Subscription Level");
        System.out.println("2. Check Message Limit");
        System.out.println("0. Back to Main Menu");
        int menuSelection = input.nextInt();
        input.nextLine();
        handleSubTrackingSelection(menuSelection);
        return;
    }

    private static void printBillingOpSubMenu() {
        System.out.println("===Billing Operations=== User: " + user);
        System.out.println("1. Generate Invoice");
        System.out.println("2. Mark Invoice as Paid");
        System.out.println("0. Back to Main Menu");
        int menuSelection = input.nextInt();
        input.nextLine();
        handleBillingOpSelection(menuSelection);
        return;
    }

    private static void printSupportTicketSubMenu() {
        System.out.println("===Support Ticket Management=== User: " + user);
        System.out.println("1. Create Support Ticket");
        System.out.println("2. Assign Ticket to Agent");
        System.out.println("3. Update Ticket Status");
        System.out.println("0. Back to Main Menu");
        int menuSelection = input.nextInt();
        input.nextLine();
        handleSupportTicketSelection(menuSelection);
    }

    private static void printQueryMenu() {
        System.out.println("\n===Query Menu===");
        System.out.println("1. Get All Bookmarked Messsages");
        System.out.println("2. List of Users Who Haven't Paid Up");
        System.out.println("3. Get Highest-Rated Persona");
        System.out.println("4. Custom Query");
        System.out.println("0 Back to Main Menu");
        int menuSelection = input.nextInt();
        input.nextLine();
        handleQuerySelection(menuSelection);
        return;
    }

    private static void handleUserManageSelection(int menuSelection) {
        switch(menuSelection) {
            case 1:
                //TODO: Delete print statement and implement later
                System.out.println("Adding a user");
                break;
            case 2:
                System.out.println("Chaning user tier");
                break;
            case 3:
                System.out.println("Deleting user");
                break;
            case 0:
                printMainMenu();
                break;
            default:
                //Handle invalid inputs
                break;
        }
        return;
    }

    private static void handleConvoMenuSelection(int menuSelection) {
        switch(menuSelection) {
            case 1:
                System.out.println("Starting new convo");
                break;
            case 2:
                System.out.println("Continuing existing convo");
                break;
            case 0:
                printMainMenu();
            default:
                //handle invalid inputs
                break;
        }
        return;
    }

    private static void handleWorkspaceOrgSelection(int menuSelection) {
        switch(menuSelection) {
            case 1:
                System.out.println("Making new workspace");
                break;
            case 2: 
                System.out.println("Editing workspace");
                break;
            case 0:
                printMainMenu();
                break;
            default:
                //Handle invalid inputs
                break;
        }
        return;
    }

    private static void handlePersonaManageSelection(int menuSelection) {
        switch(menuSelection) {
            case 1:
                System.out.println("Making persona");
                break;
            case 2:
                System.out.println("Deleting persona");
                break;
            case 0:
                printMainMenu();
                break;
            default:
                //handle invalid inputs
                break;
        }
       return; 
    }

    private static void handlePromptLibSelection(int menuSelection) {
        switch(menuSelection) {
            case 1:
                System.out.println("Adding New Prompt");
                break;
            case 2:
                System.out.println("Editing Prompt");
                break;
            case 0:
                printMainMenu();
                break;
            default:
                //handle invalid inputs
                break;
        }
        return;
    }

    private static void handleSubTrackingSelection(int menuSelection) {
        switch(menuSelection) {
            case 1:
                System.out.println("Updating Sub level");
                break;
            case 2:
                System.out.println("Checking message limit");
                break;
            case 0:
                printMainMenu();
            default:
                //handle invalid inputs
                break;
        }
        return;
    }

    private static void handleBillingOpSelection(int menuSelection) {
        switch(menuSelection) {
            case 1:
                System.out.println("Generating invoice");
                break;
            case 2:
                System.out.println("Marking invoice as paid");
                break;
            case 0:
                printMainMenu();
                break;
            default:
                //handle invalid inputs
                break;
        }
        return;
    }

    private static void handleSupportTicketSelection(int menuSelection) {
        switch(menuSelection) {
            case 1:
                System.out.println("Creating Support Ticket");
                break;
            case 2:
                System.out.println("Assigning Ticket to Agent");
                break;
            case 3:
                System.out.println("Updating Ticket Status");
                break;
            case 0:
                printMainMenu();
                break;
            default:
                //handle invalid inputs
                break;
        }
        return;
    }

    private static void handleQuerySelection(int menuSelection) {
        switch(menuSelection) {
            case 1:
                System.out.println("Gettin' all bookmarks");
                break;
            case 2:
                System.out.println("Gettin' list of non=payers");
                break;
            case 3:
                System.out.println("Getting highest=rated persona");
                break;
            case 4:
                System.out.println("Running custom query");
                break;
            case 0:
                printMainMenu();
                break;
            default:
                //handle invalid inputs
                break;
        }
        return;
    }
}