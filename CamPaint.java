import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;

/**
 * Webcam-based drawing 
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 * 
 * @author Chris Bailey-Kellogg, Spring 2015 (based on a different webcam app from previous terms)
 *
 * @author Reed Levinson, Spring 2023
 * @author Evan Lai, collaborated on a solution
 */
public class CamPaint extends Webcam {
	private char displayMode = 'w';			// what to display: 'w': live webcam, 'r': recolored image, 'p': painting
	private RegionFinder finder;			// handles the finding
	private Color targetColor;          	// color of regions of interest (set by mouse press)
	private Color paintColor = Color.blue;	// the color to put into the painting from the "brush"
	private ArrayList<Point> keyRegion;		// region to be treated as brush
	private boolean brushDown = false;		// used for toggling painting
	private BufferedImage painting;			// the resulting masterpiece

	/**
	 * Initializes the region finder and the drawing
	 */
	public CamPaint() {
		finder = new RegionFinder();
		clearPainting();
	}

	/**
	 * Resets the painting to a blank image
	 */
	protected void clearPainting() {
		painting = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * DrawingGUI method, here drawing one of live webcam, recolored image, or painting, 
	 * depending on display variable ('w', 'r', or 'p')
	 */
	@Override
	public void draw(Graphics g) {
		// draws webcam with no modifications
		if (displayMode == 'w') {
			g.drawImage(image, 0, 0, null);
		}
		// draws webcam with targeted regions colored
		else if (displayMode == 'r') {
			processImage();
			g.drawImage(image, 0, 0, null);
		}
		// draws painting using largest colored region as a paintbrush
		else if (displayMode =='p') {
			processImage();
			g.drawImage(painting, 0, 0, null);
		}
	}

	/**
	 * Webcam method, here finding regions and updating the painting.
	 */
	@Override
	public void processImage() {
		// makes no change if no target color has been selected
		if (targetColor == null) {
			return;
		}
		if (displayMode == 'r') {
			// sets current image to webcam feed
			finder.setImage(image);
			// finds regions in image of target color
			finder.findRegions(targetColor);
			// recolors these regions in image
			finder.recolorImage();
			// replaces image with recolored regions
			image = finder.getRecoloredImage();
		}
		else if (displayMode == 'p') {
			//only paints if the brush is down
			if (brushDown) {
				finder.setImage(image);
				finder.findRegions(targetColor);
				// identifies the largest region of regions near target color
				keyRegion = finder.largestRegion();
				// paints nothing if no region is found
				if (keyRegion == null) {
					return;
				}
				// paints each point in region in desired color on painting
				for (Point p : keyRegion) {
					painting.setRGB(p.x, p.y, paintColor.getRGB());
				}
			}
		}
	}

	/**
	 * Overrides the DrawingGUI method to set the track color.
	 */
	@Override
	public void handleMousePress(int x, int y) {
		targetColor = new Color(image.getRGB(x, y));
	}

	/**
	 * DrawingGUI method, here doing various drawing commands
	 */
	@Override
	public void handleKeyPress(char k) {
		if (k == 'p' || k == 'r' || k == 'w') { // display: painting, recolored image, or webcam
			displayMode = k;
		}
		else if (k == 'c') { // clear
			clearPainting();
		}
		else if (k == 'o') { // save the recolored image
			saveImage(finder.getRecoloredImage(), "pictures/recolored.png", "png");
		}
		else if (k == 's') { // save the painting
			saveImage(painting, "pictures/painting.png", "png");
		}
		else if (k == 'b') { // toggles painting
			brushDown = !brushDown;
		}
		else if (k == '1') { // sets color to red
			paintColor = Color.red;
		}
		else if (k == '2') { // sets color to orange
			paintColor = Color.orange;
		}
		else if (k == '3') { // sets color to yellow
			paintColor = Color.yellow;
		}
		else if (k == '4') { // sets color to green
			paintColor = Color.green;
		}
		else if (k == '5') { // sets color to blue
			paintColor = Color.blue;
		}
		else if (k == '6') { // sets color to magenta
			paintColor = Color.magenta;
		}
		else if (k == '7') { // sets color to pink
			paintColor = Color.pink;
		}
		else if (k == '8') { // sets color to black
			paintColor = Color.black;
		}
		else {
			System.out.println("unexpected key "+k);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CamPaint();
			}
		});
	}
}
