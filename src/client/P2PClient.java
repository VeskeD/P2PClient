package client;

import static util.Constants.HP_GET_ADDRESS;
import static util.Constants.CLIENT_REGISTR;
import static util.Constants.CHECK_CLIENT_C;

import static util.Constants.SERVER_PORT;
import static util.Constants.SERVER_IP;
import static util.Logger.log;

import java.io.IOException;
import java.net.*;

public class P2PClient {
    private static boolean isConnectionEstablished = false;
    public static void main(String[] args) throws InterruptedException {
        log.info("main executed");

        //Emulate multiple clients
        Client client1 = new Client("TEST_CLIENT_1", 1234);
        client1.setOtherClientName("CLIENT_TEST_2");

        Client client2 = new Client("CLIENT_TEST_2", 2345);
        client2.setOtherClientName("TEST_CLIENT_1");

        new Thread(client1).start();
        new Thread(client2).start();

        //Wait while both client will check that connection is established
        Thread.sleep(2000);
        System.out.println();

        if (isConnectionEstablished) {
            //Emulate Chatting between two clients
            client1.sendMessage("Hi Second client");
            Thread.sleep(100);
            client2.sendMessage("Halo, First One");
            Thread.sleep(100);
            client1.sendMessage("How are you client2");
            Thread.sleep(100);
            client2.sendMessage("I'm worked fine client1, and you");
            Thread.sleep(100);
            client1.sendMessage("Yes i'm worked fine also");
            Thread.sleep(100);
            client2.sendMessage("cool, great work");
            Thread.sleep(100);
        }
    }

    private static class Client implements Runnable {
        private String cName;
        private int cPort;

        private DatagramSocket socket;

        private String otherClientName;
        private InetSocketAddress otherAddress;

        Client(String name, int port) {
            this.cName = name;
            this.cPort = port;
        }

        void setOtherClientName(String otherClientName) {
            this.otherClientName = otherClientName;
        }

        @Override
        public void run() {
            log.info(cName + " run executed");

            try {
                socket = new DatagramSocket(cPort);
                log.info(cName + " socket is initialized");
            } catch (SocketException e) {
                log.error(cName + " Cannot initialize socket");
                return;
            }

            clientRegistration();

            //Send request to Server for get OtherClient address.
            getOtherClientAddress(otherClientName);

//            Start Message_Listener for get messages from other Client
            new Thread(new Message_Listener()).start();

//            Check that Client can send message to Other Client
            checkConnection();
        }

        private void sendMessage(String msg) {
            sendMessage(msg, otherAddress);
        }

        private void sendMessage(String msg, InetSocketAddress address) {
            try {
                msg = cName + "::" + msg;
                byte[] buffer = msg.getBytes();

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                packet.setSocketAddress(address);

                socket.send(packet);
            } catch (Exception e) {
                log.error("Failed to send message", e);
            }
        }

        private void clientRegistration() {
            //Initialize address to SERVER IP/PORT and send request
            InetSocketAddress address = new InetSocketAddress(SERVER_IP, SERVER_PORT);
            sendMessage(CLIENT_REGISTR + ":", address);
        }

        private void getOtherClientAddress(String otherClient) {
            //Initialize address to SERVER IP/PORT and send request
            InetSocketAddress address = new InetSocketAddress(SERVER_IP, SERVER_PORT);
            sendMessage(HP_GET_ADDRESS + ":" + otherClient + ":", address);

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            log.info(cName + " IS READY TO GET ADDRESS");

            try {
                socket.receive(packet);
            } catch (IOException e) {
                log.error(cName + " CANNOT GET ADDRESS", e);
            }

            String msg = new String(packet.getData());
            log.info(cName + " < " + packet.getAddress() + ":" + packet.getPort() + " - " + msg);

            String ip = msg.split("::")[1].split(":")[1].substring(1);
            int port = Integer.valueOf(msg.split("::")[1].split(":")[2]);
            otherAddress = new InetSocketAddress(ip, port);
        }

        private void checkConnection() {
            sendMessage(CHECK_CLIENT_C, otherAddress);
        }

        private class Message_Listener implements Runnable {
            //Port Listener that will receive all data
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                try {
                    socket.receive(packet);
                    if (new String(packet.getData()).contains(CHECK_CLIENT_C))
                        isConnectionEstablished = true;
                    else
                        throw new Exception();

                } catch (Exception e) {
                    log.error("Cannot establish connection with otherClient", e);
                    return;
                }

                while(true) {
                    try {
                        buffer = new byte[1024];
                        packet = new DatagramPacket(buffer, buffer.length);

                        socket.receive(packet);

                        String msg = new String(packet.getData());
                        log.info(cName + " < " + msg);
                    } catch (Exception e) {
                        log.error("Cannot get Message", e);
                    }
                }
            }
        }
    }
}