import java.util.Scanner;

public class Parser {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("Enter the command number:");

            if (sc.hasNextInt()) {
                int number = sc.nextInt();
                if (number == 1) {

                    break;
                } else if (number == 2) {

                    break;
                } else {
                    System.out.println("Invalid command number");
                }
            } else {
                System.out.println("You did not enter a number");
                sc.nextLine();
            }
        }
    }
}
