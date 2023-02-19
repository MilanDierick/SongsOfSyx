package view.ui.tech;

import java.util.Arrays;
import java.util.Comparator;

import init.C;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import init.tech.TECHS;
import snake2d.LOG;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LinkedList;
import util.gui.misc.GText;

final class NodeCreator {

	static RENDEROBJ[][] make(){
		
		RENDEROBJ[][] res = create((9*C.WIDTH()/C.MIN_WIDTH));
		
		int m = 9*C.WIDTH()/C.MIN_WIDTH;
		for (RENDEROBJ[] r : res) {
			m = Math.max(m, r.length);
		}
		
		return create(m);
		
	}
	
	private static RENDEROBJ[][] create(int m){
		
		LinkedList<Group> groups = groups(); 

		LinkedList<TreeGroup> trees = new LinkedList<>();
		
		for (Group g : groups) {
			trees.add(new TreeGroup(g, m));
		}
		
		int h = 0;
		int w = 0;
		
		for (TreeGroup t : trees) {
			w = Math.max(w, t.nodes[0].length);
			h += t.nodes.length;
		}
		
		m = Math.max(w, m);
		
		RENDEROBJ[][] res = new RENDEROBJ[h][m];
		

		int y = 0;
		
		int hiY = 0;
		int lastX = 0;
		
		for (TreeGroup t : trees) {
			
			w = 0;
			for (int dy = 0; dy < t.nodes.length; dy++)
				w = Math.max(w, t.nodes[dy].length);
				
			
			if (w + lastX > m) {
				y += hiY;
				lastX = 0;
				hiY = 0;
				
			}

			for (int dy = 0; dy < t.nodes.length; dy++) {
				
				for (int dx = 0; dx < t.nodes[dy].length; dx++) {
					res[y+dy][dx+lastX] = t.nodes[dy][dx];
				}
				
			}
			lastX += t.nodes[0].length + 1;
			hiY = Math.max(hiY, t.nodes.length);
			
		}
		y += hiY;
		
		RENDEROBJ[][] fin = new RENDEROBJ[y][res[0].length]; 
		
		for (y = 0; y < fin.length; y++) {
			for (int x = 0; x < res[y].length; x++) {
				if (res[y][x] == null) {
					res[y][x] = new RENDEROBJ.RenderDummy(Node.WIDTH, Node.HEIGHT);
				}
				fin[y][x] = res[y][x];
			}
		}
		
		
		
		
		
		return fin;
		
		
	}
	
