import java.util.*;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
* Binary Search Partition object class. For more details see '../references/reference.pdf'
*
* @author HUYLENBROECK Florent, DACHY Corentin
*/
public class BSP{
	private Node root;
	private String path;

	/**
	* Random heuristic constant.
	*/
	public static final int RANDOM = 0;
	/**
	* Ordered heuristic constant.
	*/
	public static final int ORDERED = 1;
	/**
	* Free splits heuristic constant.
	*/
	public static final int FREE_SPLITS = 2;

	private int x_bound;
	private int y_bound;
	private int n_segments;

	private Random rd = new Random();

	/**
	* Sentinel value for slope.
	*/
	public static final float INF = (float)Float.MAX_VALUE;

	/**
	* @param path 		String, path to the Scene2D file.
	* @param heuristic 	int, type of heuristic to use, see class variables.
	*/
	public BSP(String path, int heuristic){
		this.path=path;
		ArrayList<Segment> segments = openBSPFile(path);

		root = new Node();

		switch(heuristic){
			case 1: orderedHeuristicBSP(segments);
			break;
			case 2: freeSplitsHeuristicBSP(segments);
			break;
			case 0:
			default: randomHeuristicBSP(root, segments);;
		}
	}

	private void randomHeuristicBSP(Node root, ArrayList<Segment> segments){
		int split = rd.nextInt(segments.size());
		Segment segment = segments.get(split);
		root.addSegment(segments.get(split));
		segments.remove(split);
		root.setM(slope(segment));
		root.setP(intercept(segment));

		ArrayList<Segment> left = new ArrayList<Segment>();
		ArrayList<Segment> right = new ArrayList<Segment>();

		split(root.getM(), root.getP(), segments, left, right);

		root.addSegment(segments);

		if(left.size()>0){
			Node leftSon = new Node();
			randomHeuristicBSP(leftSon, left);
			root.setLeft(leftSon);
		}
		if(right.size()>0){
			Node rightSon = new Node();
			randomHeuristicBSP(rightSon, right);
			root.setLeft(rightSon);
		}
	}

	private void orderedHeuristicBSP(ArrayList<Segment> segments){

	}

	private void freeSplitsHeuristicBSP(ArrayList<Segment> segments){

	}

	/**
	* Returns the root of the BSP.
	*
	* @return 	Node, root of the BSP.
	*/
	public Node getRoot(){
		return root;
	}

	/**
	* Returns the bound for the x axis. Every value is contained in [-x_bound;x_bound].
	*
	* @return int, bound for the x axis of the scene.
	*/
	public int getXBound(){
		return x_bound;
	}

	/**
	* Returns the bound for the y axis. Every value is contained in [-y_bound;y_bound].
	*
	* @return int, bound for the y axis of the scene.
	*/
	public int getYBound(){
		return y_bound;
	}

	/**
	* Returns the number of segment contained in the BSP.
	*
	* @return int, number of segments contained in the scene.
	*/
	public int getNSegments(){
		return n_segments;
	}

	/**
	* Returns the path to the Scene2D file.
	*
	* @return 	String, the path to SCene 2D file.
	*/
	public String getPath(){
		return path;
	}

	/**
	* Reads a Scene file and initialize x_bound, y_bound and n_segments before returning the segments.
	*
	* @param path 	String, path to the Scene2D file.
	* @return 		ArrayList<String[]> containing all the segment as they are described in the Scene2D file, as Strings.
	*/
	private ArrayList<Segment> openBSPFile(String path){
		ArrayList<Segment> segments = new ArrayList<Segment>();

		try{
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String line;
			Boolean first_line=true;
			while((line=reader.readLine())!=null){
				String[] words = line.split(" ");
				if(first_line){
					x_bound=Integer.parseInt(words[1]); // skips useless first ">" char
					y_bound=Integer.parseInt(words[2]);
					n_segments=Integer.parseInt(words[3]);
					first_line=false;
				}
				else{
					segments.add(new Segment(line.split(" ")));
				}
				
			}

		} catch(IOException ioe){
			throw new RuntimeException(ioe);
		}

		return segments;
		
	}

	private float slope(Segment segment){
		if(segment.getP1().getX()-segment.getP2().getX() == 0.f)
			return INF;
		else
			return (float)((segment.getP1().getY()-segment.getP2().getY())/(segment.getP1().getX()-segment.getP2().getX()));
	}

	private float intercept(Segment segment){
		float slope = slope(segment);
		if(slope == INF)
			return (float)segment.getP1().getX();
		else
			return (float)(segment.getP1().getY()-slope*segment.getP1().getX());
	}

