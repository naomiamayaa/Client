//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.net.DatagramPacket;

public class DatagramParser {
    private static final int OPCODE_LENGTH = 2;

    public DatagramParser() {
    }

    public static int parseRequest(DatagramPacket packet) {
        byte[] data = packet.getData();
        int length = packet.getLength();

        if(length == 0){
            System.out.println("Acknowledgement received from host \nat address: " + packet.getAddress() + " \nfrom port: " + packet.getPort());
            return 0;
        }
        if (length == 4) {
            System.out.print("Packet in bytes: ");

            for(int i = 0; i < 4; ++i) {
                System.out.print(String.format("%02X ", data[i]));
            }

            System.out.println();
            return 0;
        } else {
            byte[] opcodeBytes = new byte[2];
            System.arraycopy(data, 0, opcodeBytes, 0, 2);
            int opcode = (opcodeBytes[0] & 255) << 8 | opcodeBytes[1] & 255;
            if (opcode != 1 && opcode != 2 && opcode != 3) {
                System.out.println("Unsupported opcode: " + opcode);
                return 1;
            } else {
                int i = 2;
                int filenameEnd = indexOfNullByte(data, i);
                String filename = new String(data, i, filenameEnd - i);
                int modeStart = filenameEnd + 1;
                int modeEnd = indexOfNullByte(data, modeStart);
                String mode = new String(data, modeStart, modeEnd - modeStart);
                System.out.println((opcode == 1 ? "Read" : "Write") + " Request");
                System.out.println("From host: " + packet.getAddress());
                System.out.println("Host port: " + packet.getPort());
                System.out.println("Filename: " + filename);
                System.out.println("Mode: " + mode);
                System.out.print("Packet in bytes: ");

                for(i = 0; i < modeEnd + 1; ++i) {
                    System.out.print(String.format("%02X ", data[i]));
                }

                System.out.println();
                return 0;
            }
        }
    }

    private static int indexOfNullByte(byte[] array, int startIndex) {
        for(int i = startIndex; i < array.length; ++i) {
            if (array[i] == 0) {
                return i;
            }
        }

        return array.length;
    }

    public int getOpcode(DatagramPacket packet) {
        byte[] data = packet.getData();
        return (data[0] & 255) << 8 | data[1] & 255;
    }
}
