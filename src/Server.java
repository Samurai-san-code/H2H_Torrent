import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Server {

    public static String peerAppNumber;
    public BufferedReader serverBR;
    public BufferedWriter serverBW;
    public InputStreamReader serversISR;
    public OutputStreamWriter serverOSW;
    public InputStream serverIS;
    public OutputStream serverOS;
    public Socket clientServerSocket;
    public FileInputStream fileinputStream;
    public ArrayList<String> mylist;

    public Server(int serverPort) throws IOException {

        ServerSocket serverSocket = new ServerSocket(serverPort);
        clientServerSocket = serverSocket.accept();
        serverIS = clientServerSocket.getInputStream();
        serversISR = new InputStreamReader(serverIS);
        serverBR = new BufferedReader(serversISR);
        serverOS = clientServerSocket.getOutputStream();
        serverOSW = new OutputStreamWriter(serverOS);
        serverBW = new BufferedWriter(serverOSW);
        mylist = new ArrayList<>();
        peerAppNumber = serverBR.readLine().trim();

        if (peerAppNumber.equals(Integer.toString(Main.appnumber)))
            Main.appnumber++;

    }

    public void pushFile(String path) throws IOException {


        System.out.println("Attempting to send file: " + mylist.get(Integer.parseInt(path)));
        if (Files.exists(Paths.get(mylist.get(Integer.parseInt(path))))) {

            serverBW.write("sendingFile");
            serverBW.newLine();
            serverBW.flush();

            String pathTosend = mylist.get(Integer.parseInt(path));
            serverBW.write(pathTosend);
            serverBW.newLine();
            serverBW.flush();


            fileinputStream = new FileInputStream(pathTosend);//I am fetching the binary info from a file in the pc
            File fileForLength = new File(pathTosend);
            byte[] b = new byte[(int) fileForLength.length() * 2];
            fileinputStream.read(b, 0, b.length);// i am reading the info into my byte array
            serverOS.write(b, 0, b.length); //i an writing this info into the TCP socket outputstream

            System.out.println("File sent: " + pathTosend);
        } else {
            System.out.println("The file that you are trying to send does not exist.");
        }


    }

    public void listenForPullrequests() throws IOException { //this one is threadable!

        while (true) {
            String readline = serverBR.readLine().trim();
            if (readline.equals("FIN*BAM")) {//this is the client asking for the available file lists
                sendFilelist();
            } else if (readline.equals("exitexit")) {
                exitProgram(readline); //we are asking the client to exit as well
            } else {
                updateList();
                try {
                    pushFile(readline);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    System.out.println("Your peer wants you to send him this file: \"" + readline + "\", however, it doesn't exist.");

                }
            }
        }
    }

    public void exitProgram(String path) throws IOException {

        serverBW.write(path); // telling the client to exit
        serverBW.newLine();
        serverBW.flush();
        serverBR.close();
        serverBW.close();
        serversISR.close();
        serverOSW.close();
        serverIS.close();
        serverOS.close();
        clientServerSocket.close();
        System.exit(0);
    }

    private void sendFilelist() throws IOException {

        updateList();
        serverBW.write("beginOflist");
        serverBW.newLine();
        serverBW.flush();

        for (int i = 0; i < mylist.size(); i++) {
            serverBW.write(i + ": " + mylist.get(i));
            File file = new File(mylist.get(i));
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            serverBW.write("   -> MD5 checksum: " + checkMD5(fileBytes, "MD5") + "\n");

        }

        serverBW.newLine();
        serverBW.newLine();
        serverBW.flush();

    }

    public void updateList() throws IOException {
        mylist.clear();
        Stream<Path> walk = Files.walk(Paths.get("D:/TORrent_" + Main.appnumber));
        List<String> result = walk.filter(Files::isRegularFile)
                .map(x -> x.toString()).collect(Collectors.toList());
        for (int i = 0; i < result.size(); i++) {
            mylist.add(result.get(i));
        }
    }

    public void printupdatedList() throws IOException {
        mylist.clear();
        Stream<Path> walk = Files.walk(Paths.get("D:/TORrent_" + Main.appnumber));
        List<String> result = walk.filter(Files::isRegularFile)
                .map(x -> x.toString()).collect(Collectors.toList());

        for (int i = 0; i < result.size(); i++) {
            System.out.print(i + ": " + (result.get(i)));
            File file = new File(result.get(i));
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            System.out.println("   -> MD5 checksum: " + checkMD5(fileBytes, "MD5"));
            mylist.add(result.get(i));
        }
    }


    public String checkMD5(byte[] inputBytes, String algorythm) {

        String hashValue = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorythm);
            messageDigest.update(inputBytes);
            byte[] digestedBytes = messageDigest.digest();
            hashValue = String.format("%032X", new BigInteger(1, digestedBytes));
        } catch (Exception e) {
        }
        return hashValue.toLowerCase();
    }

}
