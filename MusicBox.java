import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import javax.sound.sampled.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;


public class MusicBox extends JFrame implements Runnable, MouseListener, ActionListener, AdjustmentListener
{
	JPanel boardPanel;
	JToggleButton[][] musicBoard;
	String instrumentNames[];
	String clipNames[];
	Clip clip[];
	JScrollPane scrollPane;

	int col = 0;
	Thread timing;
	int time;
	int dimR = 37;
	int dimC = 100;

	JMenuBar menuBar;
	JPanel buttonPanel, scrollPanel, labelPanel, totalPanel, totalPanel2;
	JButton playStop, clear;
	boolean playing = false;
	JMenu instrumentMenu, fileMenu;
	JMenuItem bell, piano, glockenspiel, marimba, oboe, ohah, save, load;
	JScrollBar tempoBar;
	JLabel tempoLabel;
	JFileChooser fileChooser;
	String instrument;
	int tempo = 200;


	public MusicBox()
	{
		this.setSize(1000, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createBoard(dimR, dimC);
		this.setVisible(true);
		scrollPane = new JScrollPane(boardPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.add(scrollPane, BorderLayout.CENTER);
		timing = new Thread(this);
		timing.start();
		loadNotes(instrument);



		//Day 2
		menuBar = new JMenuBar();
		instrumentMenu = new JMenu("Instruments");
		fileMenu = new JMenu("File");

		//Buttons
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		playStop = new JButton();
		clear = new JButton();
		playStop.setText("PLAY");
		clear.setText("CLEAR");
		playStop.addActionListener(this);
		clear.addActionListener(this);
		buttonPanel.add(playStop);
		buttonPanel.add(clear);

		//Instrument Menu & File Menu
		bell = new JMenuItem("Bell");
		bell.putClientProperty("Instrument", "Bell");
		bell.addActionListener(this);
		piano = new JMenuItem("Piano");
		piano.putClientProperty("Instrument", "Piano");
		piano.addActionListener(this);
		glockenspiel = new JMenuItem("Glockenspiel");
		glockenspiel.putClientProperty("Instrument", "Glockenspiel");
		glockenspiel.addActionListener(this);
		marimba = new JMenuItem("Marimba");
		marimba.putClientProperty("Instrument", "Marimba");
		marimba.addActionListener(this);
		oboe = new JMenuItem("Oboe");
		oboe.putClientProperty("Instrument", "Oboe");
		oboe.addActionListener(this);
		ohah = new JMenuItem("Oh_Ah");
		ohah.putClientProperty("Instrument", "Oh_Ah");
		ohah.addActionListener(this);


		menuBar.add(fileMenu);

		instrumentMenu.add(bell);
		instrumentMenu.add(piano);
		instrumentMenu.add(glockenspiel);
		instrumentMenu.add(marimba);
		instrumentMenu.add(oboe);
		instrumentMenu.add(ohah);
		menuBar.add(instrumentMenu);

		save = new JMenuItem("Save");
		save.addActionListener(this);
		load = new JMenuItem("Load");
		load.addActionListener(this);
		fileMenu.add(save);
		fileMenu.add(load);


		//Adding widgets to top bar
		totalPanel2 = new JPanel();
		GridLayout grid2 = new GridLayout(1, 4);
		totalPanel2.setLayout(grid2);
		totalPanel2.add(menuBar);
		totalPanel2.add(buttonPanel);


		//Tempo Bar and Label
		tempoBar = new JScrollBar(JScrollBar.HORIZONTAL, tempo, 0, 50, 350);
		tempo = tempoBar.getValue();
		tempoBar.addAdjustmentListener(this);

		GridLayout grid = new GridLayout(1, 1);
		tempoLabel = new JLabel("Tempo: "+tempo);
		labelPanel = new JPanel();
		labelPanel.setLayout(grid);
		labelPanel.add(tempoLabel);

		scrollPanel = new JPanel();
		scrollPanel.setLayout(grid);
		scrollPanel.add(tempoBar);


		//Adding Everything to JFrame
		totalPanel = new JPanel();
		totalPanel.setLayout(new BorderLayout());
		totalPanel.add(labelPanel, BorderLayout.WEST);
		totalPanel.add(scrollPanel, BorderLayout.CENTER);
		this.add(totalPanel, BorderLayout.SOUTH);
		this.add(totalPanel2, BorderLayout.NORTH);
		this.setVisible(true);

		String currentDir = System.getProperty("user.dir");
		fileChooser = new JFileChooser(currentDir);
	}

	public void createBoard(int row, int col)
	{
		musicBoard = new JToggleButton[row][col];
		boardPanel = new JPanel();
		clipNames = new String [] {"C4", "B4", "AS4", "A4", "GS3", "G3", "FS3", "F3", "E3", "DS3", "D3", "CS3", "C3", "B3", "AS3", "A3", "GS2", "G2", "FS2", "F2", "E2", "DS2", "D2", "CS2", "C2", "B2", "AS2", "A2", "GS1", "G1", "FS1", "F1", "E1", "DS1", "D1", "CS1", "C1"};
		instrumentNames = new String[] {"Bell", "Piano", "Glockenspiel", "Marimba", "Oboe", "Oh_Ah"};
		clip = new Clip[clipNames.length];
		boardPanel.setLayout(new GridLayout(row, col));

		for(int r = 0; r<musicBoard.length; r++)
		{
			String text = clipNames[r].replaceAll("S", "#");
			for(int c = 0; c<musicBoard[r].length; c++)
			{
				musicBoard[r][c] = new JToggleButton();
				musicBoard[r][c].addMouseListener(this);
				musicBoard[r][c].setText(text);
				musicBoard[r][c].setPreferredSize(new Dimension(30, 30));
				musicBoard[r][c].setMargin(new Insets(0, 0, 0, 0));
				boardPanel.add(musicBoard[r][c]);
			}
		}
		this.add(boardPanel);
		this.setSize(musicBoard[0].length*35, musicBoard.length*35);
		this.revalidate();
	}
	public void loadNotes(String music)
	{
		String initInstrument = music;
		try{
				for(int x=0;x<clipNames.length;x++)
				{
					URL url = new File(initInstrument+"/"+initInstrument+" - "+clipNames[x]+".wav").toURI().toURL();
					AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
					clip[x] = AudioSystem.getClip();
					clip[x].open(audioIn);
				}
		}catch (UnsupportedAudioFileException|IOException|LineUnavailableException e) {}
	}
	public void run()
	{
		boolean found = true;
		while(true)
		{
			try{
				if(!playing)
				{
					timing.sleep(0);
				}
				else
				{
					for(int r=0; r<musicBoard.length; r++)
					{
						if(musicBoard[r][col].isSelected())
						{
							clip[r].start();
						}
					}
					timing.sleep(150);
					for(int r=0; r<musicBoard.length; r++)
					{
						if(musicBoard[r][col].isSelected())
						{
							clip[r].stop();
							clip[r].setFramePosition(0);
						}
					}
					if(col == musicBoard[0].length-1)
					{
						col = 0;
					}
					col++;

				}


			}catch(InterruptedException e)
			{

			}
		}
	}
	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		if(e.getSource() == tempoBar)
		{
			tempo = tempoBar.getValue();
			tempoLabel.setText("Tempo: "+tempo);

		}
	}
	public void mouseClicked(MouseEvent e)
	{

	}
	public void mousePressed(MouseEvent e)
	{

	}
	public void mouseReleased(MouseEvent e)
	{

	}
	public void mouseEntered(MouseEvent e)
	{

	}
	public void mouseExited(MouseEvent e)
	{

	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == playStop)
		{
			if(playing)
			{
				playing = false;
				playStop.setText("PLAY");
			}
			else
			{
				playing = true;
				playStop.setText("PAUSE");
			}
		}//playStop
		else if(e.getSource() == clear)
		{
			for(int r = 0; r<musicBoard.length; r++)
			{
				for(int c=0; c<musicBoard[0].length; c++)
				{
					if(musicBoard[r][c].isSelected())
					{
						musicBoard[r][c].setSelected(false);
					}
				}
			}
			reset();
		}//clear
		else if(e.getSource() == save)
		{
			reset();
			saveSong();
		}
		else if(e.getSource() == load)
		{
			reset();
			loadFile();
		}
		else{
			try{
				loadNotes((String)((JMenuItem)e.getSource()).getClientProperty("Instrument"));
				instrumentMenu.setText((String)((JMenuItem)e.getSource()).getClientProperty("Instrument"));
				reset();

			}catch(NullPointerException ev)
			{

			}
		}
	}
	public void reset()
	{
		col = 0;
		playing = false;
		playStop.setText("PLAY");

	}

