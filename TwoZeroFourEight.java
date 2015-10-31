import java.util.Random;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import java.awt.event.KeyAdapter;

 class Game {
	protected Grid grid;
	protected Grid copy;
	protected Player player;
	protected Random rnd;
	protected GameFrame frame;

	public Game(Player player) {
		grid = new Grid();
		copy = new Grid();
		this.player = player;
		rnd = new Random();
	}

	public void setSeed(long seed) {
		rnd.setSeed(seed);
	}

	public void play() {
		addRandomTile();
		addRandomTile();
		while (!grid.gameOver()) {
			grid.getCopy(copy);
			Direction dir = player.selectDirection(copy);
			if (grid.move(dir))
				addRandomTile();
		}
	}
	
	public void playWithUI() {
		addRandomTile();
		addRandomTile();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				prepareFrame();
			}
		});
		while (!grid.gameOver()) {
			grid.getCopy(copy);
			Direction dir = player.selectDirection(copy);
			if (grid.move(dir)) {
				addRandomTile();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						frame.update(grid);
					}
				});
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public int getScore() {
		return grid.getScore();
	}

	protected void addRandomTile() {
		grid.addValue(rnd.nextDouble() < 0.9 ? 2 : 4,
				rnd.nextInt(grid.getEmptyCount()));
	}
	
	protected void prepareFrame() {
		frame = new GameFrame(grid.getSize());
		frame.addComponents();
		frame.update(grid);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
	}
}


 class Cell extends JLabel {
	private static final long serialVersionUID = 1L;
	private static final Dimension PREFERRED_SIZE = new Dimension(100, 100);
	private static final Color[] BG = {
		new Color(0xcc, 0xc0, 0xb3),
		null,
		new Color(0xee, 0xe4, 0xda),
		new Color(0xed, 0xe0, 0xc8),
		new Color(0xf2, 0xb1, 0x79),
		new Color(0xf5, 0x95, 0x63),
		new Color(0xf6, 0x7c, 0x5f),
		new Color(0xf6, 0x5e, 0x3b),
		new Color(0xed, 0xcf, 0x72),
		new Color(0xed, 0xcc, 0x61),
		new Color(0xed, 0xc8, 0x50),
		new Color(0xed, 0xc5, 0x3f),
		new Color(0xed, 0xc2, 0x2e),
		new Color(0x3c, 0x3a, 0x32),
	};
	private static final Color[] FG = {
		new Color(0xcc, 0xc0, 0xb3),
		null,
		new Color(0x77, 0x6e, 0x65),
		new Color(0x77, 0x6e, 0x65),
		new Color(0xf9, 0xf6, 0xf2),
	};
	
	public Cell() {
		super();
		setPreferredSize(PREFERRED_SIZE);
		setOpaque(true);
		setHorizontalAlignment(SwingConstants.CENTER);
		setFont(new Font(getFont().getName(), Font.BOLD, 30));
	}
	
	public void setValue(int value) {
		setText("" + value);
		int i = 0;
		while (value > 0) {
			value >>= 1;
			i++;
		}
		setBackground(BG[Math.min(i, BG.length - 1)]);
		setForeground(FG[Math.min(i, FG.length - 1)]);
	}
}


 class Grid {
	private static final int SIZE = 4;

	private int[][] values;
	private boolean[][] merged;
	private int emptyCount;
	private int score;

	public Grid() {
		values = new int[SIZE][SIZE];
		merged = new boolean[SIZE][SIZE];
		emptyCount = SIZE * SIZE;
		score = 0;
	}

	public int getSize() {
		return SIZE;
	}

	public int getValue(int x, int y) {
		return values[y][x];
	}

	public int getEmptyCount() {
		return emptyCount;
	}

	public int getScore() {
		return score;
	}

	public boolean move(Direction dir) {
		for (int y = 0; y < SIZE; y++)
			for (int x = 0; x < SIZE; x++)
				merged[y][x] = false;
		boolean moved = false;
		switch (dir) {
		case UP:
			for (int x = 0; x < SIZE; x++)
				for (int y = 0; y < SIZE; y++)
					moved = moveValue(x, y, dir) || moved;
			break;
		case DOWN:
			for (int x = 0; x < SIZE; x++)
				for (int y = SIZE - 1; y >= 0; y--)
					moved = moveValue(x, y, dir) || moved;
			break;
		case LEFT:
			for (int y = 0; y < SIZE; y++)
				for (int x = 0; x < SIZE; x++)
					moved = moveValue(x, y, dir) || moved;
			break;
		case RIGHT:
			for (int y = 0; y < SIZE; y++)
				for (int x = SIZE - 1; x >= 0; x--)
					moved = moveValue(x, y, dir) || moved;
			break;
		}
		return moved;
	}

	private boolean moveValue(int x, int y, Direction dir) {
		if (values[y][x] == 0)
			return false;
		int nx = x + dir.getX();
		int ny = y + dir.getY();
		if (nx < 0 || nx >= SIZE || ny < 0 || ny >= SIZE)
			return false;
		if (values[ny][nx] == 0) {
			values[ny][nx] = values[y][x];
			values[y][x] = 0;
			moveValue(nx, ny, dir);
			return true;
		}
		if (values[y][x] == values[ny][nx] && !merged[ny][nx]) {
			values[ny][nx] *= 2;
			values[y][x] = 0;
			merged[ny][nx] = true;
			emptyCount++;
			score += values[ny][nx];
			return true;
		}
		return false;
	}

	public void addValue(int value, int position) {
		//if (value != 2 && value != 4)
		//	throw new IllegalArgumentException("value must be 2 or 4");
		int i = 0;
		for (int y = 0; y < SIZE; y++)
			for (int x = 0; x < SIZE; x++)
				if (values[y][x] == 0) {
					if (i == position) {
						values[y][x] = value;
						emptyCount--;
						return;
					}
					i++;
				}
	}

	public boolean gameOver() {
		if (emptyCount > 0)
			return false;
		for (int y = 0; y < SIZE; y++)
			for (int x = 1; x < SIZE; x++)
				if (values[y][x] == values[y][x - 1])
					return false;
		for (int x = 0; x < SIZE; x++)
			for (int y = 1; y < SIZE; y++)
				if (values[y][x] == values[y - 1][x])
					return false;
		
		try {
				Thread.sleep(3000);
				System.exit(0);		
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
	}
	
	public void getCopy(Grid copy) {
		for (int y = 0; y < SIZE; y++)
			for (int x = 0; x < SIZE; x++)
				copy.values[y][x] = values[y][x];
		copy.emptyCount = emptyCount;
		copy.score = score;
	}

	@Override
	public String toString() {
		String s = "";
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++)
				s += String.format("%5d", values[y][x]);
			s += "\n";
		}
		return s;
	}
}

 enum Direction {
	UP(0, -1), 
	DOWN(0, 1), 
	LEFT(-1, 0), 
	RIGHT(1, 0);
	
	private final int x;
	private final int y;
	
	private Direction(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
}

 interface Player {
	Direction selectDirection(Grid grid);
}


 class HumanGame extends Game {
	public HumanGame() {
		super(null);
	}

	@Override
	public void play() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void playWithUI() {
		addRandomTile();
		addRandomTile();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				prepareFrame();
				setKeyListener();
			}
		});

	}
	
	private void setKeyListener() {
		frame.addKeyListener(new KeyAdapter() {
		    public void keyReleased(KeyEvent e) {
		        int code = e.getKeyCode();
		        switch (code) {
		        case KeyEvent.VK_UP:
		        case KeyEvent.VK_KP_UP:
		        	move(Direction.UP);
		        	break;
		        case KeyEvent.VK_DOWN:
		        case KeyEvent.VK_KP_DOWN:
		        	move(Direction.DOWN);
		        	break;
		        case KeyEvent.VK_LEFT:
		        case KeyEvent.VK_KP_LEFT:
		        	move(Direction.LEFT);
		        	break;
		        case KeyEvent.VK_RIGHT:
		        case KeyEvent.VK_KP_RIGHT:
		        	move(Direction.RIGHT);
		        	break;
		        }
		    }
		});
	}
	
	private void move(Direction dir) {
		if (!grid.gameOver() && grid.move(dir)) {
			addRandomTile();
			frame.update(grid);
		}
	}
}

 class GameFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private int size;
	private Cell[][] cells;
	private JLabel scoreLabel;

	public GameFrame(int size) {
		super("2048");
		this.size = size;
	}

	public void addComponents() {
		JPanel panel = new JPanel();
		GridLayout layout = new GridLayout(size, size);
		layout.setHgap(10);
		layout.setVgap(10);
		panel.setLayout(layout);
		panel.setBackground(new Color(0xbb, 0xad, 0xa0));
		cells = new Cell[size][size];
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++) {
				cells[y][x] = new Cell();
				cells[y][x].setValue(0);
				panel.add(cells[y][x]);
			}
		scoreLabel = new JLabel("Score: 0");
		getContentPane().add(scoreLabel, BorderLayout.NORTH);
		getContentPane().add(new JSeparator(), BorderLayout.CENTER);
		getContentPane().add(panel, BorderLayout.SOUTH);
	}

	public void update(Grid grid) {
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++)
				cells[y][x].setValue(grid.getValue(x, y));
		scoreLabel.setText("Score: " + grid.getScore());
	}
}

 class PsychorigidPlayer implements Player {
	private static final Direction[] PREFERRED_ORDER = {Direction.LEFT, Direction.DOWN, Direction.UP, Direction.RIGHT};
	public Direction selectDirection(Grid grid) {
		for (Direction dir: PREFERRED_ORDER)
			if (grid.move(dir))
				return dir;
		return null;
	}
}


 class MyopicPlayer implements Player {
	private Random rnd;
	private Grid copy;
	private List<Direction> candidates;
	
	public MyopicPlayer() {
		rnd = new Random();
		copy = new Grid();
		candidates = new ArrayList<Direction>();
	}

	public Direction selectDirection(Grid grid) {
		int bestScore = -1;
		for (Direction dir : Direction.values()) {
			grid.getCopy(copy);
			if (copy.move(dir)) {
				int score = copy.getScore();
				if (score > bestScore) {
					bestScore = score;
					candidates.clear();
				}
				if (score == bestScore)
					candidates.add(dir);
			}
		}
		return candidates.get(rnd.nextInt(candidates.size()));
	}

}


 class MonkeyPlayer implements Player {
	Random rnd;
	
	public MonkeyPlayer() {
		rnd = new Random();
	}

	public Direction selectDirection(Grid grid) {
		return Direction.values()[rnd.nextInt(Direction.values().length)];
	}

}


 class TwoZeroFourEight {
	public static void main(String[] args) {
		Game hg = new HumanGame();
		hg.playWithUI();
	}

}
