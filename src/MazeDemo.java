import javax.swing.*;

import sun.audio.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;


/*****************************************************************
 * Maze Demo Class
 * @author Joseph Bourque & Connor Peters
 * This class is used to generate a maze from a text file and to
 * provide basic interaction and instructions to the user
 *****************************************************************/
public class MazeDemo extends JFrame
{
	private static final long serialVersionUID = 1L; // Serial ID for the class, must include this since we're extending Jframe
	
	/////////////////////////////////////////////////////////////////////
	//// Player Variables                                            ////
	/////////////////////////////////////////////////////////////////////
	private int playerX; // Holds the X coordinate for the player
	private int playerY; // Holds the Y coordinate for the player
	private char frameNo; // Used to handle animation on player sprite
	
	/////////////////////////////////////////////////////////////////////
	//// Stage Variables                                             ////
	/////////////////////////////////////////////////////////////////////
	private int stageW; // Holds the number of tiles the stage is wide
	private int stageH; // Holds the number of tiles the stage is tall
	private char[][] stModel; // Array that serves as the stage model, holds the maze read in from the text file
	private JPanel stView; // Panel that serves as the view for the program (Holds the graphic components for each stage)
	private final int EXIT_REACHED = Integer.MAX_VALUE; // Value for displaying level win screen
	
	/////////////////////////////////////////////////////////////////////
	//// Misc Variables                                             ////
	/////////////////////////////////////////////////////////////////////
	private Scanner readMaze; // Scanner object used to read in the maze layout from the text file
	private int currentScreen; // used to switch between screens/levels
	private KeyboardFocusManager manager; // Used to handle keyboard events from the user

	/*****************************************************************
	 * Default Constructor
	 *-----------------------------------------------------------------
	 * Jumper function, responsible for starting the program, displaying
	 * instructions to the user, and reading in the first level from
	 * a map file.
	 *****************************************************************/
	public MazeDemo()
	{
		// Set up event listener for keyboard, start listening for keyboard input
		// from user
		manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new MyDispatcher());
		
		// Dialog screen has not been displayed yet so this is set to false
		currentScreen = 0;
		
