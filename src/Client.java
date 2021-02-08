import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.nio.file.Paths;

public class Client {

    public Socket clientSocket;
    public OutputStream clientOS;
    public InputStream clientIS;
    public OutputStreamWriter clientOSW;
    public InputStreamReader clientISR;
    public BufferedReader clientBR;
    public BufferedWriter clientBW;
    public Inet4Address peerAddress;
    public FileOutputStream fileoutputSream;


    Client(int portNumber, String peerName) throws IOException {

        peerAddress = (Inet4Address) Inet4Address.getByName(peerName);
        clientSocket = new Socket(peerAddress, portNumber);
        System.out.println("Connection established succesfully with : " + peerAddress + ":" + portNumber);
        clientOS = clientSocket.getOutputStream();
        clientOSW = new OutputStreamWriter(clientOS);
        clientBW = new BufferedWriter(clientOSW);
        clientIS = clientSocket.getInputStream();
        clientISR = new InputStreamReader(clientIS);
        clientBR = new BufferedReader(clientISR);
        clientBW.write(Integer.toString(Main.appnumber));
        clientBW.newLine();
        clientBW.flush();

        Runnable runnableservice = () -> { //We will be constantly reading from the inputstream on a separate thread

            try {
                receiveFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        };
        new Thread(runnableservice).start();
    }

    public void pullFile(String file) throws IOException {

        this.clientBW.write(file);
        this.clientBW.newLine();
        if (!file.equals("exitexit")) // just for front-end purposes
            System.out.println("Requesting file " + file);

        this.clientBW.flush();
    }

    public void checkFiles() throws IOException {

        String bambam = "FIN*BAM";//im informing the server that i want to have the list of files
        clientBW.write(bambam);
        clientBW.newLine();
        clientBW.flush();
    }


    public void receiveFile() throws IOException {

        while (true) {

            String incomingText = clientBR.readLine();


            if (incomingText.equals("exitexit")) {
                exitProgram();
            } else if (incomingText.equals("sendingFile")) { //creates a copie of the file if this is the case

                String incomingFilepathText = clientBR.readLine().trim();
                System.out.println("Incoming file : " + clientSocket.getLocalAddress() + "/" + incomingFilepathText);


                File fileForLength = new File(incomingFilepathText);
                String filename = Paths.get(incomingFilepathText).getFileName().toString();
                fileoutputSream = new FileOutputStream("D:/TORrent_" + Main.appnumber + "/" + filename);

                System.out.println("Created file : D:/TORrent_" + Main.appnumber + "/" + filename);


                byte[] b = new byte[(int) fileForLength.length() * 2];

                clientIS.read(b, 0, b.length);// I am reading the info that are in the socket Inputstream into a byte array
                fileoutputSream.write(b, 0, b.length); // and writing the array in the file i created on the outputstream
                System.out.println("File succesfully transfered.");


            } else if (incomingText.equals("beginOflist")) { //prints the list of files if this is the case

                String fromServer;
                while ((fromServer = clientBR.readLine()) != null) {
                    if (fromServer.isEmpty())
                        break;
                    System.out.println(fromServer);
                }

            }


        }

    }


    private void exitProgram() throws IOException {

        clientBR.close();
        clientBW.close();
        clientISR.close();
        clientOSW.close();
        clientIS.close();
        clientOS.close();
        clientSocket.close();
        System.exit(0);
    }
}