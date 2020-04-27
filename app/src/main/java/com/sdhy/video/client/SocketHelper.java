package com.sdhy.video.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketHelper {
    public boolean connected = false;

    private Socket client = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;

    public boolean open(String serverAddr, int serverPort) {
        try {
            client = new Socket(serverAddr, serverPort);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());
            connected = true;
            //Log.i(this.getClass().getName(), "socket connected");
            return true;
        } catch (UnknownHostException e) {
            close();
            //Log.e(this.getClass().getName(), e.getMessage());
            return false;
        } catch (IOException e) {
            close();
            //Log.e(this.getClass().getName(), e.getMessage());
            return false;
        }
    }

    public void close() {
        connected = false;
        try {
            if (dis != null) {
                dis.close();
                dis = null;
            }
        } catch (IOException e) {
            //Log.e(this.getClass().getName(), e.getMessage());
        }

        try {
            if (dos != null) {
                dos.close();
                dos = null;
            }
        } catch (IOException e) {
            //Log.e(this.getClass().getName(), e.getMessage());
        }

        try {
            if (client != null) {
                client.close();
                client = null;
            }
        } catch (IOException e) {
            //Log.e(this.getClass().getName(), e.getMessage());
        }
        //Log.i(this.getClass().getName(), "socket closed");
    }

    public boolean send(byte[] dataBuffer) {
        try {
            if (!connected) {
                return false;
            }
            dos.write(dataBuffer);
            return true;
        } catch (IOException e) {
            //Log.e(this.getClass().getName(), e.getMessage());
            return false;
        }
    }

    public int recv(byte[] dataBuffer) {
        try {
            if (!connected) {
                return 0;
            }
            return dis.read(dataBuffer);
        } catch (IOException e) {
            //Log.e(this.getClass().getName(), e.getMessage());
            return -1;
        }
    }
}