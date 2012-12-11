package me.ibhh.xpShop;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class PanelControl extends JFrame {

    private JPanel hauptPanel;
    private xpShop auc;
    private JButton Button1;
    private JButton Button2;
    private JButton Button3;
    private JRadioButton Radio1;
    private JRadioButton Radio2;
    private ButtonGroup gruppe;
    private GroupLayout layout;

    public PanelControl(xpShop au) {
        super();
        auc = au;
        setTitle("Default options");
        setDefaultCloseOperation(JFrame.NORMAL);
        gruppe = new ButtonGroup();
        layout = new GroupLayout(getContentPane());
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        hauptPanel = init();
        this.getContentPane().add(hauptPanel);

    }

    public JPanel hauptpanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        Button1 = new JButton("Cancel");
        Button1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                if (auc.config.debug) {
                    System.out.println("Button2 get" + event.toString());
                    System.out.println("Source: " + event.getSource());
                }
                PanelControl.this.remove(hauptPanel);
                PanelControl.this.repaint();
                PanelControl.this.dispose();
            }
        });
        Button2 = new JButton("I will edit the config.yml");
        Button2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                if (auc.config.debug) {
                    System.out.println("Button2 get" + event.toString());
                    System.out.println("Source: " + event.getSource());
                }
                auc.getConfig().set("firstRun", false);
                auc.saveConfig();
                auc.reloadConfig();
                auc.config.reload();
                PanelControl.this.remove(hauptPanel);
                PanelControl.this.repaint();
                PanelControl.this.dispose();
            }
        });
        Button3 = new JButton("Finish");
        Button3.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                if (auc.config.debug) {
                    System.out.println("Button2 get" + event.toString());
                    System.out.println("Source: " + event.getSource());
                }
                auc.getConfig().set("firstRun", false);
                auc.saveConfig();
                auc.reloadConfig();
                auc.config.reload();
                PanelControl.this.remove(hauptPanel);
                PanelControl.this.repaint();
                PanelControl.this.dispose();
            }
        });
        JPanel panelunten = new JPanel();
        panelunten.add(Button1);
        if (auc.getConfig().getBoolean("firstRun")) {
            panelunten.add(Button2);
        }
        panelunten.add(Button3);
        JPanel mixed = new JPanel(new BorderLayout());
        mixed.add(getPanel(1), BorderLayout.NORTH);
        mixed.add(getPanel(2), BorderLayout.CENTER);
        JPanel new1 = new JPanel(new BorderLayout());
        new1.add(getPanel(3), BorderLayout.NORTH);
        new1.add(getPanel(4), BorderLayout.CENTER);
        new1.add(getPanel(5), BorderLayout.SOUTH);
        panel.add(mixed, BorderLayout.NORTH);
        panel.add(new1, BorderLayout.CENTER);
        panel.add(panelunten, BorderLayout.SOUTH);
        return panel;
    }

    public JPanel getPanel(int panelindex) {
        if (auc.config.debug) {
            auc.Logger("" + panelindex, "Debug");
        }
        if (panelindex == 1) {
            JPanel panel = new JPanel();
            final JLabel label = new JLabel("Select your language: ");
            Radio1 = new JRadioButton("en");
            Radio2 = new JRadioButton("de");
            Radio1.setSelected(auc.getConfig().getString("language").equalsIgnoreCase("en"));
            Radio2.setSelected(auc.getConfig().getString("language").equalsIgnoreCase("de"));
            Radio1.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    if (auc.config.debug) {
                        System.out.println("Button2 get" + event.toString());
                        System.out.println("Source: " + event.getSource());
                    }
                    auc.getConfig().set("language", "en");
                    auc.saveConfig();
                    auc.reloadConfig();
                    auc.config.reload();
                }
            });
            Radio2.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    if (auc.config.debug) {
                        System.out.println("Button2 get" + event.toString());
                        System.out.println("Source: " + event.getSource());
                    }
                    auc.getConfig().set("language", "de");
                    auc.saveConfig();
                    auc.reloadConfig();
                    auc.config.reload();
                }
            });
            panel.add(label);
            gruppe.add(Radio1);
            gruppe.add(Radio2);
            panel.add(Radio1);
            panel.add(Radio2);
            return panel;
        } else if (panelindex == 2) {
            JPanel panel = new JPanel();
            final JLabel label = new JLabel("Select if debug should be aktivated: ");
            JCheckBox Haken = new JCheckBox("debug mode");
            Haken.setSelected(auc.config.debug);
            Haken.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    if (auc.config.debug) {
                        System.out.println("Button2 get" + event.toString());
                        System.out.println("Source: " + event.getSource());
                    }
                    auc.getConfig().set("debug", !auc.getConfig().getBoolean("debug"));
                    auc.saveConfig();
                    auc.reloadConfig();
                    auc.config.reload();
                }
            });
            panel.add(label);
            panel.add(Haken);
            return panel;
        } else if (panelindex == 3) {
            JPanel panel = new JPanel();
            final JLabel label = new JLabel("moneytoxp: ");
            final JTextField moneytoxp = new JTextField(String.valueOf(auc.config.moneytoxp));
            moneytoxp.setSize(20, 50);
            final JLabel Status = new JLabel();
            final JButton Save = new JButton("Save");
            Save.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    double temp = auc.config.moneytoxp;
                    try {
                        temp = Double.parseDouble(moneytoxp.getText());
                    } catch (Exception e3) {
                        Status.setText("Cant save this value!");
                    }
                    if (auc.config.debug) {
                        System.out.println("Button2 get" + event.toString());
                        System.out.println("Source: " + event.getSource());
                    }
                    try {
                        auc.getConfig().set("moneytoxp", temp);
                    } catch (Exception e2) {
                        Status.setText("Cant save this value!");
                    }
                    auc.saveConfig();
                    auc.reloadConfig();
                    auc.config.reload();
                }
            });
            panel.add(label);
            panel.add(moneytoxp);
            panel.add(Status);
            panel.add(Save);
            return panel;
        } else if (panelindex == 4) {
            JPanel panel = new JPanel();
            final JLabel label = new JLabel("xptomoney: ");
            final JTextField xptomoney = new JTextField(String.valueOf(auc.config.xptomoney));
            xptomoney.setSize(20, 50);
            final JLabel Status = new JLabel();
            final JButton Save = new JButton("Save");
            Save.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    double temp = auc.config.xptomoney;
                    try {
                        temp = Double.parseDouble(xptomoney.getText());
                    } catch (Exception e3) {
                        Status.setText("Cant save this value!");
                    }
                    if (auc.config.debug) {
                        System.out.println("Button2 get" + event.toString());
                        System.out.println("Source: " + event.getSource());
                    }
                    try {
                        auc.getConfig().set("xptomoney", temp);
                    } catch (Exception e2) {
                        Status.setText("Cant save this value!");
                    }
                    auc.saveConfig();
                    auc.reloadConfig();
                    auc.config.reload();
                }
            });
            panel.add(label);
            panel.add(xptomoney);
            panel.add(Status);
            panel.add(Save);
            return panel;
        }else if(panelindex == 5){
            JPanel panel = new JPanel();
            final JLabel label = new JLabel("Select if a new version should be downloaded: ");
            JCheckBox Haken = new JCheckBox("autodownload");
            Haken.setSelected(auc.config.autodownload);
            Haken.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    if (auc.config.debug) {
                        System.out.println("Button2 get" + event.toString());
                        System.out.println("Source: " + event.getSource());
                    }
                    auc.getConfig().set("autodownload", !auc.getConfig().getBoolean("autodownload"));
                    auc.saveConfig();
                    auc.reloadConfig();
                    auc.config.reload();
                }
            });
            panel.add(label);
            panel.add(Haken);
            return panel;
        } else {
            return null;
        }
    }

    /**
     * Komponenten initialisieren
     */
    private JPanel init() {
        JPanel j = hauptpanel();
        return j;
    }
}
