package gameOfLife;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.Random;

public class GameOfLife extends JFrame implements ActionListener
{
	JButton cBStart;
	JTextField cTSeed;
	JLabel cLSeed;
	String cSMsg = "Seed = ";
	GOLCanvas cCanvas;
	int iNumElements = 4;
	int m_iCellWidth;
	int iGameWidth; // number of cells
	int iGameHeight; // number of cells
	private Boolean m_bRunning;
	private double m_dSeed = 0.4;
	
	GameOfLife (String aTitle, int aFrameWidth, int aFrameHeight, int aGameWidth, int aGameHeight, int aCellWidth) 
	{
		super(aTitle);
		this.setSize(aFrameWidth, aFrameHeight);
		m_iCellWidth = aCellWidth;
		iGameWidth = aGameWidth / m_iCellWidth; // total number of pixels / number of pixels per cell = number of cells
		iGameHeight = aGameHeight / m_iCellWidth; // total number of pixels / number of pixels per cell = number of cells
		m_bRunning = false;
		
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() 
        {
            public void windowClosing(WindowEvent e)
            {
                stop();
                super.windowClosing(e);
                System.exit(0);
            }
        });
        
        GridBagLayout cLayout = new GridBagLayout();
		GridBagConstraints[] cConstraints = new GridBagConstraints[iNumElements];		
		
		for(int i = 0; i < iNumElements; i++) 
		{
			cConstraints[i] = new GridBagConstraints();
		}
	
		cCanvas = new GOLCanvas(iGameWidth, iGameHeight, m_dSeed, m_iCellWidth);
		cCanvas.setSize(iGameWidth * m_iCellWidth, iGameHeight * m_iCellWidth);		
		
		
		cConstraints[0].gridx = 0;
		cConstraints[0].gridy = 1;
		cConstraints[0].insets = new Insets(1,1,1,1);

		cConstraints[1].gridx = 1;
		cConstraints[1].gridy = 1;
		cConstraints[1].insets = new Insets(1,1,1,1);

		
		cConstraints[2].gridx = 0;
		cConstraints[2].gridy = 2;
		cConstraints[2].insets = new Insets(1,1,1,1);
		
		cConstraints[3].gridx = 0;
		cConstraints[3].gridy = 0;
		cConstraints[3].insets = new Insets(2,2,2,2);

		cBStart = new JButton("Start");
		cBStart.addActionListener(this);
		cTSeed = new JTextField(5);
		cTSeed.addActionListener(this);
		cLSeed = new JLabel("Seed percentage (0 <= x <= 1):");
		
		cLayout.setConstraints(cLSeed, cConstraints[0]);
		cLayout.setConstraints(cTSeed, cConstraints[1]);
		cLayout.setConstraints(cBStart, cConstraints[2]);
		cLayout.setConstraints(cCanvas, cConstraints[3]);
		
		this.setLayout(cLayout);
		this.add(cTSeed);
		this.add(cLSeed);
		this.add(cBStart);
		this.add(cCanvas);
		this.repaint();
	}
	
	public synchronized void stop() 
	{
		if (m_bRunning) 
		{
			m_bRunning = false;
			boolean bRetry = true;
			while (bRetry) 
			{
				// TODO implement rest of stop from stackexchange answer
			}
		}
	}
	
	public void paint (Graphics aGraphics)
	{
		cCanvas.setSize(new Dimension(iGameWidth * m_iCellWidth, iGameHeight * m_iCellWidth));
		this.paintComponents(aGraphics);
		aGraphics.drawString("This is in frame window", 10, 40);
		aGraphics.drawString(cSMsg, 10, 50);
	}
	
	public void actionPerformed(ActionEvent aActionEvent) 
	{
		String strCmd = aActionEvent.getActionCommand();
		
		if(strCmd.equals("Start")) 
		{
			cCanvas.init();
			cSMsg = "Seed = " + Double.toString(m_dSeed);
		}
		else if (aActionEvent.getSource() == cTSeed)
		{
			m_dSeed = Double.parseDouble(cTSeed.getText());
			cCanvas.setSeed(m_dSeed);
		}
		
		
		this.update(this.getGraphics());
	}
}

class GOLCanvas extends Canvas 
{
	private int m_iWidth;
	private int m_iHeight;
	private GOLCell[][] m_cCells;
	private int[][] m_iNeighbours;
	private double m_dSeed;
	private Random m_cRnd;
	private BufferedImage cBackGroundImage = null;
	private int m_iCellWidth;
		
	GOLCanvas(int aWidth, int aHeight, double aSeed, int aCellWidth) 
	{
		m_iWidth = aWidth;
		m_iHeight = aHeight;
		m_dSeed = aSeed;
		m_iCellWidth = aCellWidth;
		m_cRnd = new Random();
		m_cCells = new GOLCell[m_iWidth][m_iHeight];
		
		for(int i = 0; i < m_iWidth; i++) 
		{
			for(int j = 0; j < m_iHeight; j++)
			{
					m_cCells[i][j] = new GOLCell(m_iCellWidth, i, j, false);
			}
		}
		
		m_cRnd.setSeed(m_cRnd.nextLong());
		
		m_iNeighbours = new int[m_iWidth][m_iHeight];
		cBackGroundImage = new BufferedImage(m_iWidth*m_iCellWidth, m_iHeight*m_iCellWidth, BufferedImage.TYPE_INT_RGB);

	}
	
