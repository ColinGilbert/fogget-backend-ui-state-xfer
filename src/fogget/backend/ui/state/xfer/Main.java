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
import java.util.ArrayDeque;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import noob.plantsystem.common.ArduinoConfigChangeRepresentation;
import noob.plantsystem.common.ArduinoProxy;
import noob.plantsystem.common.EventRecord;

/**
 *
 * @author noob
 */
public class Main {
    public static TreeMap<Long, ArduinoProxy> proxies = new TreeMap<>();
    public static TreeMap<Long, ArrayDeque<EventRecord>> events = new TreeMap<>();
    public static TreeMap<Long, String> eventDescriptions = new TreeMap<>();
    public static TreeMap<Long, ArduinoConfigChangeRepresentation> configChanges = new TreeMap<>();
    
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
                    switch (clientMessage) {
                        case "PUTPROXIES": {
                            String data = inFromClient.readLine();
                            proxies = mapper.readValue(data, new TypeReference<TreeMap<Long, ArduinoProxy>>() {
                            });
                            break;
                        }
                        case "GETPROXIES":
                            outToClient.writeBytes(mapper.writeValueAsString(proxies));
                            break;
                        case "PUTEVENTS": {
                            String data = inFromClient.readLine();
                            events = mapper.readValue(data, new TypeReference<TreeMap<Long, ArrayDeque<EventRecord>>>() {
                            });
                            break;
                        }
                        case "GETEVENTS":
                            outToClient.writeBytes(mapper.writeValueAsString(events));
                            break;
                        case "PUTCONFIGCHANGES": {
                            String data = inFromClient.readLine();
                            configChanges = mapper.readValue(data, new TypeReference<TreeMap<Long, ArduinoConfigChangeRepresentation>>() {
                            });
                            break;
                        }
                        case "GETCONFIGCHANGES":
                            outToClient.writeBytes(mapper.writeValueAsString(configChanges));
                            break;
                        case "PUTDESCRIPTIONS": {
                            String data = inFromClient.readLine();
                            eventDescriptions = mapper.readValue(data, new TypeReference<TreeMap<Long, String>>() {
                            });
                            break;
                        }
                        case "DETDESCRIPTIONS":
                            outToClient.writeBytes(mapper.writeValueAsString(events));
                            break;
                        default:
                            System.out.println("Received: " + clientMessage + ". Huh???");
                            break;
                    }
                }
            } catch (JsonProcessingException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
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