	private static LinkedList<Group> groups(){
		LinkedList<Node> techs = new LinkedList<>();
		
		KeyMap<Group> cats = new KeyMap<>();
		LinkedList<Group> groups = new LinkedList<>();
		
		for (TECH t : TECHS.ALL()) {
			Node n = new Node(t);
			techs.add(n);
			if (!cats.containsKey(t.category)) {
				Group g = new Group(t.category);
				groups.add(g);
				cats.put(t.category, g);
			}
			cats.get(t.category).add(n);
		}

		Comparator<Node> nComp = new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return o1.tech.order.compareTo(o2.tech.order);
			}
		
		};
		
		int i = 0;
		Group[] sort = new Group[groups.size()];
		for (Group g : groups) {
			
			Node[] ns = new Node[g.size()];
			int k = 0;
			for (Node n : g) {
				ns[k++] = n;
				
			}
			
			Arrays.sort(ns, nComp);
			g.clear();
			for (Node n : ns) {
				g.add(n);
			}
			sort[i++] = g;
		}
		
		Arrays.sort(sort, new Comparator<Group>() {

			@Override
			public int compare(Group o1, Group o2) {
				return o1.order.compareTo(o2.order);
			}
			
		});
		
		groups.clear();
		for (Group g : sort)
			groups.add(g);
		
		
		return groups;
	}
	
	private static class Group extends LinkedList<Node> implements Comparable<Group> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final CharSequence name;
		private String order = "";
		Group(CharSequence name){
			this.name = name;
		}
		
		@Override
		public int add(Node element) {
			
			if (element.tech.order.compareTo(order) > 0)
				order = element.tech.order;
			return super.add(element);
		}

		@Override
		public int compareTo(Group o) {
			return order.compareTo(o.order);
		};

	}
	

	
	private static class TreeGroup {
		
		private RENDEROBJ[][] nodes;
		public CharSequence name;
		
		TreeGroup(Group g, int maxW){
			
			name = g.name;
			LinkedList<SubGroup> groups = new LinkedList<>();
			
			while(!g.isEmpty()) {
				Node n = g.removeFirst();
				new SubGroup(groups, n);
			}
			
			LinkedList<Tree> trees = new LinkedList<>();
			for (SubGroup t : groups)
				trees.add(new Tree(t));
			
			
			for (Tree t : trees) {
				maxW = Math.max(maxW,  t.nodes[0].length);
			
			}
			
			LinkedList<LinkedList<Tree>> rows = new LinkedList<LinkedList<Tree>>();
			
			int wi = 0;
			while(!trees.isEmpty()) {
				
				Tree t = trees.removeFirst();
				LinkedList<Tree> row = new LinkedList<>();
				rows.add(row);
				row.add(t);
				int w = t.nodes[0].length;
				wi = Math.max(wi, w);
				while(!trees.isEmpty()) {
					if (w + trees.getFirst().nodes[0].length > maxW)
						break;
					w += trees.getFirst().nodes[0].length;
					wi = Math.max(wi, w);
					row.add(trees.removeFirst());
				}
				
			}
			
			int h = 0;
			
			for (LinkedList<Tree> row : rows) {
				int hi = 0;
				for (Tree t : row) {
					hi = Math.max(hi, t.nodes.length);
				}
				h+=hi;
			}
			{
				int am = 0;
				for (LinkedList<Tree> row : rows)
					am = Math.max(am, row.size());
				nodes = new RENDEROBJ[h][wi];
			}
			
			wi = 0;
			for (LinkedList<Tree> row : rows) {
				for (Tree t : row) {
					wi = Math.max(insert(t), wi);
					
				}
			}
			
			RENDEROBJ[][] fi = new RENDEROBJ[nodes.length+1][wi];
			fi[0][0] = new RENDEROBJ.RenderImp(Node.WIDTH, Node.HEIGHT) {
				final GText tt = new GText(UI.FONT().H2, name).lablifySub();
				final int x1 = (fi[0].length * Node.WIDTH)/2 - tt.width()/2;
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					tt.render(r, body().x1() + x1, body().y2()-tt.height()-8);
				}
			};
			
			for (int y = 1; y < fi.length; y++) {
				fi[y] = nodes[y-1];
			}
			
			nodes = fi;
			
		}
		
		private int insert(Tree tree) {
			
			int w = tree.nodes[0].length;
			int h = tree.nodes.length;
			
			for (int sx = 0; sx < nodes[0].length; sx++) {
				con:
				for (int sy = 0; sy < nodes.length-h+1; sy++) {
					if (nodes[sy][sx] == null) {
						
						for (int y = 0; y < h; y++) {
							for (int x = 0; x < w; x++) {
								if (sy+y >= nodes.length || sx+x >= nodes[sy+y].length)
									continue con;
								if (nodes[sy+y][sx+x] != null) {
									continue con;
								}
							}
						}
						
						for (int y = 0; y < h; y++) {
							for (int x = 0; x < w; x++) {
								RENDEROBJ r = tree.nodes[y][x];
								if (r == null)
									r = new RENDEROBJ.RenderDummy(Node.WIDTH, Node.HEIGHT);
								nodes[sy+y][sx+x] = r;
								
							}
						}
						return sx + w;
						
					}
				}
			}
			LOG.ln("nay" + " " + w + " " + h + " " + nodes[0].length + " "+ nodes.length);
			return 0;
		}
		
	}
	
	private static class SubGroup {
		
		private final LinkedList<Node> nodes = new LinkedList<>();
		private final boolean[] contains = new boolean[TECHS.ALL().size()];
		
		SubGroup(LinkedList<SubGroup> others, Node n){

			
			for (SubGroup g : others) {
				if (g.contains[n.tech.index()]) {
					g.nodes.add(n);
					return;
				}
			}
			
			for (TechRequirement r : n.tech.requires()) {
				for (SubGroup g : others) {
					if (g.contains[r.tech.index()]) {
						g.contains[n.tech.index()] = true;
						g.nodes.add(n);
						return;
					}
				}
			}
			
			nodes.add(n);
			contains[n.tech.index()] = true;
			for (TechRequirement r : n.tech.requires()) {
				if (r.level > 0)
					contains[r.tech.index()] = true;
			}
			others.add(this);
		}
		
	}
	
	
	private static class Tree {
		
		final RENDEROBJ[][] nodes;
		
		Tree(SubGroup group){
			LinkedList<TreeNode> tnodes = new LinkedList<>();
			
			for (Node n : group.nodes) {
				tnodes.add(new TreeNode(n, group));
			}
			
			int h = 0;
			
			for (TreeNode n : tnodes) {
				h = Math.max(n.level+1, h);
			}
			
			int[] ws = new int[h];
			int w = 0;
			
			for (TreeNode n : tnodes) {
				ws[n.level] ++;
				w = Math.max(ws[n.level], w);
			}
			
			nodes = new RENDEROBJ[h][w];
			
			for (TreeNode n : tnodes) {
				add(n, ws[n.level]);
			}

			
		}
		
		private void add(TreeNode n, int wi) {
			
			int x1 = (nodes[n.level].length - wi)/2;
			
			while(nodes[n.level][x1] != null)
				x1++;
			
			nodes[n.level][x1] = n.node;
	
			
		}
		
		private static class TreeNode {
			
			private final int level;
			private final Node node;
			
			TreeNode(Node node, SubGroup nodes){
				
				boolean checked[] = new boolean[TECHS.ALL().size()];
				level = level(node.tech, 0, nodes.contains, checked);
				this.node = node;
			}
			
			private int level(TECH t, int level, boolean[] inGroup, boolean[] checked) {
				if (t.requires().size() == 0)
					return level;
				else {
					for (int ri = 0; ri < t.requires().size(); ri++) {
						if (!inGroup[t.requires().get(ri).tech.index()] || checked[t.requires().get(ri).tech.index()])
							continue;
						checked[t.requires().get(ri).tech.index()] = true;
						if (t.requires().get(ri).level > 0)
							level = Math.max(level(t.requires().get(ri).tech, level+1, inGroup, checked), level);
					}
					return level;
				}
			}
			
		}
		
	}
	
}
