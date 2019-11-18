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

import noob.plantsystem.common.ArduinoProxy;
import noob.plantsystem.common.CommonValues;
import noob.plantsystem.common.EventRecord;
import noob.plantsystem.common.PersistentArduinoState;

/**
 *
 * @author noob
 */
public class Main {

    public static TreeMap<Long, ArduinoProxy> proxies = new TreeMap<>();
    public static TreeMap<Long, ArrayDeque<EventRecord>> events = new TreeMap<>();
    public static TreeMap<Long, String> descriptions = new TreeMap<>();
    public static TreeMap<Long, PersistentArduinoState> configChanges = new TreeMap<>();
    final static Object proxyLock = new Object();
    final static Object eventsLock = new Object();
    final static Object descriptionsLock = new Object();

    public static void main(String[] args) {
        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(CommonValues.localUIPort);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(2);
        }
        while (true) {
            BufferedReader inFromClient = null;
            Socket connectionSocket = null;
            try {
                connectionSocket = welcomeSocket.accept();
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                String clientMessage = inFromClient.readLine();
                ObjectMapper mapper = new ObjectMapper();
                switch (clientMessage) {
                    case (CommonValues.pushProxiesToUI): {

                        String data = inFromClient.readLine();
                        if (!"".equals(data)) {

                            synchronized (proxyLock) {
                                proxies.clear();
                                proxies = mapper.readValue(data, new TypeReference<TreeMap<Long, ArduinoProxy>>() {
                                });
                                proxyLock.notifyAll();
                            }
                        }
                        break;

                    }
                    case (CommonValues.getProxiesForUI): {
                        synchronized (proxyLock) {
                            outToClient.writeBytes(mapper.writeValueAsString(proxies));
                            proxyLock.notifyAll();
                        }
                        break;

                    }
                    case (CommonValues.pushEventsToUI): {
                        String data = inFromClient.readLine();
                        if (!"".equals(data)) {
                            synchronized (eventsLock) {
                                // events.clear();
                                events = mapper.readValue(data, new TypeReference<TreeMap<Long, ArrayDeque<EventRecord>>>() {
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
                        String data = inFromClient.readLine();
                        if (!"".equals(data)) {

                            synchronized (descriptionsLock) {
                                // descriptions.clear();
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
                // connectionSocket.shutdownInput();
                // connectionSocket.shutdownOutput();
                connectionSocket.close();

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
                }
            }
        }
    }
}
