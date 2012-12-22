package com.google.code.corewars8086.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import com.google.code.corewars8086.war.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author BS
 */
public class CompetitionWindow extends JFrame
    implements ScoreEventListener, ActionListener, CompetitionEventListener {

    private Competition competition;
    private ColumnGraph columnGraph;

    // widgets
    private JButton runWarButton;
    private JLabel warCounterDisplay;
    private JButton showBattleButton;
    private JTextField battlesPerGroupField;
    private JTextField warriorsPerGroupField;
    private WarFrame battleFrame;
    private boolean first=true;
    private int warCounter;
    private int totalWars;
    private Thread warThread;
    private boolean showBattleFrame;
	private boolean competitionRunning;

    public CompetitionWindow() throws IOException {
        super("CodeGuru Extreme - Competition Viewer");
        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        competition = new Competition();
        competition.addCompetitionEventListener(this);
        WarriorRepository warriorRepository = competition
                        .getWarriorRepository();
        warriorRepository.addScoreEventListener(this);
        columnGraph = new ColumnGraph(warriorRepository.getGroupNames());
        getContentPane().add(columnGraph, BorderLayout.CENTER);
        // -------------
        JPanel controlArea = new JPanel();
        controlArea.setLayout(new BoxLayout(controlArea, BoxLayout.Y_AXIS));
        // -------------- Button Panel
        JPanel buttonPanel = new JPanel();
        runWarButton = new JButton("<html><font color=red>Start!</font>");
        runWarButton.addActionListener(this);
        buttonPanel.add(runWarButton);
        warCounterDisplay = new JLabel("");
        buttonPanel.add(warCounterDisplay);
        buttonPanel.add(Box.createHorizontalStrut(30));
        showBattleButton = new JButton("Show session");
        showBattleButton.addActionListener(this);
        buttonPanel.add(showBattleButton);
        controlArea.add(buttonPanel);
        // -------------
        controlArea.add(new JSeparator(JSeparator.HORIZONTAL));
        // ------------ Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(new JLabel("Survivor groups per session:"));

        // If total number of teams is less then four, make it the defauld number
		int numberOfGropus = Math.min(4,
            competition.getWarriorRepository().getNumberOfGroups());

		warriorsPerGroupField = new JTextField(String.format("%d", numberOfGropus), 3);
		controlPanel.add(warriorsPerGroupField);
		controlPanel.add(new JLabel("Sessions per groups combination:"));
		battlesPerGroupField = new JTextField("1", 3);
		controlPanel.add(battlesPerGroupField);
        controlArea.add(controlPanel);
        // ------------
        getContentPane().add(controlArea, BorderLayout.SOUTH);

        addWindowListener(new WindowListener() {
            boolean flag=true;
            public void windowOpened(WindowEvent e) {}
            public void windowClosing(WindowEvent e) {
                if (warThread!= null) {
                    competition.setAbort();
                }
            }

            public void windowClosed(WindowEvent e) {}
            public void windowIconified(WindowEvent e) {}
            public void windowDeiconified(WindowEvent e) {}
            public void windowActivated(WindowEvent e) {if(flag){showBattleButton.doClick(); runWarButton.doClick();}flag=false;}
            public void windowDeactivated(WindowEvent e) {}
        });
    }

    /**
     * Starts a new war.
     * @return whether or not a new war was started.
     */
    public boolean runWar() {
        try {
            final int battlesPerGroup = Integer.parseInt(
                battlesPerGroupField.getText().trim());
            final int warriorsPerGroup = Integer.parseInt(
                warriorsPerGroupField.getText().trim());
            if (competition.getWarriorRepository().getNumberOfGroups() < warriorsPerGroup) {
                JOptionPane.showMessageDialog(this,
                    "Not enough survivors (got " +
                    competition.getWarriorRepository().getNumberOfGroups() +
                    " but " + warriorsPerGroup + " are needed)");
                return false;
            }

            warThread = new Thread("CompetitionThread") {
                @Override
                public void run() {
                    try {
                        competition.runCompetition(battlesPerGroup, warriorsPerGroup,warThread);
                    } catch (Exception e) {
                        Logger.getLogger(CompetitionWindow.class.getName()).log(Level.SEVERE, null, e);
                    }
                };
            };
            this.battleFrame.t= warThread;
            if (!competitionRunning) {
            	warThread.start();
            	return true;
            }
        } catch (NumberFormatException e2) {
            //JOptionPane.showMessageDialog(this, "Error in configuration");
            Logger.getLogger(CompetitionWindow.class.getName()).log(Level.SEVERE, null, e2);
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        CompetitionWindow c = new CompetitionWindow();
        c.setVisible(true);
        c.pack();
    }

    public void scoreChanged(String name, float addedValue, int groupIndex, int subIndex) {
        columnGraph.addToValue(groupIndex, subIndex, addedValue);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == runWarButton) {
            if (runWar()) {
            	competitionRunning = true;
		runWarButton.setEnabled(false);
            }
        } else if (e.getSource() == showBattleButton) {
            if (battleFrame == null) {
                if (warThread == null) {
                    // war hasn't started yet
                    WarFrame.flag1=true;
                    showBattleRoom();
                    //for(long i=0;i<900000000;i++);
                    WarFrame.flag1=false;
                } else {
                    // show the battle frame when the next battle starts
                    showBattleFrame = true;
                }
            }
        }
    }

    public void onWarStart() {
        if (showBattleFrame == true) {
            showBattleRoom();
            showBattleFrame = false;
        }
    }

    private void showBattleRoom() {
        competition.setSpeed(5);
        battleFrame = new WarFrame(competition,this.warThread);
        battleFrame.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
            }

            public void windowClosed(WindowEvent e) {
                //System.out.println("BattleFrame=null");
                battleFrame = null;
                competition.setSpeed(Competition.MAXIMUM_SPEED);
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowActivated(WindowEvent e) {
                if(first)
                {
                    battleFrame.setSize(battleFrame.getSize().width+150, battleFrame.getSize().height);
                    first=false;
                }
            }

            public void windowDeactivated(WindowEvent e) {
            }
        });

        competition.addMemoryEventLister(battleFrame);
        competition.addCompetitionEventListener(battleFrame);
        battleFrame.setSize(750, 700);
        battleFrame.setVisible(true);
    }

    public void onWarEnd(int reason, String winners) {
        warCounter++;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                warCounterDisplay.setText("Sessions so far:" + warCounter +
                    " (out of " + totalWars + ")");
            };
        });
    }

    public void onRound(int round) {
    }

    public void onWarriorBirth(Warrior warriorName) {
    }

    public void onWarriorDeath(Warrior warriorName, String reason) {
    }

    public void onCompetitionStart() {
        warCounter = 0;
        totalWars = competition.getTotalNumberOfWars();
    }

    public void onCompetitionEnd() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                warCounterDisplay.setText("The competition is over. " +
                    warCounter + " sessions were run.");

            };
        });

        warThread = null;
        runWarButton.setEnabled(true);
		competitionRunning = false;
    }
}