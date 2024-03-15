import java.net.*;
import java.util.Arrays;

public class Client implements Runnable{
    DatagramPacket in, out, request, response;
    private DatagramSocket sendReceiveSocket;
    private static final int HOST_PORT = 23;
    private DatagramParser parse = new DatagramParser();
    public Client() throws UnknownHostException {
        try {
            this.sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();

            System.exit(1);
        }
    }

    private byte[] createOpcode(int type) {
        byte[] opcode = new byte[]{0, (byte)type};
        return opcode;
    }

    public void buildRequest(int readOrWrite, String filename, String mode) throws UnknownHostException {

        // Set up the outgoing request
        byte[] request = new byte[100];
        byte[] opcode = this.createOpcode(readOrWrite);
        System.arraycopy(opcode, 0, request, 0, opcode.length);
        byte[] filenameBytes = filename.getBytes();
        byte[] modeBytes = mode.getBytes();
        System.arraycopy(filenameBytes, 0, request, 2, filenameBytes.length);
        request[filenameBytes.length + 2] = 0;
        System.arraycopy(modeBytes, 0, request, filenameBytes.length + 3, modeBytes.length);
        request[filenameBytes.length + modeBytes.length + 3] = 0;

        this.out = new DatagramPacket(request, request.length, InetAddress.getLocalHost(), HOST_PORT);

        // set up the incoming request
        byte[] data = new byte[100];

        this.in = new DatagramPacket(data, data.length);
    }

    public void sendRequest(int i){
        System.out.println("\n\nIteration: \u001b[32m\t" + i + "\u001b[0m");

        if(i%2 == 0){
            try {
                buildRequest(1, "test.txt", "netascii");
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                buildRequest(2, "test.txt", "octet");
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        this.rpc_send(this.out, this.in, 5);

        byte[] requestData = new byte[4];
        byte[] receiveData = new byte[4];

        requestData[0] = 0;
        requestData[1] = 1;
        requestData[2] = 0;
        requestData[3] = 1;

        this.request = new DatagramPacket(requestData, requestData.length, in.getAddress(), in.getPort());
        this.response = new DatagramPacket(receiveData, receiveData.length);

        rpc_send(request, response, 5);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException var4) {
            var4.printStackTrace();
        }
    }

    public void rpc_send(DatagramPacket out, DatagramPacket in, int timeout) {
        try {
            System.out.println("\nSending " + requestOrResponse(out) + " packet to host");

            if(requestOrResponse(out).equals("REQUEST")){
                System.out.print("Client request in bytes: ");
                for (int i = 0; i < 4; i++) {
                    System.out.print(String.format("%02X ", out.getData()[i]));
                }
                System.out.println();
            }else{
                parse.parseRequest(out);
            }

            this.sendReceiveSocket.send(out);

            // Set timeout for receiving response
            this.sendReceiveSocket.setSoTimeout(timeout * 1000);

            try {
                this.sendReceiveSocket.receive(in); // Block until response received
                System.out.println("\nPacket received from host: ");

                if(requestOrResponse(in).equals("REQUEST")){
                    System.out.print("Host response in bytes: ");
                    for (int i = 0; i < 4; i++) {
                        System.out.print(String.format("%02X ", in.getData()[i]));
                    }
                    System.out.println();
                }else{
                    parse.parseRequest(in);
                }

                // Process the response data here
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout reached. No response received from host.");
                // Handle timeout
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String requestOrResponse(DatagramPacket packet){

        byte[] request = new byte[4];

        request[0] = 0;
        request[1] = 1;
        request[2] = 0;
        request[3] = 1;

        if (Arrays.equals(packet.getData(), request)) {
            return "REQUEST";
        }
        return "RESPONSE";
    }
    @Override
    public void run() {
        for(int i = 0; i < 5; ++i) {
            sendRequest(i);
        }
    }
    public static void main(String[] args) throws UnknownHostException {
        Thread client;
        client = new Thread(new Client(),"Client");
        client.start();
    }
}
