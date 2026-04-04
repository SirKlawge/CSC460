/*
Author: Ventura Abram

a) easiest, just gotta get the count for the four years
*/

import java.util.Scanner;

public class Prog3 {

    static Scanner input;
    
    public static void main(String[] args) {
        //Print menu
        printMenu();
        //get query selection
        int querySelection = getSelection();
        //Handle the selection
        handleSelection(querySelection);
    }

    private static void printMenu() {
        System.out.println("You can query anything you want about this database ");
        System.out.println("so long as it's one of the four following queries (enter a, b, c, or d)");
        System.out.println("1) How many incident reports were filed in each of the four years?");
        System.out.println("2) What are the ten states with the most rail incidents in a given year?");
        System.out.println("3) Given two years, which five states had the greatest decrease in rail incidents?");
        System.out.println("4) ");
        return;
    }

    private static int getSelection() {
        input = new Scanner(System.in);
        int selection = input.nextInt();
        //Validate it
        if(selection < 1 || selection > 4) {
            System.out.println("That's not even a selection");
            System.exit(1);
        }
        return selection;
    }

    private static void handleSelection(int querySelection) {
        switch(querySelection) {
            case 1:
                System.out.println("Handling selection 1");
                break;
            case 2:
                System.out.println("Handling selection 2");
                break;
            case 3:
                System.out.println("Handling selection 3");
                break;
            case 4:
                System.out.println("Handling selection 4");
                break;
            default:
                break;
        }
        return;
    }
}