	public void init() 
	{
		for(int i = 0; i < m_iWidth; i++) 
		{
			for(int j = 0; j < m_iHeight; j++) 
			{
				if (this.getRandomInt(m_dSeed) == 1) 
				{
					m_cCells[i][j].setIsAlive(true);
				}
				else {
					m_cCells[i][j].setIsAlive(false);
				}
			}
		}
	}
	
	public void setSeed(double aSeed)
	{
		m_dSeed = aSeed;
	}
	private int getRandomInt(double aProbability) 
	{
		return (m_cRnd.nextDouble() < m_dSeed) ? 1 : 0;
	}
	
	public int getCellWidth ()
	{
		return m_iCellWidth;
	}
	
	public void countNeighbours () 
	{
		// count neighbours for corner elements: this is a toroid world, wrap around accordingly
		m_iNeighbours[0][0] = m_cCells[0][1].getIsAlive() + m_cCells[1][1].getIsAlive() + m_cCells[1][0].getIsAlive()
				+ m_cCells[m_iWidth - 1][m_iHeight - 1].getIsAlive() + m_cCells[0][m_iHeight - 1].getIsAlive()
				+ m_cCells[m_iWidth - 1][0].getIsAlive() + m_cCells[1][m_iHeight - 1].getIsAlive() 
				+ m_cCells[m_iWidth - 1][1].getIsAlive();
		
		m_iNeighbours[0][m_iHeight-1] = m_cCells[0][m_iHeight-2].getIsAlive() + m_cCells[1][m_iHeight-2].getIsAlive()
				+ m_cCells[1][m_iHeight-1].getIsAlive() + m_cCells[m_iWidth - 1][m_iHeight-1].getIsAlive()
				+ m_cCells[0][0].getIsAlive()  + m_cCells[m_iWidth - 1][0].getIsAlive()
				+ m_cCells[m_iWidth - 1][m_iHeight - 2].getIsAlive() + m_cCells[1][0].getIsAlive();
		
		m_iNeighbours[m_iWidth-1][m_iHeight-1] = m_cCells[m_iWidth-1][m_iHeight-2].getIsAlive()
				+ m_cCells[m_iWidth-2][m_iHeight-2].getIsAlive() + m_cCells[m_iWidth-2][m_iHeight-1].getIsAlive()
				+ m_cCells[0][m_iHeight-1].getIsAlive() + m_cCells[m_iWidth - 1][0].getIsAlive()
				+ m_cCells[0][0].getIsAlive() + m_cCells[0][m_iHeight - 2].getIsAlive()
				+ m_cCells[m_iWidth - 2][0].getIsAlive();
		
		m_iNeighbours[m_iWidth-1][0] = m_cCells[m_iWidth-1][1].getIsAlive() + m_cCells[m_iWidth-2][1].getIsAlive()
				+ m_cCells[m_iWidth-2][0].getIsAlive() + m_cCells[m_iWidth - 1][m_iHeight-1].getIsAlive()
				+ m_cCells[0][m_iHeight-1].getIsAlive() + m_cCells[m_iWidth - 2][m_iHeight-1].getIsAlive()
				+ m_cCells[0][0].getIsAlive() + m_cCells[0][1].getIsAlive();

		// count neighbours of first and last columns without corners
		for (int j = 1; j < m_iHeight - 1; j++) 
		{
			m_iNeighbours[0][j] = 0;
			m_iNeighbours[m_iWidth - 1][j] = 0;
			
			for(int countY = j - 1; countY <= j + 1; countY++) 
			{
				// left column
				for(int countX = 0; countX <=  1; countX++) 
				{
					if (!((countX == 0) && (countY == j))) 
					{
						m_iNeighbours[0][j] += m_cCells[countX][countY].getIsAlive(); 
					}
				}
				// wraparound of left column
				m_iNeighbours[0][j] += m_cCells[m_iWidth - 1][countY].getIsAlive(); 
				
				for(int countX = m_iWidth - 2; countX <= m_iWidth - 1; countX++) 
				{

					if (!((countX == m_iWidth - 1) && (countY == j))) 
					{
						m_iNeighbours[m_iWidth - 1][j] += m_cCells[countX][countY].getIsAlive(); 
					}
				}
				// wraparound of right column
				m_iNeighbours[m_iWidth -1][j] += m_cCells[0][countY].getIsAlive(); 

			}	
		}
		
		// count neighbours of first and last rows without corners
		for (int i = 1; i < m_iWidth - 1; i++)
		{			
			m_iNeighbours[i][0] = 0;
			m_iNeighbours[i][m_iHeight - 1] = 0;

			for(int countX = i - 1; countX <= i + 1; countX++) 
			{
				// top row
				for(int countY = 0; countY <=  1; countY++) 
				{
					if (!((countX == i) && (countY == 0))) 
					{
						m_iNeighbours[i][0] += m_cCells[countX][countY].getIsAlive(); 
					}
				}
				//wraparound of top row
				m_iNeighbours[i][0] += m_cCells[countX][m_iHeight-1].getIsAlive(); 

				
				// bottom row
				for(int countY = m_iHeight - 2; countY <= m_iHeight - 1; countY++) 
				{
					if (!((countX == i) && (countY == m_iHeight - 1))) 
					{
						m_iNeighbours[i][m_iHeight - 1] += m_cCells[countX][countY].getIsAlive(); 
					}
				}
				// wraparound of bottom row
				m_iNeighbours[i][m_iHeight - 1] += m_cCells[countX][0].getIsAlive(); 

			}		
		}
		
		// count neighbours of inner matrix elements without corners and first/last rows
		for(int i = 1; i < m_iWidth - 1; i++) 
		{
			for(int j = 1; j < m_iHeight - 1; j++) 
			{
				m_iNeighbours[i][j] = 0;
				
				for(int countX = i - 1; countX <= i + 1; countX++) 
				{
					for(int countY = j - 1; countY <= j + 1; countY++) 
					{
						if (!((countX == i) && (countY == j))) 
						{
							m_iNeighbours[i][j] += m_cCells[countX][countY].getIsAlive(); 
						}
					}	
				}
			}
		}	
	}
	
