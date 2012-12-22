package com.google.code.corewars8086.gui;

import com.google.code.corewars8086.memory.MemoryException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.code.corewars8086.memory.MemoryEventListener;
import com.google.code.corewars8086.memory.RealModeAddress;
import com.google.code.corewars8086.utils.Unsigned;
import com.google.code.corewars8086.war.*;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * The main GUI class for core-wars.
 * The frame includes:
 * <ul>
 * <li> Canvas for showing the memory
 * <li> a list of warrior names
 * <li> messaging area
 * <li> start/stop buttons
 * <li> speed slider
 * </ul>
 *
 * @author BS
 */
public class WarFrame extends JFrame
    implements MemoryEventListener,  CompetitionEventListener,ActionListener{

    /** the canvas which show the core war memory area */
    private Canvas warCanvas;

    /** the message area show misc. information about the current fight */
    private JTextArea messagesArea;

    /** list of warrior names */
    private JList nameList;

    /** Model for the name list */
    private DefaultListModel nameListModel;

    public Thread t;
    public JButton runStep;
    public JButton runNormally;
    /** Holds the current round number */
    private int nRoundNumber;

    /** A text field showing the current round number */
    private JTextField roundNumber;

    private JSlider speedSlider;

    private final Competition competition;

    public WarFrame(Competition competition,Thread t) {
        super("CodeGuru Extreme - Session Viewer");
        this.t=t;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.competition = competition;
        getContentPane().setLayout(new BorderLayout());

        // build widgets
        JPanel mainPanel = new JPanel(new BorderLayout());

        // build war zone (canvas + title)
        JPanel warZone = new JPanel(new BorderLayout());
        warZone.setBackground(Color.BLACK);

        JPanel canvasPanel = new JPanel();
        canvasPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(169,154,133),3),
            BorderFactory.createEmptyBorder(10,10,20,10)));
        canvasPanel.setBackground(Color.BLACK);
        warCanvas = new Canvas();
        canvasPanel.add(warCanvas);
        warZone.add(canvasPanel, BorderLayout.CENTER);

        //warZone.add(new JLabel(new ImageIcon("images/warzone.jpg")), BorderLayout.NORTH);
        mainPanel.add(warZone, BorderLayout.CENTER);

        // build info zone (message area + buttons)
        JPanel infoZone = new JPanel(new BorderLayout());
        messagesArea = new JTextArea(5, 60);
        messagesArea.setFont(new Font("Tahoma", Font.PLAIN, 12));

        infoZone.add(new JScrollPane(messagesArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(new JLabel("Round:"));
        roundNumber = new JTextField(4);
        roundNumber.setEnabled(false);
        buttonPanel.add(roundNumber);
        buttonPanel.add(Box.createHorizontalStrut(20));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(closeButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(new JLabel("Speed:"));
        speedSlider = new JSlider(1,50,competition.getSpeed());
        speedSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                WarFrame.this.competition.setSpeed(speedSlider.getValue());
            }
        });
        buttonPanel.add(speedSlider);
        nRoundNumber = 0;
        infoZone.add(buttonPanel, BorderLayout.SOUTH);
        infoZone.setBackground(Color.black);

        // build warrior zone (warrior list + title)
        JPanel warriorZone = new JPanel(new BorderLayout());
        warriorZone.setBackground(Color.BLACK);
        nameListModel = new DefaultListModel();
        nameList = new JList(nameListModel);
        nameList.setPreferredSize(new Dimension(340,0));
        nameList.setCellRenderer(new NameCellRenderer());
        nameList.setOpaque(false);
        nameList.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(169,154,133),3),
            BorderFactory.createEmptyBorder(10,10,20,10)));
        //.scrollRectToVisible(new Rectangle(0,0,10,50));
        /*JScrollPane Scroll=new JScrollPane(nameList);
        nameList.setLayoutOrientation(JList.VERTICAL);
        nameList.repaint();*/
        warriorZone.add(nameList, BorderLayout.CENTER);
        //warriorZone.add(Scroll);
        //Scroll.getViewport().add(nameList);
        //warriorZone.add(new JLabel(new ImageIcon("images/warriors.jpg")), BorderLayout.NORTH);
        warriorZone.add(Box.createHorizontalStrut(20), BorderLayout.WEST);
        mainPanel.add(warriorZone, BorderLayout.EAST);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        //getContentPane().add(new JLabel(new ImageIcon("images/title2.png")), BorderLayout.EAST);
        getContentPane().add(infoZone, BorderLayout.SOUTH);
        JPanel controlArea = new JPanel();
        controlArea.setLayout(new BoxLayout(controlArea, BoxLayout.Y_AXIS));
        // -------------- Button Panel
        this.runStep = new JButton("<html><font color=red>Step</font>");
        runStep.addActionListener(this);
        buttonPanel.add(runStep);
        this.runNormally = new JButton("Continue Runnig");
        runNormally.addActionListener(this);
        buttonPanel.add(runNormally);
        controlArea.add(buttonPanel);
        // -------------
        controlArea.add(new JSeparator(JSeparator.HORIZONTAL));
        getContentPane().add(controlArea, BorderLayout.SOUTH);
        //this.setPreferredSize(new Dimension(this.getSize().width+150, this.getSize().height));
    }


    /** Add a message to the message zone */
    public void addMessage(String message) {
        messagesArea.append(message + "\n");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
            }
        });
    }

    /** Add a message to the message zone (with round number) */
    public void addMessage(int round, String message) {
        addMessage("[" + round + "] "+ message);
    }
    public static boolean flag1=true;
    /** @see MemoryEventListener#onMemoryWrite(RealModeAddress) */
    public void onMemoryWrite(RealModeAddress address) {
        // we are only interested in addresses in the ARENA segment
        // FIXME: calculate using linear addresses to suport CS+1
        if (address.getSegment() != War.ARENA_SEGMENT) {
            return;
        }
        warCanvas.paintPixel(
            Unsigned.unsignedShort(address.getOffset()),
            (flag1?(byte)(competition.getCurrentWarrior()-1):(byte)competition.getCurrentWarrior()));
    }

    /** @see CompetitionEventListener#onWarStart(int) */
    public void onWarStart() {
        addMessage("=== Session started ===");
        nameListModel.clear();
        warCanvas.clear();
    }

    /** @see CompetitionEventListener#onWarEnd(int, String) */
    public void onWarEnd(int reason, String winners) {
        roundNumber.setText(Integer.toString(nRoundNumber));
        roundNumber.repaint();

        switch (reason) {
            case SINGLE_WINNER:
                addMessage(nRoundNumber,
                    "Session over: The winner is " + winners + "!");

                break;
            case MAX_ROUND_REACHED:
                addMessage(nRoundNumber,
                    "Maximum round reached: The winners are " + winners + "!");
                break;
            case ABORTED:
                addMessage(nRoundNumber,
                    "Session aborted: The winners are " + winners + "!");
                break;
            default:
                throw new RuntimeException();
        }
    }

    /** @see CompetitionEventListener#onRound(int) */
    public void onRound(int round) {
        nameList.repaint();
        nRoundNumber = round;
        if ((nRoundNumber % 1000) == 0) {
            roundNumber.setText(Integer.toString(nRoundNumber));
            roundNumber.repaint();
        }
    }

    /** @see CompetitionEventListener#onWarriorBirth(String) */
    public void onWarriorBirth(Warrior warriorName) {
        addMessage(nRoundNumber, warriorName.getName() + " enters the arena.");
        nameListModel.addElement(warriorName);
    }

    /** @see CompetitionEventListener#onWarriorDeath(String) */
    public void onWarriorDeath(Warrior warrior, String reason) {
        String warriorName=warrior.getName();
        addMessage(nRoundNumber, warriorName + " died due to " + reason + ".");
        Enumeration namesListElements = nameListModel.elements();
        while(namesListElements.hasMoreElements()) {
            Warrior info = (Warrior) namesListElements.nextElement();
            if (info.m_name.equals(warriorName)) {
                info.m_isAlive = false;
                break;
            }
        }


        // a bit bogus... just to make the list refresh and show the new status.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nameList.repaint();
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.runStep) {
            this.competition.StepByStep=true;
            t.resume();
            //synchronized(this){
            //t.notifyAll();
            //}
        } else if (e.getSource() == this.runNormally) {
            this.competition.StepByStep=false;
            t.resume();
             //synchronized(this){
           // t.notifyAll();
            //}
        }
    }

    /**
     * A renderer for the names on the warrior list.
     * Paints each warrior with its color and uses <S>strikeout</S> to show
     * dead warriors.
     */
    class NameCellRenderer extends JLabel implements ListCellRenderer {
        private static final int FONT_SIZE = 12;

        /**
         * Construct a name cell renderer
         * Set font size to FONT_SIZE.
         */
        public NameCellRenderer() {
           setFont(new Font("Tahoma", Font.PLAIN, FONT_SIZE));
        }

        /**
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
         int nnn=1;
        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            Warrior info = (Warrior)value;
            RealModeAddress address1 = new RealModeAddress(
            info.m_state.getCS(),(short)(info.m_state.getIP()));
            warCanvas.paintPixel( Unsigned.unsignedShort(address1.getOffset()),(byte)4);
            warCanvas.repaint();

            /*
            float warriorScore = m_warSession.m_scoreBoard.getScore(warriorName);
            warriorScore = (float)((int)(warriorScore * 100)) / 100;
            */
            String text = info.m_name;// + " (" + warriorScore + ")";
            if (!info.m_isAlive) {
                // strike out dead warriors
                text = "<S>" + text + "</S>";
            }
            String line="";
            if(!flag1){
            byte[] b=new byte[5];
            for(short i=0;i<5;i++){
            RealModeAddress address = new RealModeAddress(
            info.m_state.getCS(),(short)(info.m_state.getIP()+i));
            try {
                b[i]=info.m_memory.readExecuteByte(address);
            } catch (MemoryException ex) {
                Logger.getLogger(WarFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
            String temp="";
            try {
                FileOutputStream fos = new FileOutputStream("temp.bin");
                 fos.write(b);
                 fos.close();
                 Runtime r = Runtime.getRuntime();
                Process p= r.exec("Nasm\\ndisasm.exe temp.bin");
                p.waitFor();
                BufferedReader is= new BufferedReader(new InputStreamReader(p.getInputStream()));
                 for(int i=0;i<2&&is.ready();i++){
                     temp=is.readLine();
                     temp=temp.substring(temp.indexOf("                ")+16);
                     line += temp+"<br/>";
                 }
            } catch (Exception ex) {
                Logger.getLogger(WarFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
            setText("<html>"+text
                    +"<br/><B><U>AX</U></B>:"+String.format("%04x",info.m_state.getAX()/*+32768*/)+"  <B><U>BX</U></B>:"+String.format("%04x",info.m_state.getBX()/*+32768*/)
                    +"  <B><U>CX</U></B>:"+String.format("%04x",info.m_state.getCX()/*+32768*/)+"  <B><U>DX</U></B>:"+String.format("%04x",info.m_state.getDX()/*+32768*/)
                    +"  <B><U>BP</U></B>:"+String.format("%04x",info.m_state.getBP()/*+32768*/)+"<br/><B><U>CS</U></B>:"+String.format("%04x",info.m_state.getCS()/*+32768*/)
                    +"  <B><U>DS</U></B>:"+String.format("%04x",info.m_state.getDS()/*+32768*/)+"  <B><U>ES</U></B>:"+String.format("%04x",info.m_state.getES()/*+32768*/)
                    +"  <B><U>IP</U></B>:"+String.format("%04x",info.m_state.getIP()/*+32768*/)+"  <B><U>SI</U></B>:"+String.format("%04x",info.m_state.getSI()/*+32768*/)
                    +"<br/><B><U>SP</U></B>:"+String.format("%04x",info.m_state.getSP()/*+32768*/)+"  <B><U>SS</U></B>:"+String.format("%04x",info.m_state.getSS()/*+32768*/)
                    +"  <B><U>DI</U></B>:"+String.format("%04x",info.m_state.getDI()/*+32768*/)
                    +"  <B><U>DF</U></B>:"+(info.m_state.getDirectionFlag()/*+32768*/)
                    +"  <B><U>ZF</U></B>:"+(info.m_state.getZeroFlag()/*+32768*/)
                    +"<br/>"+line
                    +"</html>");
            setForeground(warCanvas.getColorForWarrior(index));
            return this;
        }
    }

    public void onCompetitionStart() {
    }

    public void onCompetitionEnd() {
    }

    class WarriorInfo {
        String name;
        boolean alive;

        public WarriorInfo(String name) {
            this.name= name;
            this.alive = true;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj!=null) && (obj instanceof String) &&
                (((String)obj).equals(name));
        }
    }
}