	/**
	* Splits an ArrayList of Segment into two ArrayLists of Segment given a split line y = mx+p.
	* Segments above that line are added to right ArrayList.
	* Segments under that line are added to left ArrayList.
	* Segments included in that line are left in the ArrayList
	* Segments that are intersecting the line are split and each half is processed individually.
	*/
	private void split(float m, float p, ArrayList<Segment> segments, ArrayList<Segment> left, ArrayList<Segment> right){
		if(m==INF){
			for(Segment segment : segments){
				float a = (float)segment.getP1().getX();
				float b = (float)segment.getP2().getX();

				if(a==p && b==p)
					continue;
				else if(a>=p && b>=p)
					left.add(segment);
				else if(a<=p && b<=p)
					right.add(segment);
				else{
					float intersectionY = slope(segment)*p+intercept(segment);
					Point2D.Float intersect = new Point2D.Float(p, intersectionY);
					segments.add(new Segment(segment.getP1(), intersect, segment.getColor()));
					segments.add(new Segment(segment.getP2(), intersect, segment.getColor()));
				}
				segments.remove(segment);
			}
		}
		else{	
			for(Segment segment : segments){
				float a = (float)(m*segment.getP1().getX()-p-segment.getP1().getY());
				float b = (float)(m*segment.getP2().getX()-p-segment.getP2().getY());

				if(a==0.f && b==0.f)
					continue;
				else if (a>=0.f && b>=0.f)
					left.add(segment);
				else if (a<=0.f && b<=0.f)
					right.add(segment);
				else{
					Point2D.Float intersect = intersection(segment, m, p);
					segments.add(new Segment(segment.getP1(), intersect, segment.getColor()));
					segments.add(new Segment(segment.getP2(), intersect, segment.getColor()));
				}
				segments.remove(segment);
			}
		}
	}

	private Point2D.Float intersection(Segment segment, Float m, Float p){
		float m2 = slope(segment);
		float p2 = intercept(segment);

		if(m2==INF)
			return new Point2D.Float((float)segment.getP1().getX(), m*p2+p);
		else{
			float intersectionX = (p2-p)/(m-m2);
			float intersectionY = m*intersectionX+p;

			return new Point2D.Float(intersectionX, intersectionY);
		}
	}

	/**
	* Represents a node of the BSP tree
	*
	* @author HUYLENBROECK Florent
	*/
	public class Node{

		private LinkedList<Segment> data;
		private Node left;
		private Node right;
		private float m, p;

		public Node(){
			data=new LinkedList<Segment>();
		}

		public Node(Segment split){
			this();
			addSegment(split);
		}

		/**
		* @param segment 	Segment to initialize the Node with.
		* @param left 		Node, left son.
		* @param right 		Node, right son.
		* @param m 			float, slope of the 2D line that the Node describes.
		* @param p 			float, intercept of the 2D line that the Node describes.
		*/
		public Node(Segment segment, Node left, Node right, float m, float p){
			this();
			addSegment(segment);
			this.left=left;
			this.right=right;
			this.m=m;
			this.p=p;
		}

		/**
		* @param segments 	ArrayList<Segment> to initialize the Node with.
		* @param left 		Node, left son.
		* @param right 		Node, right son.
		* @param m 			float, slope of the 2D line that the Node describes.
		* @param p 			float, intercept of the 2D line that the Node describes.
		*/
		public Node(ArrayList<Segment> segments, Node left, Node right, float m, float p){
			this();
			for(Segment segment : segments){
				addSegment(segment);
			}
			this.left=left;
			this.right=right;
			this.m=m;
			this.p=p;
		}

		/**
		* @param leaf 		Node (Leaf) to convert into inner Node.
		* @param left 		Node, left son.
		* @param right 		Node, right son.
		* @param m 			float, slope of the 2D line that the Node describes.
		* @param p 			float, intercept of the 2D line that the Node describes.
		*/
		public Node(Node leaf, Node left, Node right, float m, float p){
			this();
			Iterator leafIter = leaf.getData();
			while(leafIter.hasNext()){
				addSegment((Segment)leafIter.next());
			}
			this.left=left;
			this.right=right;
			this.m=m;
			this.p=p;
		}



		/**
		* Returns the left son of the Node.
		*
		* @return 	Node, left son.
		*/
		public Node getLeft(){
			return left;
		}

		/**
		* Sets the left son of the Node.
		*
		* @param left 	Node to set as the left son.
		*/
		public void setLeft(Node left){
			this.left=left;
		}

		/**
		* Returns the right son of the Node.
		*
		* @return 	Node, right son.
		*/
		public Node getRight(){
			return right;
		}

		/**
		* Sets the right son of the Node.
		*
		* @param right 	Node to set as the right son.
		*/ 
		public void setRight(Node right){
			this.right=right;
		}

		/**
		* Returns an iterator for the LinkedList of the segments contained in the Node.
		*
		* @return 	Iterator for the segment list.
		*/
		public Iterator getData(){
			return data.iterator();
		}

		/**
		* Adds a segment to a Node.
		*
		* @param segment  	Segment to add.
		* @return 	Boolean, true if the Segment has been added successfuly (useful in Leaf sub-class)
		*/
		public Boolean addSegment(Segment segment){
			data.add(segment);
			return true;
		}

		public Boolean addSegment(ArrayList<Segment> segments){
			for(Segment segment : segments){
				data.add(segment);
			}
			return true;
		}

		/**
		* Clears all the segments contained in a Node.
		*/
		public void clearSegments(){
			data=new LinkedList<Segment>();
		}

		/**
		* Returns the number of segment contained in a Node.
		*
		* @return 	int, number of segments.
		*/
		public int getSize(){
			return data.size();
		}

