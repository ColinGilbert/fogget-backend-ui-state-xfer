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
    public static TreeMap<Long, String> descriptions = new TreeMap<>();
    public static TreeMap<Long, ArduinoConfigChangeRepresentation> configChanges = new TreeMap<>();
    final static protected long timeOut = 1000;
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
        long lastTime = System.currentTimeMillis();
        while (true) {
            BufferedReader inFromClient = null;
            Socket connectionSocket = null;
            try {
                connectionSocket = welcomeSocket.accept();
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                String clientMessage = inFromClient.readLine();
                if (!isBeingWrittenTo.get()) {
                    final long currentTime = System.currentTimeMillis();
                    final long deltaT = currentTime - lastTime;
                    if (deltaT > timeOut) {
                        System.out.println("No more information. :(");
                    }
                    lastTime = currentTime;
                    ObjectMapper mapper = new ObjectMapper();
                    switch (clientMessage) {
                        case "PUTPROXIES": {
                            String data = inFromClient.readLine();
                            proxies.clear();
                            proxies = mapper.readValue(data, new TypeReference<TreeMap<Long, ArduinoProxy>>() {
                            });
                            break;
                        }
                        case "GETPROXIES": {
                            outToClient.writeBytes(mapper.writeValueAsString(proxies));
                            break;
                        }
                        case "PUTEVENTS": {
                            String data = inFromClient.readLine();
                            events.clear();
                            events = mapper.readValue(data, new TypeReference<TreeMap<Long, ArrayDeque<EventRecord>>>() {
                            });
                            break;
                        }
                        case "GETEVENTS": {
                            outToClient.writeBytes(mapper.writeValueAsString(events));
                            break;
                        }
                        case "PUTDESCRIPTIONS": {
                            String data = inFromClient.readLine();
                            descriptions.clear();
                            descriptions = mapper.readValue(data, new TypeReference<TreeMap<Long, String>>() {
                            });
                            break;
                        }
                        case "GETDESCRIPTIONS": {
                            outToClient.writeBytes(mapper.writeValueAsString(descriptions));
                            break;
                        }
                        default: {
                            System.out.println("Received: " + clientMessage + ". Huh???");
                            break;
                        }
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
                    inFromClient.close();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);

                }
            }
        }
    }
}