	public void loadFile()
	{
		if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			try{
				File loadFile = fileChooser.getSelectedFile();
				BufferedReader input = new BufferedReader(new FileReader(loadFile));
				String temp = input.readLine();
				String arr[] = temp.split(" ");
				tempo = Integer.parseInt(arr[0]);
				tempoBar.setValue(tempo);
				int columns = Integer.parseInt(arr[1]);
				Character[][] song = new Character[37][columns];
				int rowV = 0;
				while((temp = input.readLine())!=null)
				{
					for(int i = 2; i<columns+2; i++)
					{
						song[rowV][i-2] = temp.charAt(i);

					}
					rowV++;

				}
				setNotes(song);
				input.close();
			}catch(Exception e)
			{

			}
			loadNotes(instrument);
		}
	}
	public void setNotes(Character[][] characters)
	{
		scrollPane.remove(boardPanel);
		boardPanel = new JPanel();
		musicBoard = new JToggleButton[dimR][dimC];
		boardPanel.setLayout(new GridLayout(dimR, dimC));
		for(int r = 0; r<musicBoard.length; r++)
		{
			String text = clipNames[r].replaceAll("S", "#");
			for(int c = 0; c<musicBoard[r].length; c++)
			{
				musicBoard[r][c] = new JToggleButton();
				musicBoard[r][c].addMouseListener(this);
				musicBoard[r][c].setText(text);
				musicBoard[r][c].setPreferredSize(new Dimension(30, 30));
				musicBoard[r][c].setMargin(new Insets(0, 0, 0, 0));
				boardPanel.add(musicBoard[r][c]);
			}
		}
		this.remove(scrollPane);
		scrollPane = new JScrollPane(boardPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.add(scrollPane, BorderLayout.CENTER);
		for(int i=0; i<characters.length; i++)
		{
			for(int j=0; j<characters[0].length; j++)
			{
				if(characters[i][j] == 'x')
				{
					musicBoard[i][j].setSelected(true);
				}
			}
		}
		this.revalidate();
	}
	public void saveSong()
	{
		FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", "txt");
		fileChooser.setFileFilter(filter);
		if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			File newFile = fileChooser.getSelectedFile();
			try{
						String text = newFile.getAbsolutePath();
						if(text.contains(".txt"))
						{
							text = text.substring(0, text.length()-3);
						}
						String currSong = "";
						String[] noteNames = new String[] {"  ", "c ", "b ", "a-", "a ", "g-", "g ", "f-", "f ", "e ", "d-", "d ", "c-","c ", "b ", "a-", "a ", "g-", "g ",
							"f-", "f ", "e ", "d-", "d ", "c-", "c ", "b ", "a-", "a ", "g-", "g ", "f-", "f ", "e ", "d-", "d ", "c-", "c "};
						for(int i = 0; i<musicBoard.length; i++)
						{
							if(i == 0)
							{
								currSong = ""+tempo+" "+musicBoard[0].length+"\n";
							}
							currSong = currSong+noteNames[i];
							for(int j=0; j<musicBoard[0].length; j++)
							{
								if(musicBoard[i][j].isSelected())
								{
									currSong = currSong+"x";
								}
								else
								{
									currSong = currSong+"-";
								}
							}//column loop
							currSong = currSong+="\n";
							BufferedWriter outputStream = new BufferedWriter(new FileWriter(text));
							outputStream.write(currSong);
							outputStream.close();

						}//row loop
					}catch(Exception e)
					{

					}
		}
	}


	public static void main(String[]args)
	{
		MusicBox app = new MusicBox();
	}
}
