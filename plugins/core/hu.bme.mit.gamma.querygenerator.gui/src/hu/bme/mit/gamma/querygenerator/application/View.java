/********************************************************************************
 * Copyright (c) 2018 Contributors to the Gamma project
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * SPDX-License-Identifier: EPL-1.0
 ********************************************************************************/
package hu.bme.mit.gamma.querygenerator.application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;

import hu.bme.mit.gamma.querygenerator.controller.AbstractController;
import hu.bme.mit.gamma.querygenerator.controller.ThetaController;

/**
 * @author Bence Graics
 */
public class View extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private static final String A_ALL_TEXT = "A[]: The model must always satisfy the following condition during every behavior.";
	private static final String A_ALL_EXAMPLE = "A critical error must never occur.";
	private static final String A_SOME_TEXT = "A<>: The model must reach a state where the following condition holds.";
	private static final String A_SOME_EXAMPLE = "The system must shut down eventually.";	
	private static final String E_SOME_TEXT = "E<>: It is possible to reach a state where the following condition holds.";
	private static final String E_SOME_EXAMPLE	= "The system should be able to initialize.";	
	private static final String E_ALL_TEXT = "E[]: The model may always satisfy the following condition during some behavior.";
	private static final String E_ALL_EXAMPLE = "It is possible to keep the system in a stable state.";	
	private static final String IMPLICATION = "-->: If condition A ever holds in a state, then a state must be reached after that where condition B holds.";
	private static final String IMPLICATION_EXAMPLE = "Whenever a request is received, there must be a response.";
	
	protected static final String MIGHT_EVENTUALLY = "\"Might eventually\"";
	protected static final String MUST_EVENTUALLY = "\"Must eventually\"";
	protected static final String MIGHT_ALWAYS = "\"Might always\"";
	protected static final String MUST_ALWAYS = "\"Must always\"";
	protected static final String LEADS_TO = "\"Leads to\"";
	
	private static final Dimension UP_RIGHT_PANEL_SIZE = new Dimension(405, 30);
	private static final Dimension MAX_PANEL_SIZE = new Dimension(2000, 30);
	
	private JMenuBar menuBar;
	private JMenu optionsMenu;
	
	private JMenu modelCheckingOptionsMenu;
	
	private JMenuItem traversalMenu;	
	private ButtonGroup searchOrderGroup;
	private JRadioButtonMenuItem breadthFirst;
	private JRadioButtonMenuItem depthFirst;
	private JRadioButtonMenuItem randomDepthFirst;
	private JRadioButtonMenuItem optimalFirst;
	private JRadioButtonMenuItem randomOptimalDepthFirst;
	
	private JMenuItem traceMenu;	
	private ButtonGroup traceGroup;
	private JRadioButtonMenuItem someTrace;
	private JRadioButtonMenuItem shortestTrace;
	private JRadioButtonMenuItem fastestTrace;
	
	private JMenuItem stateSpaceRepresentationMenu;	
	private ButtonGroup stateSpaceRepresentationGroup;
	private JRadioButtonMenuItem dbm;
	private JRadioButtonMenuItem overApproximation;
	private JRadioButtonMenuItem underApproximation;
	
	private JMenuItem hashtableSizeMenu;	
	private ButtonGroup hashtableSizeGroup;
	private JRadioButtonMenuItem size64M;
	private JRadioButtonMenuItem size256M;
	private JRadioButtonMenuItem size512M;
	private JRadioButtonMenuItem size1024M;
	
	private JMenuItem spaceStateReductionMenu;	
	private ButtonGroup spaceStateReductionGroup;
	private JRadioButtonMenuItem noSpaceStateReduction;
	private JRadioButtonMenuItem conservativeSpaceStateReduction;
	private JRadioButtonMenuItem aggressiveSpaceStateReduction;
	
	private JMenu testGenerationTimeoutMenu;

	private JRadioButtonMenuItem reuseStateSpaceItem;
	private JRadioButtonMenuItem optimizeTestSetItem;
	
	private JMenuItem testGenerationTimeoutMenuItem;
	private ButtonGroup testGenerationTimeoutGroup;
	private JRadioButtonMenuItem sec15;
	private JRadioButtonMenuItem sec30;
	private JRadioButtonMenuItem sec60;
	private JRadioButtonMenuItem sec90;
	private JRadioButtonMenuItem sec180;
	private JRadioButtonMenuItem sec360;
	
	private JTextArea exampleTextArea;
	private JTextArea helpTextArea;
	
	private JPanel optionalPanel;	

	private JComboBox<String> stateSelector = new JComboBox<String>();
	private JComboBox<String> variableSelector = new JComboBox<String>();
	private JComboBox<String> eventSelector = new JComboBox<String>();
	private JComboBox<String> operatorSelector = new JComboBox<String>(new String[] {"AND", "OR", "IMPLICATION", "NEGATION", "EQUALITY"});

	private JTextField resultText;
	private JTextField optionalText;
	private JTextField primeText;
	
	private JTextField activeText;

	private JButton verifyButton;
	private JButton resetButton;
	private JButton generateTestSetButton;
	private JLabel verificationResultLabel;

	private JComboBox<String> howToList;

	private JLabel primeLabel;
	private JLabel optionalLabel;
	private JLabel resultLabel;
	
	private JTextArea logTextArea;
	
	private AbstractController controller;
	
	// The location of the model on which this query generator is opened
	// E.g.: F:/eclipse_ws/sc_analysis_comp_oxy/runtime-New_configuration/hu.bme.mit.inf.gamma.tests/model/TestOneComponent.statechartmodel
	public View(IFile file) throws IOException {
		controller = new ThetaController(this, file);
		setDefaultCloseOperation(2);
		setFrameSizeSmaller();
		
		// Setting the menu bar
		menuBar = new JMenuBar();
		optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);
		setJMenuBar(menuBar);
		
		// Model checking options
		modelCheckingOptionsMenu = new JMenu("Model Checking");
		optionsMenu.add(modelCheckingOptionsMenu);
		
		// Setting the search order
		traversalMenu = new JMenu("Search Order");
		breadthFirst = new JRadioButtonMenuItem("Breadth First");
		depthFirst = new JRadioButtonMenuItem("Depth First");
		randomDepthFirst = new JRadioButtonMenuItem("Random Depth First");
		optimalFirst = new JRadioButtonMenuItem("Optimal First");
		randomOptimalDepthFirst = new JRadioButtonMenuItem("Random Optimal Depth First");
		breadthFirst.setSelected(true);
		searchOrderGroup = new ButtonGroup();
		searchOrderGroup.add(breadthFirst);
		searchOrderGroup.add(depthFirst);
		searchOrderGroup.add(randomDepthFirst);
		searchOrderGroup.add(optimalFirst);
		searchOrderGroup.add(randomOptimalDepthFirst);
	    traversalMenu.add(breadthFirst);
	    traversalMenu.add(depthFirst);
	    traversalMenu.add(randomDepthFirst);
	    traversalMenu.add(optimalFirst);
	    traversalMenu.add(randomOptimalDepthFirst);
		
	    modelCheckingOptionsMenu.add(traversalMenu);
	    
	    // Setting the diagnostic trace
	 	traceMenu = new JMenu("Diagnostic Trace");
	 	someTrace = new JRadioButtonMenuItem("Some");
	 	shortestTrace = new JRadioButtonMenuItem("Shortest");
	 	fastestTrace = new JRadioButtonMenuItem("Fastest");
	 	someTrace.setSelected(true);
	 	traceGroup = new ButtonGroup();
	 	traceGroup.add(someTrace);
	 	traceGroup.add(shortestTrace);
	 	traceGroup.add(fastestTrace);
	 	traceMenu.add(someTrace);
	 	traceMenu.add(shortestTrace);
	 	traceMenu.add(fastestTrace);
	 		
	 	modelCheckingOptionsMenu.add(traceMenu);
	 	
	    // Setting the hashtable size
	 	hashtableSizeMenu = new JMenu("Hash Table Size");
	 	size64M = new JRadioButtonMenuItem("64 MB");
	 	size256M = new JRadioButtonMenuItem("256 MB");
	 	size512M = new JRadioButtonMenuItem("512 MB");
	 	size1024M = new JRadioButtonMenuItem("1024 MB");
	 	size512M.setSelected(true);
	 	hashtableSizeGroup = new ButtonGroup();
	 	hashtableSizeGroup.add(size64M);
	 	hashtableSizeGroup.add(size256M);
	 	hashtableSizeGroup.add(size512M);
	 	hashtableSizeGroup.add(size1024M);
	 	hashtableSizeMenu.add(size64M);
	 	hashtableSizeMenu.add(size256M);
	 	hashtableSizeMenu.add(size512M);
	 	hashtableSizeMenu.add(size1024M);
	 		
	 	modelCheckingOptionsMenu.add(hashtableSizeMenu);
	 	
	    // Setting the test generation timeout
	 	testGenerationTimeoutMenu = new JMenu("Test Generation");
	 	
	 	reuseStateSpaceItem = new JRadioButtonMenuItem("Reuse State Space");
	 	reuseStateSpaceItem.addActionListener(new ActionListener() {
			private boolean wasOptimizeTestSetItemSelected = false;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (reuseStateSpaceItem.isSelected()) {
					optimizeTestSetItem.setEnabled(true);
					optimizeTestSetItem.setSelected(wasOptimizeTestSetItemSelected); 
				}
				else {
					optimizeTestSetItem.setEnabled(false);
					wasOptimizeTestSetItemSelected = optimizeTestSetItem.isSelected();
					optimizeTestSetItem.setSelected(false); 
				}
			}
		});
	 	optimizeTestSetItem = new JRadioButtonMenuItem("Optimize Test Set");
	 	optimizeTestSetItem.setEnabled(false);
	 	
	 	testGenerationTimeoutMenuItem = new JMenu("Test Generation Timeout");
	 	sec15 = new JRadioButtonMenuItem("15 sec");
	 	sec30 = new JRadioButtonMenuItem("30 sec");
	 	sec60 = new JRadioButtonMenuItem("60 sec");
	 	sec90 = new JRadioButtonMenuItem("90 sec");
	 	sec180 = new JRadioButtonMenuItem("180 sec");
	 	sec360 = new JRadioButtonMenuItem("360 sec");
	 	sec90.setSelected(true);
	 	testGenerationTimeoutGroup = new ButtonGroup();
	 	testGenerationTimeoutGroup.add(sec15);
	 	testGenerationTimeoutGroup.add(sec30);
	 	testGenerationTimeoutGroup.add(sec60);
	 	testGenerationTimeoutGroup.add(sec90);
	 	testGenerationTimeoutGroup.add(sec180);
	 	testGenerationTimeoutGroup.add(sec360);
	 	testGenerationTimeoutMenuItem.add(sec15);
	 	testGenerationTimeoutMenuItem.add(sec30);
	 	testGenerationTimeoutMenuItem.add(sec60);
	 	testGenerationTimeoutMenuItem.add(sec90);
	 	testGenerationTimeoutMenuItem.add(sec180);
	 	testGenerationTimeoutMenuItem.add(sec360);

	 	testGenerationTimeoutMenu.add(reuseStateSpaceItem);
	 	testGenerationTimeoutMenu.add(optimizeTestSetItem);
	 	testGenerationTimeoutMenu.add(testGenerationTimeoutMenuItem);
	 		
	 	optionsMenu.add(testGenerationTimeoutMenu);
	 	
	    // Setting the state space representation
	 	stateSpaceRepresentationMenu = new JMenu("State Space Representation");
	 	dbm = new JRadioButtonMenuItem("DBM");
	 	overApproximation = new JRadioButtonMenuItem("Over Approximation");
	 	underApproximation = new JRadioButtonMenuItem("Under Approximation");
	 	dbm.setSelected(true);
	 	stateSpaceRepresentationGroup = new ButtonGroup();
	 	stateSpaceRepresentationGroup.add(dbm);
	 	stateSpaceRepresentationGroup.add(overApproximation);
	 	stateSpaceRepresentationGroup.add(underApproximation);
	 	stateSpaceRepresentationMenu.add(dbm);
	 	stateSpaceRepresentationMenu.add(overApproximation);
	 	stateSpaceRepresentationMenu.add(underApproximation);
	 		
	 	modelCheckingOptionsMenu.add(stateSpaceRepresentationMenu);
	 	
	    // Setting the state space reduction
	 	spaceStateReductionMenu = new JMenu("State Space Reduction");
	 	noSpaceStateReduction = new JRadioButtonMenuItem("None");
	 	conservativeSpaceStateReduction = new JRadioButtonMenuItem("Conservative");
	 	aggressiveSpaceStateReduction = new JRadioButtonMenuItem("Aggressive");
	 	conservativeSpaceStateReduction.setSelected(true);
	 	spaceStateReductionGroup = new ButtonGroup();
	 	spaceStateReductionGroup.add(noSpaceStateReduction);
	 	spaceStateReductionGroup.add(conservativeSpaceStateReduction);
	 	spaceStateReductionGroup.add(aggressiveSpaceStateReduction);
	 	spaceStateReductionMenu.add(noSpaceStateReduction);
	 	spaceStateReductionMenu.add(conservativeSpaceStateReduction);
	 	spaceStateReductionMenu.add(aggressiveSpaceStateReduction);
	 		
	 	modelCheckingOptionsMenu.add(spaceStateReductionMenu);
	 	
		// Setting the temporal logical operators using JComboBox		
		String[] items = {MIGHT_EVENTUALLY, MUST_EVENTUALLY, MIGHT_ALWAYS, MUST_ALWAYS, LEADS_TO};
		howToList = new JComboBox<String>(items);
		helpTextArea = new JTextArea();
		helpTextArea.setFont(new Font("Serif", Font.PLAIN, 16));
		helpTextArea.setText(E_SOME_TEXT);
		helpTextArea.setEditable(false);
		helpTextArea.setLineWrap(true);
		helpTextArea.setWrapStyleWord(true);
		howToList.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				switch ((String) howToList.getSelectedItem()) {
					case MUST_ALWAYS:					
						helpTextArea.setText(A_ALL_TEXT);
						exampleTextArea.setText(A_ALL_EXAMPLE);
						setPrimeTextName();
						optionalPanel.setVisible(false);
						setFrameSizeSmaller();
						break;
					case MUST_EVENTUALLY:
						helpTextArea.setText(A_SOME_TEXT);
						exampleTextArea.setText(A_SOME_EXAMPLE);
						setPrimeTextName();
						optionalPanel.setVisible(false);
						setFrameSizeSmaller();
						break;
					case MIGHT_EVENTUALLY:
						helpTextArea.setText(E_SOME_TEXT);
						exampleTextArea.setText(E_SOME_EXAMPLE);
						setPrimeTextName();
						optionalPanel.setVisible(false);
						setFrameSizeSmaller();
						break;
					case MIGHT_ALWAYS:
						helpTextArea.setText(E_ALL_TEXT);
						exampleTextArea.setText(E_ALL_EXAMPLE);
						setPrimeTextName();
						optionalPanel.setVisible(false);
						setFrameSizeSmaller();
						break;
					case LEADS_TO:
						helpTextArea.setText(IMPLICATION);
						exampleTextArea.setText(IMPLICATION_EXAMPLE);
						setPrimeAndOptionalTextName();
						optionalPanel.setVisible(true);
						setFrameSizeBigger();
						break;				
				}
			}
		});	
		
		// Example part
		final JLabel exampleLabel = new JLabel("Example:");
		exampleLabel.setFont(new Font("Serif", Font.PLAIN, 16));
		exampleLabel.setPreferredSize(new Dimension(250, 35));
		exampleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		exampleTextArea = new JTextArea();
		exampleTextArea.setFont(new Font("Serif", Font.PLAIN, 16));
		exampleTextArea.setText(E_SOME_EXAMPLE);
		exampleTextArea.setEditable(false);
		exampleTextArea.setLineWrap(true);
		exampleTextArea.setWrapStyleWord(true);
		JPanel examplePanel = new JPanel();
		BoxLayout examplePanelLayout = new BoxLayout(examplePanel, BoxLayout.Y_AXIS);
		examplePanel.setLayout(examplePanelLayout);
		examplePanel.add(exampleLabel);
		examplePanel.add(exampleTextArea);
		examplePanel.setBorder(new EmptyBorder(3, 0, 0, 0));
		
		JLabel selectorLabel = new JLabel("Select an element below to insert into the condition.");
		selectorLabel.setFont(new Font("Serif", Font.PLAIN, 16));
		selectorLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		stateSelector.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				activeText.setText(activeText.getText() + "(" + stateSelector.getSelectedItem().toString() + ")");				
			}
		});
		
		variableSelector.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				activeText.setText(activeText.getText() + "(" + variableSelector.getSelectedItem().toString() + ")");				
			}
		});
		
		eventSelector.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				activeText.setText(activeText.getText() + "(" + eventSelector.getSelectedItem().toString() + ")");				
			}
		});
		
		operatorSelector.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				switch (operatorSelector.getSelectedItem().toString()) {
				case "AND":
					activeText.setText(activeText.getText() + " && ");
					break;
				case "OR":
					activeText.setText(activeText.getText() + " || ");
					break;
				case "IMPLICATION":
					activeText.setText(activeText.getText() + " imply ");
					break;
				case "NEGATION":
					activeText.setText(activeText.getText() + "!");
					break;
				case "EQUALITY":
					activeText.setText(activeText.getText() + " == ");
					break;
				default:
					break;
				}
			}
		});
		
		// Setting the selector JComboBoxes
		final Dimension LABEL_PREFERRED_SIZE = new Dimension(115, 30);
		JLabel stateLabel = new JLabel("State selector:");
		stateLabel.setPreferredSize(LABEL_PREFERRED_SIZE);
		stateLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		
		JLabel variableLabel = new JLabel("Variable selector:");
		variableLabel.setPreferredSize(LABEL_PREFERRED_SIZE);
		variableLabel.setAlignmentX(LEFT_ALIGNMENT);
		
		JLabel eventLabel = new JLabel("Event selector:");
		eventLabel.setPreferredSize(LABEL_PREFERRED_SIZE);
		eventLabel.setAlignmentX(LEFT_ALIGNMENT);
		
		JLabel operatorLabel = new JLabel("Operator selector:");
		operatorLabel.setPreferredSize(LABEL_PREFERRED_SIZE);
		operatorLabel.setAlignmentX(LEFT_ALIGNMENT);
		
		// Setting the textfields where the queries are constructed
		primeText = new JTextField();
		activeText = primeText;
		primeText.addFocusListener(new FocusListener() {			
			@Override
			public void focusLost(FocusEvent e) {}			
			@Override
			public void focusGained(FocusEvent e) {
				activeText = primeText;
			}
		});
		optionalText = new JTextField();
		optionalText.addFocusListener(new FocusListener() {			
			@Override
			public void focusLost(FocusEvent e) {}			
			@Override
			public void focusGained(FocusEvent e) {
				activeText = optionalText;
			}
		});
		resultText = new JTextField();
		resultText.setEditable(false);
		
		// Log if ever needed
		logTextArea = new JTextArea();
		logTextArea.setFont(new Font("Times New Roman", Font.ITALIC, 15));
		logTextArea.setText("");
		logTextArea.setEditable(false);
		logTextArea.setColumns(4);
		// The textarea is put into a scroll pane
		JScrollPane textAreaScrollPane = new JScrollPane(logTextArea);
		textAreaScrollPane.setSize(200, 150);
		
		// Label for "Condition holds." and "Condition does not hold!"
		verificationResultLabel = new JLabel();
		verificationResultLabel.setFont(new Font("Serif", Font.PLAIN, 17));
		verificationResultLabel.setBorder(new EmptyBorder(0, 15, 0, 15));
		
		// Verify and Reset buttons
		verifyButton = new JButton("Verify");		
		verifyButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					setVerificationLabel();
					resultText.setText(howToList.getSelectedItem().toString());
					String uppaalQuery = parseText();
					// Starting the verification
					controller.verify(uppaalQuery);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		resetButton = new JButton("Reset");		
		resetButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {			
				boolean isCancelled = controller.cancelVerification();
				if (isCancelled) {
					setVerificationLabelToCancelled();
					// We do not want to empty the query labels
					return;
				}
				primeText.setText("");
				optionalText.setText("");
				resultText.setText("");
				verificationResultLabel.setText("");
			}
		});
		
		// Generate Test Set button
		
		generateTestSetButton = new JButton("Generate Test Set");		
		generateTestSetButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {	
				try {
					setVerificationLabel();
					setVerificationButtons(false);
					controller.executeGeneratedQueries();
				} catch (Exception ex) {
					handleVerificationExceptions(ex);
				}
			}
		});
		
		
		// Setting the layout
		
		// Log textarea is down
		JPanel downPanel = new JPanel(new BorderLayout());
		downPanel.add(logTextArea, BorderLayout.CENTER);		
		
		// Upper panel consists of two panels
		JPanel upPanel = new JPanel();
		BoxLayout upPanelLayout = new BoxLayout(upPanel, BoxLayout.X_AXIS);
		upPanel.setLayout(upPanelLayout);
		// UpLeft and UpRight
		JPanel upLeftPanel = new JPanel();
		JPanel upRightPanel = new JPanel();
		BoxLayout upRightPanelLayout = new BoxLayout(upRightPanel, BoxLayout.Y_AXIS);
		upRightPanel.setLayout(upRightPanelLayout);
		// Putting upLeft and upRight into the upper panel
		upPanel.add(upLeftPanel);
		upPanel.add(upRightPanel);
		
		// Selector label
		JPanel selectorLabelPanel = new JPanel();
		selectorLabelPanel.add(selectorLabel);
		selectorLabelPanel.setMinimumSize(UP_RIGHT_PANEL_SIZE);
		// State selector
		JPanel statePanel = new JPanel();
		BoxLayout statePanelLayout = new BoxLayout(statePanel, BoxLayout.X_AXIS);
		statePanel.setLayout(statePanelLayout);
		statePanel.add(stateLabel);
		statePanel.add(stateSelector);
		statePanel.setMinimumSize(UP_RIGHT_PANEL_SIZE);
		// Variable selector
		JPanel variablePanel = new JPanel();
		BoxLayout variablePanelLayout = new BoxLayout(variablePanel, BoxLayout.X_AXIS);
		variablePanel.setLayout(variablePanelLayout);
		variablePanel.add(variableLabel);
		variablePanel.add(variableSelector);
		variablePanel.setMinimumSize(UP_RIGHT_PANEL_SIZE);
		// Event selector
		JPanel eventPanel = new JPanel();
		BoxLayout eventPanelLayout = new BoxLayout(eventPanel, BoxLayout.X_AXIS);
		eventPanel.setLayout(eventPanelLayout);
		eventPanel.add(eventLabel);
		eventPanel.add(eventSelector);
		eventPanel.setMinimumSize(UP_RIGHT_PANEL_SIZE);
		// Operator selector
		JPanel operatorPanel = new JPanel();
		BoxLayout operatorPanelLayour = new BoxLayout(operatorPanel, BoxLayout.X_AXIS);
		operatorPanel.setLayout(operatorPanelLayour);
		operatorPanel.add(operatorLabel);
		operatorPanel.add(operatorSelector);
		operatorPanel.setMinimumSize(UP_RIGHT_PANEL_SIZE);
		
		// Button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(2000, 37));
		buttonPanel.setMaximumSize(new Dimension(2000, 37));
		buttonPanel.add(verifyButton);
		buttonPanel.add(resetButton);
		buttonPanel.add(generateTestSetButton);
		buttonPanel.add(verificationResultLabel);
		
		// Selectors into the upRight panel
		upRightPanel.add(selectorLabelPanel);
		upRightPanel.add(statePanel);
		upRightPanel.add(variablePanel);
		upRightPanel.add(eventPanel);
		upRightPanel.add(operatorPanel);
		upRightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// Fillings UpLeft panel
		BoxLayout upLeftPanelLayout = new BoxLayout(upLeftPanel, BoxLayout.Y_AXIS);
		upLeftPanel.setLayout(upLeftPanelLayout);
		JLabel queryModeLabel = new JLabel("Select the query mode:");
		queryModeLabel.setFont(new Font("Serif", Font.PLAIN, 16));
		queryModeLabel.setPreferredSize(new Dimension(250, 35));
		queryModeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		upLeftPanel.add(queryModeLabel);
		upLeftPanel.add(howToList);
		upLeftPanel.add(helpTextArea);
		upLeftPanel.add(examplePanel);
		upLeftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// Setting the central panel
		JPanel centralPanel = new JPanel();
		BoxLayout centralPanelLayout = new BoxLayout(centralPanel, BoxLayout.Y_AXIS);
		centralPanel.setLayout(centralPanelLayout);		
		// Textfields and the buttons
		final Dimension LABEL_SIZE = new Dimension(95, 20);
		primeLabel = new JLabel("Condition:");
		primeLabel.setPreferredSize(LABEL_SIZE);
		JPanel primePanel = new JPanel();
		BoxLayout primePanelLayout = new BoxLayout(primePanel, BoxLayout.X_AXIS);
		// First textfield
		primePanel.setLayout(primePanelLayout);
		primePanel.add(primeLabel);
		primePanel.add(primeText);
		primePanel.setPreferredSize(MAX_PANEL_SIZE);
		primePanel.setMaximumSize(MAX_PANEL_SIZE);
		// Second textfield (if needed: -->)
		optionalLabel = new JLabel("Condition B:");
		optionalLabel.setPreferredSize(LABEL_SIZE);
		optionalPanel = new JPanel();
		optionalPanel.setVisible(false);
		BoxLayout optionalPanelLayout = new BoxLayout(optionalPanel, BoxLayout.X_AXIS);
		optionalPanel.setLayout(optionalPanelLayout);
		optionalPanel.add(optionalLabel);
		optionalPanel.add(optionalText);
		optionalPanel.setPreferredSize(MAX_PANEL_SIZE);
		optionalPanel.setMaximumSize(MAX_PANEL_SIZE);
		// The third textfield (after the buttons)
		resultLabel = new JLabel("Query:");
		resultLabel.setPreferredSize(LABEL_SIZE);
		JPanel resultPanel = new JPanel();
		BoxLayout resultPanelLayout = new BoxLayout(resultPanel, BoxLayout.X_AXIS);
		resultPanel.setLayout(resultPanelLayout);
		resultPanel.add(resultLabel);
		resultPanel.add(resultText);
		resultPanel.setPreferredSize(MAX_PANEL_SIZE);
		resultPanel.setMaximumSize(MAX_PANEL_SIZE);
		// Filling the central panel
		centralPanel.add(primePanel);
		centralPanel.add(optionalPanel);
		centralPanel.add(buttonPanel);
		centralPanel.add(resultPanel);
		centralPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// Putting the panels into the frame	
		add(upPanel, BorderLayout.NORTH);
		add(centralPanel, BorderLayout.CENTER);
		add(logTextArea, BorderLayout.SOUTH);
		
		// Initializing the view
		setResizable(true);
		setLocation(550, 300);
		initStatesComboBox();
		initVariablesComboBox();
		initEventsComboBox();
		primeText.setText("");
		optionalText.setText("");
		resultText.setText("");
		
		// Positioning frame on the center
		setLocationRelativeTo(null);
	}
	
	public AbstractController getController() {
		return controller;
	}
	
	private void setVerificationLabel() {
		logTextArea.setText(""); 
		setVerificationLabel("Verifying...");
	}
	
	public void setVerificationLabel(String string) {
		verificationResultLabel.setText(string);
		verificationResultLabel.setForeground(Color.black);
	}
	
	public void setVerificationButtons(boolean isEnabled) {
		if (isEnabled) {
			resetButton.setText("Reset");
		}
		else {
			resetButton.setText("Stop");
		}
		verifyButton.setEnabled(isEnabled);
		generateTestSetButton.setEnabled(isEnabled);
	}
	
	public String getSelectedSearchOrder() {
		if (breadthFirst.isSelected()) {
			return "Breadth First";
		}
		if (depthFirst.isSelected()) {
			return "Depth First";
		}
		if (randomDepthFirst.isSelected()) {
			return "Random Depth First";
		}
		if (optimalFirst.isSelected()) {
			return "Optimal First";
		}
		if (randomOptimalDepthFirst.isSelected()) {
			return "Random Optimal Depth First";
		}
		// BFS is default
		return "Breadth First";
	}
	
	public String getStateSpaceRepresentation() {
		if (overApproximation.isSelected()) {
			return "Over Approximation";
		}
		if (underApproximation.isSelected()) {
			return "Under Approximation";
		}
		// DBM is default
		return "DBM";
	}
	
	public String getSelectedTrace() {
		if (someTrace.isSelected()) {
			return "Some";
		}
		if (shortestTrace.isSelected()) {
			return "Shortest";
		}
		if (fastestTrace.isSelected()) {
			return "Fastest";
		}
		// Shortest is default
		return "Shortest";
	}
	
	public boolean isReuseStateSpace() {
		return reuseStateSpaceItem.isSelected();
	}
	
	public boolean isOptimizeTestSet() {
		return optimizeTestSetItem.isSelected();
	}
	
	public int getHashTableSize() {
		if (size64M.isSelected()) {
			return 64;
		}
		if (size256M.isSelected()) {
			return 256;
		}
		if (size1024M.isSelected()) {
			return 1024;
		}
		// 512 MB is default
		return 512;
	}
	
	public int getTestGenerationTmeout() {
		if (sec30.isSelected()) {
			return 30;
		}
		if (sec60.isSelected()) {
			return 60;
		}
		if (sec90.isSelected()) {
			return 90;
		}
		if (sec180.isSelected()) {
			return 180;
		}
		if (sec360.isSelected()) {
			return 360;
		}
		// 15 sec is default
		return 15;
	}
	
	public String getStateSpaceReduction() {
		if (noSpaceStateReduction.isSelected()) {
			return "None";
		}
		if (aggressiveSpaceStateReduction.isSelected()) {
			return "Aggressive";
		}
		// Conservative is default
		return "Conservative";
	}

	private void setFrameSizeSmaller() {
		setSize(1095, 385);
	}
	
	private void setFrameSizeBigger() {
		setSize(1095, 425);
	}
	
	private void initStatesComboBox() {
		controller.initSelectorWithStates(stateSelector);
	}
	
	private void initVariablesComboBox() {
		controller.initSelectorWithVariables(variableSelector);
	}
	
	private void initEventsComboBox() {
		controller.initSelectorWithEvents(eventSelector);
	}
	
	private String parseText() {
		String result = null;
		if (howToList.getSelectedItem().equals(LEADS_TO)) {
			result = controller.parseLeadsToQuery(primeText.getText(), optionalText.getText());
		}
		else {
			String operator = howToList.getSelectedItem().toString();
			result = controller.parseRegularQuery(primeText.getText(), operator);
		}
		resultText.setText(result);
		return result;
	}

	private void setPrimeTextName() {
		primeLabel.setText("Condition:");
	}
	
	private void setPrimeAndOptionalTextName() {
		primeLabel.setText("Condition A:");
		optionalLabel.setText("Condition B:");		
	}
	
	public void handleVerificationExceptions(Exception ex) {
		// Logging the exception
		if (ex == null || ex.getMessage() == null) {
			// This sometimes happened
			ex.printStackTrace();
			Logger.getLogger("GammaLogger").log(Level.ERROR, "Null pointer: " +  ex);
		}
		else if (ex.getMessage().contains("[error]")) {
			logTextArea.setText("The condition is not a valid expression!");
			Logger.getLogger("GammaLogger").log(Level.ERROR, "Execution terminated: " +  ex.getMessage());
		}
		else if (ex.getMessage().contains("[warning]")) {
			verificationResultLabel.setText("");
			Logger.getLogger("GammaLogger").log(Level.WARN, "Execution terminated: " + ex.getMessage());
		}
		else {
			logTextArea.setText(ex.getMessage());
		}
		verificationResultLabel.setText("");
	}	
	
	protected void setVerificationLabel(boolean hasTrace) {
		switch (howToList.getSelectedItem().toString()) {
			case MIGHT_EVENTUALLY:
			case MIGHT_ALWAYS:
				if (hasTrace) {
					setVerificationLabelToTrue();
				}
				else {
					setVerificationLabelToFalse();
				}
				break;
			case MUST_ALWAYS:
			case MUST_EVENTUALLY:
			case LEADS_TO:
				if (!hasTrace) {
					setVerificationLabelToTrue();
				}
				else {
					setVerificationLabelToFalse();
				}
				break;
		}
	}
	
	protected void setVerificationLabelToCancelled() {
		verificationResultLabel.setText("Verification cancelled.");
		verificationResultLabel.setForeground(Color.black);
	}

	public void setVerificationLabelToFalse() {
		verificationResultLabel.setText("The condition does not hold!");
		verificationResultLabel.setForeground(Color.red);
	}

	public void setVerificationLabelToTrue() {
		verificationResultLabel.setText("The condition holds.");
		verificationResultLabel.setForeground(Color.green);
	}
	
}
