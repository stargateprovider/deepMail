import Commands.PrintEcho;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        //System.out.println("Hello World!");

        String[] test1 = {"printecho", "Hello", "World"};
        String[] test2 = {"printecho"};

        PrintEcho printEcho;
        int result;

        System.out.println("Test 1");

        printEcho = new PrintEcho(Arrays.copyOfRange(test1, 1, test1.length));
        result = printEcho.task();

        if(result == -1){
            String msg = printEcho.wrongInputErrorMsg();
            System.out.println(msg);
        }

        System.out.println("\n-------------------------------\n");

        System.out.println("Test 2");


        printEcho = new PrintEcho(Arrays.copyOfRange(test2, 1, test2.length));
        result = printEcho.task();

        if(result == -1){
            String msg = printEcho.wrongInputErrorMsg();
            System.out.println(msg);
        }

    }
}
