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
import java.util.logging.Level;
import java.util.logging.Logger;
import noob.plantsystem.common.EmbeddedSystemCombinedStateMemento;
import noob.plantsystem.common.CommonValues;
import noob.plantsystem.common.EventRecordMemento;
//import noob.plantsystem.common.PersistentArduinoState;

/**
 *
 * @author noob
 */
public class Main {

    public static TreeMap<Long, EmbeddedSystemCombinedStateMemento> proxies = new TreeMap<>();
    public static TreeMap<Long, ArrayDeque<EventRecordMemento>> events = new TreeMap<>();
    public static TreeMap<Long, String> descriptions = new TreeMap<>();
    //public static TreeMap<Long, PersistentArduinoState> configChanges = new TreeMap<>();
    public static ObjectMapper mapper = new ObjectMapper();

    final static Object proxyLock = new Object();
    final static Object eventsLock = new Object();
    final static Object descriptionsLock = new Object();
    static Socket connectionSocket = null;
    static BufferedReader inFromClient = null;
    static DataOutputStream outToClient = null; 
    static String clientMessage = null;
    static String data =  null;
    public static void main(String[] args) {
        
        ServerSocket welcomeSocket = null;
        
        try {
            welcomeSocket = new ServerSocket(CommonValues.localUIPort);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(2);
        }
        
        while (true) {
            try {
                connectionSocket = welcomeSocket.accept();
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                clientMessage = inFromClient.readLine();
                switch (clientMessage) {
                    case (CommonValues.pushSystemsToUI): {
                        data = inFromClient.readLine();
                        if (!"".equals(data)) {
                            synchronized (proxyLock) {
                                proxies.clear();
                                proxies = mapper.readValue(data, new TypeReference<TreeMap<Long, EmbeddedSystemCombinedStateMemento>>() {
                                });
                                proxyLock.notifyAll();
                            }
                        }
                        break;

                    }
                    case (CommonValues.getSystemsForUI): {
                        synchronized (proxyLock) {
                            outToClient.writeBytes(mapper.writeValueAsString(proxies));
                            proxyLock.notifyAll();
                        }
                        break;

                    }
                    case (CommonValues.pushEventsToUI): {
                        data = inFromClient.readLine();
                        if (!"".equals(data)) {
                            synchronized (eventsLock) {
                                events.clear();
                                events = mapper.readValue(data, new TypeReference<TreeMap<Long, ArrayDeque<EventRecordMemento>>>() {
                                });
                                eventsLock.notifyAll();
                            }
                        }
                        break;

                    }
                    case (CommonValues.getEventsForUI): {
                        synchronized (eventsLock) {
                            outToClient.writeBytes(mapper.writeValueAsString(events));
                        }
                        break;
                    }
                    case (CommonValues.pushDescriptionsToUI): {
                        data = inFromClient.readLine();
                        if (!"".equals(data)) {

                            synchronized (descriptionsLock) {
                                descriptions.clear();
                                descriptions = mapper.readValue(data, new TypeReference<TreeMap<Long, String>>() {
                                });
                                descriptionsLock.notifyAll();
                            }
                        }
                        break;
                    }
                    case (CommonValues.getDescriptionsForUI): {
                        synchronized (descriptionsLock) {
                            outToClient.writeBytes(mapper.writeValueAsString(descriptions));
                            descriptionsLock.notifyAll();
                        }
                        break;
                    }
                    default: {
                        System.out.println("Received: " + clientMessage + ". Huh???");
                        break;
                    }
                }
            } catch (JsonProcessingException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    connectionSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
