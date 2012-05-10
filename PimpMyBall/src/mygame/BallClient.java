/*
 * Copyright (c) 2011 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package mygame;

import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.*;

/**
 *  A simple test chat server.  When SM implements a set
 *  of standard chat classes this can become a lot simpler.
 *
 *  @version   $Revision: 8843 $
 *  @author    Paul Speed
 */
public class BallClient extends JFrame {

    private Client client;
    private JEditorPane messageLog;
    private StringBuilder messages = new StringBuilder();
    private JTextField nameField;
    private JTextField messageField;

    public BallClient(String host) throws IOException {
        super("jME3 Test Message Client - to:" + host);

        // Build out the UI       
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 600);

        messageLog = new JEditorPane();
        messageLog.setEditable(false);
        messageLog.setContentType("text/html");
        messageLog.setText("<html><body>");

        getContentPane().add(new JScrollPane(messageLog), "Center");

        // A crude form       
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel("Name:"));
        nameField = new JTextField(System.getProperty("user.name", "yourname"));
        Dimension d = nameField.getPreferredSize();
        nameField.setMaximumSize(new Dimension(120, d.height + 6));
        p.add(nameField);
        p.add(new JLabel("  Message:"));
        messageField = new JTextField();
        p.add(messageField);
        p.add(new JButton(new SendAction(true)));
        p.add(new JButton(new SendAction(false)));

        getContentPane().add(p, "South");

        client = Network.connectToServer(BallServer.NAME, BallServer.VERSION,
                host, BallServer.PORT, BallServer.UDP_PORT);
        client.addMessageListener(new ClientMessageListener(), BallMessage.class);
        client.start();
    }

    public static String getString(Component owner, String title, String message, String initialValue) {
        return (String) JOptionPane.showInputDialog(owner, message, title, JOptionPane.PLAIN_MESSAGE,
                null, null, initialValue);
    }

    public static void main(String... args) throws Exception {
        BallServer.initializeClasses();

        // Grab a host string from the user
        String s = getString(null, "Host Info", "Enter ball host:", "localhost");
        if (s == null) {
            System.out.println("User cancelled.");
            return;
        }

        BallClient test = new BallClient(s);
        test.setVisible(true);
    }

    private class ClientMessageListener implements MessageListener<Client> {
        public void messageReceived(Client source, Message m) {
            BallMessage message = (BallMessage) m;

            System.out.println("Received:" + message);

            // One of the least efficient ways to add text to a
            // JEditorPane
            //messages.append("<font color='#00a000'>" + (m.isReliable() ? "TCP" : "UDP") + "</font>");
            //messages.append(" -- <font color='#000080'><b>" + message.getPosition() + "</b></font> : ");
            messages.append("Position: ").append(message.getPosition());
            messages.append("<br />");
            messages.append("Velocity: ").append(message.getVelocity());
            messages.append("<br />");
            messages.append("Acceleration: ").append(message.getAcceleration());
            messages.append("<br />");
            messages.append("<br />");
            String s = "<html><body>" + messages + "</body></html>";
            messageLog.setText(s);

            // Set selection to the end so that the scroll panel will scroll
            // down.
            messageLog.select(s.length(), s.length());
        }
    }

    private class SendAction extends AbstractAction {

        private boolean reliable;

        public SendAction(boolean reliable) {
            super(reliable ? "TCP" : "UDP");
            this.reliable = reliable;
        }

        public void actionPerformed(ActionEvent evt) {
            String name = nameField.getText();
            String message = messageField.getText();

            Vector3f zeroVector = new Vector3f(0f,0f,0f);
            BallMessage ballMessage = new BallMessage(zeroVector, zeroVector, zeroVector);
            ballMessage.setReliable(reliable);
            System.out.println("Sending:" + ballMessage);
            client.send(ballMessage);
        }
    }
}

