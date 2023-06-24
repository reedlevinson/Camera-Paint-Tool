import javax.swing.plaf.synth.Region;
import java.awt.*;
import java.awt.image.*;
import java.lang.reflect.Array;
import java.sql.SQLOutput;
import java.util.*;

/**
 * Region growing algorithm: finds and holds regions in an image.
 * Each region is a list of contiguous points with colors similar to a target color.
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 * 
 * @author Chris Bailey-Kellogg, Spring 2014 (based on a very different structure from Fall 2012)
 * @author Travis W. Peters, Dartmouth CS 10, Updated Spring 2015
 * @author CBK, Spring 2015, updated for CamPaint
 *
 * @author Reed Levinson, Spring 2023
 * @author Evan Lai, collaborated on a solution
 */
public class RegionFinder {
	private static final int maxColorDiff = 20;				// how similar a pixel color must be to the target color, to belong to a region
	private static final int minRegion = 50; 				// how many points in a region to be worth considering

	private BufferedImage image;                            // the image in which to find regions
	private BufferedImage recoloredImage;                   // the image with identified regions recolored

	private ArrayList<ArrayList<Point>> regions = new ArrayList<>();			// a region is a list of points
																				// so the identified regions are in a list of lists of points
	private ArrayList<Point> toVisit = new ArrayList<>();						// list of all points that still need to be visited
	private BufferedImage visited;												// image of all points that have been visited

	public RegionFinder() {
		this.image = null;
	}

	public RegionFinder(BufferedImage image) {
		this.image = image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getImage() {
		return image;
	}

	public BufferedImage getRecoloredImage() {
		return recoloredImage;
	}

	/**
	 * Sets regions to the flood-fill regions in the image, similar enough to the trackColor.
	 *
	 * @authors Reed Levinson, Evan Lai, collaborated on this algorithm
	 */
	public void findRegions(Color targetColor) {
		//initializes new image to keep track of visited pixels
		visited = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		//clears previously recorded regions
		regions.clear();
		// loop over all pixels in image
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				Color c = new Color (image.getRGB(x,y));
				//checks to see if point is suitably close to target color
				if (colorMatch(c, targetColor)) {
					//creates a new region of points
					ArrayList<Point> r = new ArrayList<>();
					//adds initial point of region to region
					Point firstPoint = new Point(x, y);
					toVisit.add(firstPoint);
					//runs loop until there are no more locations in region to check
					while (toVisit.size() > 0) {
						//extra check to see if point is unvisited
						if (visited.getRGB(toVisit.get(0).x, toVisit.get(0).y) == 0) {
							//removes point from toVisit, adds to region, and marks as visited (colors in second image)
							Point pRegion = toVisit.remove(0);
							r.add(pRegion);
							visited.setRGB(pRegion.x, pRegion.y, 1);
							//checks neighbors to see if they need to be visited
							for (int dy = Math.max(pRegion.y - 1, 0); dy < Math.min(pRegion.y + 2, image.getHeight()); dy++) {
								for (int dx = Math.max(pRegion.x - 1, 0); dx < Math.min(pRegion.x + 2, image.getWidth()); dx++) {
									Color checkColor = new Color(image.getRGB(dx, dy));
									//checks if neighbor is of close enough color
									if (colorMatch(checkColor, targetColor)) {
										//adds point to toVisit array
										Point p = new Point(dx, dy);
										toVisit.add(p);
									}
								}
							}
						}
						//removes point if already visited
						else {
							toVisit.remove(0);
						}
					}
					//adds region to region list if it is of sufficient size
					if (r.size() >= minRegion) {
						regions.add(r);
					}
				}
				//sets point as visited if not close enough to targetColor
				else {
					visited.setRGB(x, y, 1);
				}
			}
		}
	}

	/**
	 * Tests whether the two colors are "similar enough" (your definition, subject to the maxColorDiff threshold, which you can vary).
	 */
	private static boolean colorMatch(Color c1, Color c2) {
		if (c1 == null || c2 == null) {
			return false;
		}
		int diffRed = Math.abs(c1.getRed() - c2.getRed());			// gets individual RGB differences for each color
		int diffGreen = Math.abs(c1.getGreen() - c2.getGreen());	// then returns a boolean whether the total sum
		int diffBlue = Math.abs(c1.getBlue() - c2.getBlue());		// of differences is greater than a threshold
		return (diffRed <= maxColorDiff && diffGreen <= maxColorDiff && diffBlue <= maxColorDiff);
	}

	/**
	 * Returns the largest region detected (if any region has been detected)
	 */
	public ArrayList<Point> largestRegion() {
		ArrayList<Point> largest = null;
		for (ArrayList<Point> r: regions) {						// checks all determined regions and compares them
			if (largest == null) {								// to see which is largest
				largest = r;
			}
			else if (r.size() > largest.size()) {
				largest = r;
			}
		}
		return largest;											// returns largest region
	}

	/**
	 * Sets recoloredImage to be a copy of image, 
	 * but with each region a uniform random color, 
	 * so we can see where they are
	 */
	public void recolorImage() {
		// First copy the original
		recoloredImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
		// Now recolor the regions in it
		for (ArrayList<Point> r: regions) {
			// chooses random color for the region
			int red = (int)(256*Math.random());
			int green = (int)(256*Math.random());
			int blue = (int)(256*Math.random());
			Color regionColor = new Color (red, green, blue);
			for (Point p: r) {
				recoloredImage.setRGB((int)(p.getX()), (int)(p.getY()), regionColor.getRGB());
			}
		}
	}
}
