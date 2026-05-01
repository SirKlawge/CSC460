import java.util.Scanner;

public class Prog4 {
    private static Scanner input = new Scanner(System.in);

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
        System.out.println("\n===MAIN MENU===");
        System.out.println("1 Manage User Accounts");
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
        handleMainMenuSelection(mainMenuSelection);
    }

    private static void handleMainMenuSelection(int mainMenuSelection) {
        switch(mainMenuSelection) {
            case 1:
                printUserAccountSubMenu();
                break;
            case 2:
                pirntConvoSubMenu();
                break;
            case 3:
                printWorkspaceOrgSubMenu();
                break;
            case 4:
                printPersonaManageSubMenu();
                break;
            case 5:
                printPromtLibManageSubMenu();
                break;
            case 6:
                printSubTrackingSubMenu();
                break;
            case 7:
                printBillingOpSubMenu();
                break;
            case 8:
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

    /*
    Notes:
        -Change user Tier might be changed to "Update user" if there are more 
        ways that we can update a user
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
        System.out.println("\n===Conversations & Messages===");
        System.out.println("1. Start New Conversation");
        System.out.println("2. Continue Conversation");
        System.out.println("0. Back to Main Menu");
        return;
    }

    /*
    Edit Workspace so far just means adding a convo to an existing workspace, I think
    */
    private static void printWorkspaceOrgSubMenu() {
        System.out.println("\n===Workspace Organization===");
        System.out.println("1. Make New Workspace");
        System.out.println("2. Edit Workspace");
        System.out.println("0. Back to Main Menu");
        return;
    }

    /*
    Note: The user is gonna be creating (and maybe deleting?) Personae when they 
    create convos.  So this might have to be modified or maybe even deleted.

    A Persona belongs to a Convo, and a Convo belongs to a User.
    Therefore we should ask 
    */
    private static void printPersonaManageSubMenu() {
        System.out.println("\n===Persona Management===");
        System.out.println("1. Create Persona");
        System.out.println("2. Delete Persona");
        System.out.println("0. Back to Main Menu");
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
        System.out.println("\n===Prompt Library Management===");
        System.out.println();
    }

    private static void printSubTrackingSubMenu() {}

    private static void printBillingOpSubMenu() {}

    private static void printSupportTicketSubMenu() {}

    private static void printQueryMenu() {}

    private static void handleUserManageSelection(int menuSelection) {
        return;
    }
}