	public void calcNextStep () 
	{
		for(int i = 0; i <= m_iWidth - 1; i++) 
		{
			for(int j = 0; j <= m_iHeight - 1; j++) 
			{
				if (m_cCells[i][j].getIsAlive() == 1)
				{
					if((m_iNeighbours[i][j] < 2) || (m_iNeighbours[i][j] > 3))
					{
						// cell dies of underpopulation or overcrowding
						m_cCells[i][j].setIsAlive(false);
					}
				}
				else 
				{
					if (m_iNeighbours[i][j] == 3) 
					{
						// cell becomes alive thanks to reproduction
						m_cCells[i][j].setIsAlive(true);
					}
				}
			}
		}
	}
	@Override
	public void paint(Graphics aGraphics) 
	{
		// store on screen graphics
		Graphics cScreenGraphics = aGraphics;
		// render on background image
		aGraphics = cBackGroundImage.getGraphics();
		
		for(int i = 0; i < m_iWidth; i++) 
		{
			for(int j = 0; j < m_iHeight; j++) 
			{
				if (m_cCells[i][j].getIsAlive() == 1)
				{
					aGraphics.setColor(Color.black);
				}
				else 
				{
					aGraphics.setColor(Color.white);
				}	
				aGraphics.fillRect(m_cCells[i][j].getPixCoordXBegin(), m_cCells[i][j].getPixCoordYBegin()
						, m_iCellWidth, m_iCellWidth);
			}
		}		
		// rendering is done, draw background image to on screen graphics
		cScreenGraphics.drawImage(cBackGroundImage, 0, 0, null);
	}
	
	@Override
	public void update(Graphics aGraphics)
	{
		paint(aGraphics);
	}
}

class GOLCell 
{
	private int m_iPixCoordXBegin; 	// left coordinate
	private int m_iPixCoordYBegin;	// top coordinate
	private int m_iPixCoordXEnd;	// right coordinate
	private int m_iPixCoordYEnd;	// bottom coordinate
	private int m_iCellCoordX;
	private int m_iCellCoordY;
	private int m_iWidth;
	private Boolean m_bIsAlive;
	
	GOLCell () 
	{
		m_iPixCoordXBegin = 0;
		m_iPixCoordYBegin = 0;
		m_iPixCoordXEnd = 0;
		m_iPixCoordYEnd = 0;
		m_iCellCoordX = 0;
		m_iCellCoordY = 0;
		m_iWidth = 1;
		m_bIsAlive = false;
	}
	
	GOLCell (int aiWidth, int aiCellCoordX, int aiCellCoordY, Boolean abIsalive) 
	{
		m_iCellCoordX = aiCellCoordX;
		m_iCellCoordY = aiCellCoordY;
		m_iWidth = aiWidth;
		m_bIsAlive = abIsalive;
		
		m_iPixCoordXBegin = m_iCellCoordX * m_iWidth;
		m_iPixCoordYBegin = m_iCellCoordY * m_iWidth;
		m_iPixCoordXEnd = m_iWidth * (m_iCellCoordX + 1) - 1;
		m_iPixCoordYEnd = m_iWidth * (m_iCellCoordY + 1) - 1;
	}
	
	public int getPixCoordXBegin () 
	{
		return m_iPixCoordXBegin;
	}
	
	public int getPixCoordYBegin () 
	{
		return m_iPixCoordYBegin;
	}
	
	public int getPixCoordXEnd () 
	{
		return m_iPixCoordXEnd;
	}
	
	public int getPixCoordYEnd () 
	{
		return m_iPixCoordYEnd;
	}
	
	public void setIsAlive(Boolean abIsAlive) 
	{
		m_bIsAlive = abIsAlive;
	}
	
	public int getIsAlive() 
	{
		return m_bIsAlive? 1 : 0;
	}
}
//class GOLCalculator implements Runnable {
//	
//}