		// Try to read in the maze layout file, if it is not present, set
		// default values
		try
		{
			// Point Scanner object to current maze layout file
			readMaze = new Scanner(new File("maze.txt"));
			
			// Start the background music
			playMusic("audio\\club_viridia.wav"); 
			
			// Set the height and width of the map based on the dimensions read in from the
			// maze layout file
			getMapDimensions();

			// Initalize the stage model and set up the model using the scanner file
			stModel = new char[stageW][stageH];
			createMap();

			// Prep the program view using the model (get the program ready to generate graphics)
			stView = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			frameNo = '1';
			locatePlayer();
			
			// Main window setup
			this.setTitle("Raichu's Battery Rush");
			this.setSize(32*stageW+6, 32*stageH+40);
			this.setLocationRelativeTo(null);
			this.setDefaultCloseOperation(EXIT_ON_CLOSE);
			this.setResizable(false);
			
			// Call the stage update function, this function used any time the view
			// needs to be updated
			updateStage(currentScreen);
			
		}
		catch(FileNotFoundException e)
		{
			// If file was not read correctly, initalize default panel values
			this.setTitle("Raichu's Battery Rush");
			this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 100));
			this.setSize(300, 300);
			this.setLocationRelativeTo(null);
			this.setDefaultCloseOperation(EXIT_ON_CLOSE);
			this.setResizable(false);
			this.setVisible(true);
			
			// Add message to panel to let the user know what went wrong
			this.add(new JLabel("maze.txt was corrupted or missing"));
		}
	}

	
	
	/*****************************************************************
	 * Get Map Dimensions Function
	 * ---------------------------------------------------------------
	 * Function used to obtain the dimensions of the maze from the 
	 * mapping file. First line in map file should be the width
	 * second line should be the height.
	 *****************************************************************/
	private void getMapDimensions()
	{
		stageW = Integer.parseInt(readMaze.nextLine());
		stageH = Integer.parseInt(readMaze.nextLine());
	}

	
	
	/*****************************************************************
	 * Create Map Function
	 * ---------------------------------------------------------------
	 * Function used generate the program model from the mapping file
	 * reads in 1 line at a time, converts the line to a character 
	 * array, and stores the array in the program model.
	 *****************************************************************/
	private void createMap()
	{
		int counter = 0;
		while(readMaze.hasNext())
		{
			stModel[counter] = readMaze.nextLine().toCharArray();
			counter++;
		}
	}
	
	/*****************************************************************
	 * Update Stage Function
	 * ---------------------------------------------------------------
	 * Function used to update the Program's View i.e. update the 
	 * program's graphics.
	 * @param uc - stands for update case; used to discern which
	 *        graphic to switch to
	 *****************************************************************/
	private void updateStage(int uc)
	{
		// Create a new empty panel (Clear out graphics from previous run)
		stView = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		switch(uc)
		{
			// Display Dialog Screen
			case 0:
			{
				stView.add(new JLabel(new ImageIcon("artwork\\dialog.jpg")));
				break;
			}
			
			// Display Level 1
			case 1:
			{
				for(int i = 0; i < stageH; i++)
					for(int j = 0; j < stageW; j++)
						stView.add(addTile(stModel[i][j]));
				break;
			}
			
			// Display Victory Screen
			case EXIT_REACHED:
			{
				stView.add(new JLabel(new ImageIcon("artwork\\forest_win.jpg")));
				break;
			}
		}
		
		// Update the graphics on the main window; you must have this in order for
		// the graphics to change
		this.setContentPane(stView);
		this.revalidate();
		this.repaint();	
	}

	/*****************************************************************
	 * Update Player Function
	 * ---------------------------------------------------------------
	 * @param newX - New X position for player
	 * @param newY - New Y position for player
	 * Function used to update the program model and then calls the
	 * update function for the view
	 *****************************************************************/
	public void updatePlayer(int newX, int newY)
	{
		if(newX >= stModel[0].length || newY >= stModel.length)
			throw new ArrayIndexOutOfBoundsException("Specified position off stage!");
		if(newX <= 0 || newY <= 0)
			throw new ArrayIndexOutOfBoundsException("Specified position off stage!");
		
		// Move the player's position in the array
		stModel[newY][newX] = frameNo;
		
		// Set the player's old position as empty
		stModel[playerY][playerX] = '-';
		
		// Update player's coords with the new coords
		playerX = newX;
		playerY = newY;
		
		// Update the view
		updateStage(currentScreen);

	}

	/*****************************************************************
	 * Locate Player Function
	 * ---------------------------------------------------------------
	 * Used to locate the player on the map and update the player's 
	 * coordinates. Note: since function is O(N^2) it is not 
	 * recommended to use this function past the inital setup
	 ****************************************************************/
	public void locatePlayer()
	{
		playerX = locateX();
		playerY = locateY();
	}

	/*****************************************************************
	 * Getter/Setter Functions for player's coordinates
	 * @return return's player's x/y values, respectively
	 *****************************************************************/
	public int getPlayerX()	{ return playerX; }
	public int getPlayerY()	{ return playerY; }

	/*****************************************************************
	 * Locate X Function
	 * --------------------------------------------------------------
	 * @return return's player's x coordinate in the program model
	 * Helper function to be used with the locatePlayer() function
	 *****************************************************************/
	private int locateX()
	{
		for(int i = 0; i < stModel.length; i++)
		{
			for(int j = 0; j < stModel[0].length; j++)
				if(stModel[i][j]==frameNo)
					return j;
		}
		return -1;
	}

	/*****************************************************************
	 * Locate Y Function
	 * --------------------------------------------------------------
	 * @return return's player's y coordinate in the program model
	 * Helper function to be used with the locatePlayer() function
	 *****************************************************************/
	private int locateY()
	{
		for(int i = 0; i < stModel.length; i++)
		{
			for(int j = 0; j < stModel[0].length; j++)
				if(stModel[i][j]==frameNo)
					return i;
		}
		return -1;
	}

	/*****************************************************************
	 * Add Tile Function
	 * @param tileName - character value to convert to an image
	 * @return returns an image which corresponds to the passed character
	 * Helper function that is used to relay information from the 
	 * program model to the program view.
	 *****************************************************************/
	private JLabel addTile(char tileName)
	{
		switch(tileName)
		{
			// Case 1 - 8 used for animation, default used for everything else
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
				return new JLabel(new ImageIcon("artwork\\player\\"+tileName+".png"));
			default:
				return new JLabel(new ImageIcon("artwork\\"+tileName+".png"));
		}
	}

	/*****************************************************************
	 * Play Music Function
	 * @param filename - name of the audio file to play
	 * Creates an Audio Player and begins playing an audio file
	 *****************************************************************/
    public static void playMusic(String filename) 
    {       
    	// create a new aduio player, audio stream, and audio data objects
        AudioPlayer MGP = AudioPlayer.player;
        AudioStream BGM;
        AudioData MD;

        // Create a continuous audio stream
        ContinuousAudioDataStream loop = null;

        // Try to set up audio file, otherwise throw error if file not found or bad file
        try
        {
            InputStream test = new FileInputStream(filename);
            BGM = new AudioStream(test);
            AudioPlayer.player.start(BGM);
            MD = BGM.getData();
            loop = new ContinuousAudioDataStream(MD);
        }
        catch(FileNotFoundException e){
            System.out.print(e.toString());
        }
        catch(IOException error)
        {
            System.out.print(error.toString());
        }
        // Start file playing
        MGP.start(loop); 
    }
	
    /*******************************************************
     * Key Event Handler Class
     * @author Joseph Bourque & Connor Peters
     * Class acts as the controller for the program
     * handles user input and uses input to update model
     * and view.
     ********************************************************/
	private class MyDispatcher implements KeyEventDispatcher 
	{
		private boolean isDisabled = true; // Movement disabled on victory screen
		@Override
		public boolean dispatchKeyEvent(KeyEvent e) 
		{
			if (e.getID() == KeyEvent.KEY_PRESSED) 
			{
				// Enter Key is used to cycle through levels; victory screen acts like a transition screen
				if(e.getKeyCode() == KeyEvent.VK_ENTER && isDisabled)
				{
					currentScreen++;
					updateStage(currentScreen);
					isDisabled = false;
				}
				
				// Up Key Events, used as a directional key
				if(e.getKeyCode() == KeyEvent.VK_UP  && !isDisabled)
				{					
					// Collision Check
					if(getPlayerY() > 0 && stModel[getPlayerY() - 1][getPlayerX()] != 'X')
					{
						// Animation cycling
						if(frameNo!='3')
							frameNo = '3';
						else
							frameNo = '4';
						
						// Check for exit
						if(stModel[getPlayerY() - 1][getPlayerX()] == 'E')
						{
							updateStage(EXIT_REACHED);
							playMusic("audio\\RaichuCry.wav");
							isDisabled = true;
						}
						else
							updatePlayer(getPlayerX(), getPlayerY()-1);
					}
				}
				
				// Down Key Events, used as a directional key
				if(e.getKeyCode() == KeyEvent.VK_DOWN  && !isDisabled)
				{
					// Collision Check
					if(getPlayerY() < stModel.length - 1 && stModel[getPlayerY() + 1][getPlayerX()] != 'X')
					{
						// Animation cycling
						if(frameNo!='1')
							frameNo = '1';
						else
							frameNo = '2';
						
						// Check for exit
						if(stModel[getPlayerY() + 1][getPlayerX()] == 'E')
						{
							updateStage(EXIT_REACHED);
							playMusic("audio\\RaichuCry.wav");
							isDisabled = true;
						}
						else
							updatePlayer(getPlayerX(),getPlayerY()+1);
					}	
				}
				if(e.getKeyCode() == KeyEvent.VK_LEFT  && !isDisabled)
				{
					// Collision Check
					if(getPlayerX() > 0  && stModel[getPlayerY()][getPlayerX() - 1] != 'X')
					{
						// Animation cycling
						if(frameNo!='5')
							frameNo = '5';
						else
							frameNo = '6';
						
						// Check for exit
						if(stModel[getPlayerY()][getPlayerX() - 1] == 'E')
						{
							updateStage(EXIT_REACHED);
							playMusic("audio\\RaichuCry.wav");
							isDisabled = true;
						}
						else
							updatePlayer(getPlayerX() - 1,getPlayerY());
					}	
				}
				if(e.getKeyCode() == KeyEvent.VK_RIGHT  && !isDisabled)
				{					
					// Collision Check
					if(getPlayerX() < stModel[0].length - 1 && stModel[getPlayerY()][getPlayerX() + 1] != 'X')
					{
						// Animation cycling
						if(frameNo!='7')
							frameNo = '7';
						else
							frameNo = '8';
						
						// Check for exit
						if(stModel[getPlayerY()][getPlayerX() + 1] == 'E')
						{
							updateStage(EXIT_REACHED);
							playMusic("audio\\RaichuCry.wav");
							isDisabled = true;
						}
						else
							updatePlayer(getPlayerX() + 1,getPlayerY());
					}
				}
			}  
			return false; // must return a boolean value here
		}
	}
}
