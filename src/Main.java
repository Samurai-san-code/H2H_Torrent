import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {

    public static int appnumber = 0;
    public static Server server;
    public static Client client;

    public static void main(String[] args) throws IOException, InterruptedException {

        int serverPort = Integer.parseInt(args[0]); //in this parameter value is 10000 or 10001

        Runnable runnableService = () -> {// the server runs on a separate thread

            try {
                server = new Server(serverPort);
                server.listenForPullrequests();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(runnableService).start();

        String peername = args[1]; // in the second parameter the value is only localhost

        int peerPort = Integer.parseInt(args[2]);//in this parameter value is 10000 or 10001

        try {
            client = new Client(peerPort, peername);
        } catch (java.net.ConnectException e) {
            System.out.println("There was no one ready");
            System.exit(0);
        }

        String string3 = null; //these null Strings are passed to mainMenu when there is no argument...
        String string4 = null;//...because mainMenu takes input from the user and it is recursive function (line 120)
        try {
            string3 = args[3];
            string4 = args[4];
        } catch (IndexOutOfBoundsException e) {
        }

        mainMenu(string3, string4);

    }

    public static void mainMenu(String string3, String string4) throws IOException, InterruptedException {


        createTorrentDirectory(); //we created the folder D:\\TORrent_x with and example file inside

        System.out.println("Welcome to the H2H TORrent. Your application number is : " + appnumber + "\n" +
                "1: Pull file\n" + "2: Check files available on the other host\n" + "3: Push file\n" + "4: Exit");

        int scInt;
        Scanner sc = null;

        if (string3 != null) //this is veryfying if we are running the program with more than 2 parameters or not


            scInt = Integer.parseInt(string3);

        else { //if there are no more than 2 parameters, then we take the user input as parameter

            sc = new Scanner(System.in);
            scInt = sc.nextInt();
            sc.nextLine();

        }

        switch (scInt) {

            case 1:

                if (string3 != null)
                    client.pullFile(string4);
                else {
                    System.out.println("Insert the number of the file you want to pull. For example: 0\n\r");
                    try {
                        String file = (sc.next());
                        client.pullFile(file);
                    } catch (InputMismatchException e) {
                        System.out.println("Insert a number");
                    }
                }
                break;

            case 2:
                client.checkFiles();
                break;
            case 3:
                System.out.println("These are your files: ");
                server.printupdatedList();
                String path;
                if (string3 != null) {
                    path = string4;

                } else {
                    System.out.println("Insert the number of the file you want to push from the list above. For example: 0 ");
                    path = (sc.next());
                    sc.nextLine();
                }

                server.pushFile(path);
                break;

            case 4:
                client.pullFile("exitexit");//my client closes all streams and sockets and informs peer server to do the same
                server.exitProgram("exitexit");//my server closes all streams and sockets and informs the peer client to do the same
                break;

        }

        string3 = null;
        string4 = null;
        TimeUnit.SECONDS.sleep(10); //This is to avoid the main menu to re-appear before the files are sent
        mainMenu(string3, string4);
    }

    public static void createTorrentDirectory() throws IOException {

        Path torrentpath = Paths.get("D:\\TORrent_" + appnumber); // we create the torrent folder in D:
        Files.createDirectories(torrentpath);
        FileWriter writeExampleFile = new FileWriter("D:/TORrent_" + Main.appnumber + "/exampleFile.txt");
        writeExampleFile.write("This in an example file");
        writeExampleFile.flush();
    }


}