		/**
		* Tells if the Node is a leaf or an inner Node.
		*
		* @return 	Boolean, true if the Node is a Leaf.
		*/
		public Boolean isLeaf(){
			return(left.equals(null) && right.equals(null));
		}
		
		public float getM(){
			return m;
		}		

		public void setM(float m){
			this.m = m;
		}

		public float getP(){
			return p;
		}

		public void setP(float p){
			this.p = p;
		}
	}

	/**
	* Represents a leaf of the BSP tree, thus does not contain a line equation, left or right son, nor multiple data.
	*/
	public class Leaf extends Node{

		/**
		* @param segment 	Segment that the Leaf contains.
		*/
		public Leaf(Segment segment){
			super(segment, null, null, 0.f, 0.f);
		}

		/**
		* Adds a segment to the Leaf.
		*
		* @param segment 
		*/
		public Boolean addSegment(Segment segment){
			if(this.getSize()<1){
				this.addSegment(segment);
				return true;
			}
			else
				return false;
		}
	}

	/**
	* Represents a segment contained in a Node.
	*
	* @author HUYLENBROECK Florent
	*/
	public class Segment{
		private Point2D.Float p1, p2;
		private Color color;

		/**
		* @param x1 	float, x coordinate for the first extremity of the Segment.
		* @param y1 	float, y coordinate for the first extremity of the Segment.
		* @param x2 	float, x coordinate for the second extremity of the Segment.
		* @param y2 	float, y coordinate for the second extremity of the Segment.
		* @param color 	Color of the Segment.
		*/
		public Segment(float x1, float y1, float x2, float y2, Color color){
			p1=new Point2D.Float(x1, y1);
			p2=new Point2D.Float(x2, y2);
			this.color=color;
		}

		public Segment(Point2D.Float p1, Point2D.Float p2, Color color){
			this.p1 = p1;
			this.p2 = p2;
			this.color = color;
		}

		public Segment(String[] fromFile){
			p1=new Point2D.Float(Float.parseFloat(fromFile[0]),Float.parseFloat(fromFile[1]));
			p2=new Point2D.Float(Float.parseFloat(fromFile[2]),Float.parseFloat(fromFile[3]));
			switch(fromFile[4]){
				case "Bleu": color=Color.BLUE;
				break;
				case "Rouge": color=Color.RED;
				break;
				case "Orange": color=Color.ORANGE;
				break;
				case "Jaune": color=Color.YELLOW;
				break;
				case "Noir": color=Color.BLACK;
				break;
				case "Violet": color=Color.MAGENTA;
				break;
				case "Marron": color=Color.DARK_GRAY;
				break;
				case "Vert": color=Color.GREEN;
				break;
				case "Gris": color=Color.LIGHT_GRAY;
				break;
				case "Rose": color=Color.PINK;
				break;
				default: color=Color.WHITE;
			}
		}

		/**
		* Returns the coordinates of the first extremity of the segment.
		*
		* @return 	Point2D.Float first extremity of the segment.
		*/
		public Point2D.Float getP1(){
			return p1;
		}

		/**
		* Sets the coordinates for the first extremity of the segment.
		*
		* @param x1 	float, x coordinate for the first extremity of the Segment.
		* @param y1 	float, y coordinate for the first extremity of the Segment.
		*/
		public void setP1(float x1, float y1)
		throws OutOfSceneException{
			if(x1<=x_bound && x1>=-x_bound && y1<=y_bound && y1>=-y_bound)
				p1=new Point2D.Float(x1, y1);
			else
				throw new OutOfSceneException("Out of scene's bounds");
		}

		/**
		* Returns the coordinates of the second extremity of the segment.
		*
		* @return 	Point2D.Float second extremity of the segment.
		*/
		public Point2D.Float getP2(){
			return p2;
		}

		/**
		* Sets the coordinates for the second extremity of the segment.
		*
		* @param x2 	float, x coordinate for the second extremity of the Segment.
		* @param y2 	float, y coordinate for the second extremity of the Segment.
		*/
		public void setP2(float x2, float y2)
		throws OutOfSceneException{
			if(x2<=x_bound && x2>=-x_bound && y2<=y_bound && y2>=-y_bound)
				p2=new Point2D.Float(x2, y2);
			else
				throw new OutOfSceneException("Out of scene's bounds");
		}

		/**
		* Returns the color fo the segment.
		*
		* @return 	Color of the Segment.
		*/
		public Color getColor(){
			return color;
		}

		/**
		* Sets the color of the segment.
		*
		* @param color 	Color to set.
		*/
		public void setColor(Color color){
			this.color=color;
		}
	}

	/**
	* Parent class for all BSP-related Exceptions
	*
	* @author HUYLENBROECK Florent
	*/
	public class BSPException extends Exception{
		public BSPException(String message){
			super(message);
		}
	}

	/**
	* BSPException class for when a point is trying to be set ouf of the scene bound's.
	*
	* @author HUYLENBROECK Florent
	*/
	public class OutOfSceneException extends BSPException{
		public OutOfSceneException(String message){
			super(message);
		}
	}
}