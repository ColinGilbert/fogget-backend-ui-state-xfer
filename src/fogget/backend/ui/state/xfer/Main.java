/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fogget.backend.ui.state.xfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import noob.plantsystem.common.ArduinoProxy;

/**
 *
 * @author noob
 */
public class Main {

    public static ArrayList<ArduinoProxy> proxies = new ArrayList<>();
    private static final AtomicBoolean isBeingWrittenTo = new AtomicBoolean(false);

    public static void main(String[] args) {
        // TODO code application logic here

        ServerSocket welcomeSocket;
        try {
            welcomeSocket = new ServerSocket(6777);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        while (true) {
            BufferedReader inFromClient = null;
            try {
                Socket connectionSocket = welcomeSocket.accept();
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                String clientMessage = inFromClient.readLine();
                if (!isBeingWrittenTo.get()) {

                    ObjectMapper mapper = new ObjectMapper();
                    if (clientMessage.equals("PUT")) {

                        String data = inFromClient.readLine();
                        try {
                            proxies = mapper.readValue(data, new TypeReference<ArrayList<ArduinoProxy>>() {
                            });
                        } catch (JsonProcessingException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            continue;
                        }
                    } else if (clientMessage.equals("GET")) {
                        outToClient.writeBytes(mapper.writeValueAsString(proxies));
                    } else {
                        System.out.println("Received: " + clientMessage);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    inFromClient.close();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
