package com.socket;

import com.ui.UI_Chat;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class SocketClient implements Runnable {

    public int port;
    public String serverAddr;
    public Socket socket;
    public UI_Chat ui;
    public ObjectInputStream In;
    public ObjectOutputStream Out;
    public History hist;
    private String archivo ;
    HTMLEditorKit kit = new HTMLEditorKit();
    HTMLDocument doc = new HTMLDocument();

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public SocketClient(UI_Chat frame) throws IOException {
        ui = frame;
        this.serverAddr = ui.serverAddr;
        this.port = ui.port;
        socket = new Socket(InetAddress.getByName(serverAddr), port);

        Out = new ObjectOutputStream(socket.getOutputStream());
        Out.flush();
        In = new ObjectInputStream(socket.getInputStream());

        hist = ui.hist;
    }

    @Override
    public void run() {
        boolean keepRunning = true;
        ui.jTextPane1.setEditorKit(kit);
        ui.jTextPane1.setDocument(doc);
        while (keepRunning) {
            try {
                Message msg = (Message) In.readObject();
                System.out.println("Incoming : " + msg.toString());

                if (msg.type.equals("message")) {
                    if (msg.recipient.equals(ui.username)) {

                        String textReplace = replaceEmoji(msg.content);
                        if (textReplace.trim().isEmpty()) {
                            insertText("[" + msg.sender + " > Yo] : " + msg.content + "\n");
                            // ui.jTextArea1.append("["+ msg.sender +" > "+ msg.recipient +"] : " + msg.content + "\n");
                        } else {

                            insertText("[" + msg.sender + " > " + " Yo] : " + textReplace + "\n");
                            System.out.println("Obteniendo Emoji: " + msg.toString());

                        }

                        //ui.jTextArea1.append("["+msg.sender +" > Me] : " + msg.content + "\n");
                    } else {

                        String textReplace = replaceEmoji(msg.content);
                        if (textReplace.trim().isEmpty()) {
                            insertText("[" + msg.sender + " > " + msg.recipient + "] : " + msg.content + "\n");
                            // ui.jTextArea1.append("["+ msg.sender +" > "+ msg.recipient +"] : " + msg.content + "\n");
                        } else {

                            insertText("[" + msg.sender + " > " + msg.recipient + "] : " + textReplace + "\n");
                            System.out.println("Obteniendo Emoji: " + msg.toString());

                        }
                    }

                    if (!msg.content.equals(".bye") && !msg.sender.equals(ui.username)) {
                        String msgTime = (new Date()).toString();

                        try {
                            hist.addMessage(msg, msgTime);
                            DefaultTableModel table = (DefaultTableModel) ui.historyFrame.jTable1.getModel();
                            table.addRow(new Object[]{msg.sender, msg.content, "Me", msgTime});
                        } catch (Exception ex) {
                        }
                    }
                } else if (msg.type.equals("login")) {
                    if (msg.content.equals("TRUE")) {
                        ui.jButton2.setEnabled(false);
                        ui.jButton3.setEnabled(false);
                        ui.jButton4.setEnabled(true);
                        ui.jButton5.setEnabled(true);
                        insertText("[SERVIDOR > Yo] : Inicio de sesión exitoso\n");
                        //ui.jTextArea1.append("[SERVER > Me] : Login Successful\n");
                        ui.jTextField3.setEnabled(false);
                        ui.jPasswordField1.setEnabled(false);
                    } else {
                        insertText("[SERVIDOR > Yo] : Inicio de sesión fallido\n");
                        //ui.jTextArea1.append("[SERVER > Me] : Login Failed\n");
                    }
                } else if (msg.type.equals("test")) {
                    ui.jButton1.setEnabled(false);
                    ui.jButton2.setEnabled(true);
                    ui.jButton3.setEnabled(true);
                    ui.jTextField3.setEnabled(true);
                    ui.jPasswordField1.setEnabled(true);
                    ui.jTextField1.setEditable(false);
                    ui.jTextField2.setEditable(false);
                    ui.jButton7.setEnabled(true);
                } else if (msg.type.equals("newuser")) {
                    if (!msg.content.equals(ui.username)) {
                        boolean exists = false;
                        for (int i = 0; i < ui.model.getSize(); i++) {
                            if (ui.model.getElementAt(i).equals(msg.content)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            ui.model.addElement(msg.content);
                        }
                    }
                } else if (msg.type.equals("signup")) {
                    if (msg.content.equals("TRUE")) {
                        ui.jButton2.setEnabled(false);
                        ui.jButton3.setEnabled(false);
                        ui.jButton4.setEnabled(true);
                        ui.jButton5.setEnabled(true);
                        insertText("[SERVIDOR > Yo] : Registro Exitoso\n");
                        //ui.jTextArea1.append("[SERVER > Me] : Singup Successful\n");
                    } else {
                        insertText("[SERVIDOR > Yo] : Registro Fallido\n");
                        //ui.jTextArea1.append("[SERVER > Me] : Signup Failed\n");
                    }
                } else if (msg.type.equals("signout")) {
                    if (msg.content.equals(ui.username)) {
                        insertText("[" + msg.sender + " > Yo] : Bye\n");
                        //ui.jTextArea1.append("["+ msg.sender +" > Me] : Bye\n");
                        ui.jButton1.setEnabled(true);
                        ui.jButton4.setEnabled(false);
                        ui.jTextField1.setEditable(true);
                        ui.jTextField2.setEditable(true);

                        for (int i = 1; i < ui.model.size(); i++) {
                            ui.model.removeElementAt(i);
                        }

                        ui.clientThread.stop();
                    } else {
                        ui.model.removeElement(msg.content);
                        insertText("[" + msg.sender + " > Todos] : " + msg.content + " se ha desconectado\n");
                        //ui.jTextArea1.append("["+ msg.sender +" > All] : "+ msg.content +" has signed out\n");
                    }
                } else if (msg.type.equals("upload_req")) {

                    if (JOptionPane.showConfirmDialog(ui, ("Aceptar '" + msg.content + "' de " + msg.sender + " ?")) == 0) {
                        
                        JFileChooser jf = new JFileChooser();
                        jf.setSelectedFile(new File(msg.content));
                        int returnVal = jf.showSaveDialog(ui);
                      
                        String saveTo = jf.getSelectedFile().getPath();
                        if (saveTo != null && returnVal == JFileChooser.APPROVE_OPTION) {
                            Download dwn = new Download(saveTo, ui);
                            Thread t = new Thread(dwn);
                            t.start();
                            insertText("[" + msg.sender + " > " + msg.recipient + "] : "+msg.content + " \n");
                            send(new Message("upload_res", ui.username, ("" + dwn.port), msg.sender));
                        } else {
                            send(new Message("upload_res", ui.username, "NO", msg.sender));
                        }
                    } else {
                        send(new Message("upload_res", ui.username, "NO", msg.sender));
                    }
                } else if (msg.type.equals("upload_res")) {
                    if (!msg.content.equals("NO")) {

                        int port = Integer.parseInt(msg.content);
                        String addr = msg.sender;
                        ui.jButton5.setEnabled(false);
                        ui.jButton6.setEnabled(false);
                        Upload upl = new Upload(addr, port, ui.file, ui);
                        Thread t = new Thread(upl);
                        t.start();
                        insertText("[" + msg.sender + " > " + msg.recipient + "] : Archivo subido correctamente  \n");
             
                    } else {
                        insertText("[SERVIDOR > Yo] : " + msg.sender + " solicitud de archivo rechazada\n");
                        //ui.jTextArea1.append("[SERVER > Me] : "+msg.sender+" rejected file request\n");
                    }
                } else {
                    insertText("[SERVIDOR > Yo] : Tipo de mensaje desconocido\n");
                    //ui.jTextArea1.append("[SERVER > Me] : Unknown message type\n");
                }
            } catch (Exception ex) {
                keepRunning = false;
                try {
                    insertText("[Aplicación > Yo] : Conexión Fallida\n");
                } catch (IOException ex1) {
                    Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex1);
                } catch (BadLocationException ex1) {
                    Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex1);
                }
                //ui.jTextArea1.append("[Application > Me] : Connection Failure\n");
                ui.jButton1.setEnabled(true);
                ui.jTextField1.setEditable(true);
                ui.jTextField2.setEditable(true);
                ui.jButton4.setEnabled(false);
                ui.jButton5.setEnabled(false);
                ui.jButton5.setEnabled(false);

                for (int i = 1; i < ui.model.size(); i++) {
                    ui.model.removeElementAt(i);
                }

                ui.clientThread.stop();

                System.out.println("Exception SocketClient run()");
                ex.printStackTrace();
            }
        }
    }

    public void send(Message msg) {
        try {
            Out.writeObject(msg);
            Out.flush();
            System.out.println("Outgoing : " + msg.toString());

            if (msg.type.equals("message") && !msg.content.equals(".bye")) {
                String msgTime = (new Date()).toString();
                try {
                    hist.addMessage(msg, msgTime);
                    DefaultTableModel table = (DefaultTableModel) ui.historyFrame.jTable1.getModel();
                    table.addRow(new Object[]{"Me", msg.content, msg.recipient, msgTime});
                } catch (Exception ex) {
                }
            }
        } catch (IOException ex) {
            System.out.println("Exception SocketClient send()");
        }
    }

    public void closeThread(Thread t) {
        t = null;
    }

    public void insertText(String text) throws IOException, BadLocationException {
        kit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
        kit.insertHTML(doc, doc.getLength(), "<br/>", 0, 0, null);
    }

    public String replaceEmoji(String text) {
        String doc = "";
        String smileyName = "";
        String replaceString = "";

        if (text.contains(":)")) {
            smileyName = "0.png";
            doc += ""
                    + "<img src= '"
                    + this.getClass().getResource("/images/" + smileyName)
                    + "' width=50 height=50 />";
            replaceString = text.replace(":)", doc);
        } else if (text.contains(":(")) {
            smileyName = "2.png";
            doc += ""
                    + "<img src= '"
                    + this.getClass().getResource("/images/" + smileyName)
                    + "' width=50 height=50 />";
            replaceString = text.replace(":(", doc);
        } else if (text.contains(":<")) {
            smileyName = "1.png";
            doc += ""
                    + "<img src= '"
                    + this.getClass().getResource("/images/" + smileyName)
                    + "' width=50 height=50 />";
            replaceString = text.replace(":<", doc);
        } else if (text.contains(":P")) {
            smileyName = "4.png";
            doc += ""
                    + "<img src= '"
                    + this.getClass().getResource("/images/" + smileyName)
                    + "' width=50 height=50 />";
            replaceString = text.replace(":P", doc);
        } else if (text.contains(":O")) {
            smileyName = "3.png";
            doc += ""
                    + "<img src= '"
                    + this.getClass().getResource("/images/" + smileyName)
                    + "' width=50 height=50 />";
            replaceString = text.replace(":O", doc);
        } else if (text.contains("-_-")) {
            smileyName = "5.png";
            doc += ""
                    + "<img src= '"
                    + this.getClass().getResource("/images/" + smileyName)
                    + "' width=50 height=50 />";
            replaceString = text.replace("-_-", doc);
        } else if (text.contains(":B")) {
            smileyName = "6.png";
            doc += ""
                    + "<img src= '"
                    + this.getClass().getResource("/images/" + smileyName)
                    + "' width=50 height=50 />";
            replaceString = text.replace(":B", doc);
        } else if (text.contains(":*")) {
            smileyName = "9.png";
            doc += ""
                    + "<img src= '"
                    + this.getClass().getResource("/images/" + smileyName)
                    + "' width=50 height=50 />";
            replaceString = text.replace(":*", doc);
        } else if (text.contains(":F")) {
            smileyName = "7.png";
            doc += ""
                    + "<img src= '"
                    + this.getClass().getResource("/images/" + smileyName)
                    + "' width=50 height=50 />";
            replaceString = text.replace(":F", doc);
        } else if (text.contains(":W")) {
            smileyName = "8.png";
            doc += ""
                    + "<img src= '"
                    + this.getClass().getResource("/images/" + smileyName)
                    + "' width=50 height=50 />";
            replaceString = text.replace(":W", doc);
        }
        return replaceString;
    }
}
