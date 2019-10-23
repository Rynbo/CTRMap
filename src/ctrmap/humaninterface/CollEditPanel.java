package ctrmap.humaninterface;

import ctrmap.CtrmapMainframe;
import ctrmap.Utils;
import ctrmap.formats.Triangle;
import ctrmap.formats.containers.GR;
import ctrmap.formats.gfcollision.GRCollisionFile;
import ctrmap.formats.h3d.H3DModelNameGet;
import ctrmap.humaninterface.tools.AbstractTool;
import ctrmap.humaninterface.tools.FillTool;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Simple vertex editor.
 */
public class CollEditPanel extends javax.swing.JPanel {

	private DefaultMutableTreeNode root = new DefaultMutableTreeNode("No map loaded");
	public List<GRCollisionFile> files = new ArrayList<>();
	private List<DefaultMutableTreeNode> meshContainers = new ArrayList<>();
	private List<DefaultMutableTreeNode[]> meshNodes = new ArrayList<>();
	public GRCollisionFile coll;
	private int collIndex = -1;
	public int selectedMesh = -1;
	public int selectedTri = -1;

	/**
	 * Creates new form CollEditPanel
	 */
	public CollEditPanel() {
		initComponents();
		meshTree.addTreeSelectionListener((TreeSelectionEvent e) -> {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) meshTree.getLastSelectedPathComponent();
			if (node == null) {
				deselectTri();
				deselectMesh();
			} else {
				String label = (String) node.getUserObject();
				if (label.startsWith("Mesh")) {
					selectMesh(node.getParent().getIndex(node));
				} else if (label.startsWith("Triangle")) {
					int parentMesh = meshContainers.get(collIndex).getIndex(node.getParent());
					selectTriangle(parentMesh, node.getParent().getIndex(node));
				} else if (root != node) {
					deselectTri();
					deselectMesh();
					selectFile(root.getIndex(node));
				}
			}
		});
		v1x.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				adjustX(v1x, 0);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				adjustX(v1x, 0);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		v1y.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				adjustY(v1y, 0);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				adjustY(v1y, 0);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		v1z.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				adjustZ(v1z, 0);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				adjustZ(v1z, 0);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		v2x.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				adjustX(v2x, 1);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				adjustX(v2x, 1);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		v2y.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				adjustY(v2y, 1);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				adjustY(v2y, 1);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		v2z.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				adjustZ(v2z, 1);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				adjustZ(v2z, 1);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		v3x.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				adjustX(v3x, 2);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				adjustX(v3x, 2);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		v3y.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				adjustY(v3y, 2);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				adjustY(v3y, 2);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
		v3z.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				adjustZ(v3z, 2);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				adjustZ(v3z, 2);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
	}

	public void unload() {
		selectedMesh = -1;
		selectedTri = -1;
		meshTree.setModel(null);
		coll = null;
		root = new DefaultMutableTreeNode("No map loaded");
		meshNodes.clear();
		meshContainers.clear();
		files.clear();
	}

	public void loadCollision(GR f) {
		loadCollision(new GRCollisionFile(f), H3DModelNameGet.H3DModelNameGet(f.getFile(1)));
	}

	public void store() {
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i).modified) {
				files.get(i).write();
			}
		}
		buildTree();
	}

	public void loadCollision(GRCollisionFile f, String name) {
		this.coll = f;
		root.setUserObject("Collision data");
		files.add(coll);
		meshTree.setModel(new DefaultTreeModel(root));
		DefaultMutableTreeNode[] newNode = new DefaultMutableTreeNode[16];
		DefaultMutableTreeNode cont = new DefaultMutableTreeNode(name);
		for (int i = 0; i < 16; i++) {
			newNode[i] = new DefaultMutableTreeNode("Mesh " + String.valueOf(i));
			for (int j = 0; j < coll.meshes[i].tris.size(); j++) {
				newNode[i].add(new DefaultMutableTreeNode("Triangle " + String.valueOf(j)));
			}
			cont.add(newNode[i]);
		}
		meshNodes.add(newNode);
		meshContainers.add(cont);
		root.add(cont);
		deselectMesh();
		deselectTri();
		((DefaultTreeModel) meshTree.getModel()).reload();
	}

	public void selectFile(int idx) {
		coll = files.get(idx);
		collIndex = idx;
	}

	public void selectTriangle(int mesh, int tri) {
		deselectTri();
		deselectMesh();
		if (mesh == -1 || tri == -1 || tri >= coll.meshes[mesh].tris.size()) {
			return;
		}
		Triangle triobj = coll.meshes[mesh].tris.get(tri);
		v1x.setValue(triobj.getX(0));
		v1y.setValue(triobj.getY(0));
		v1z.setValue(triobj.getZ(0));
		v2x.setValue(triobj.getX(1));
		v2y.setValue(triobj.getY(1));
		v2z.setValue(triobj.getZ(1));
		v3x.setValue(triobj.getX(2));
		v3y.setValue(triobj.getY(2));
		v3z.setValue(triobj.getZ(2));
		triobj.setSelected(true);
		selectedMesh = mesh;
		selectedTri = tri;
	}

	public void deselectTri() {
		if (selectedTri != -1) {
			coll.meshes[selectedMesh].tris.get(selectedTri).setSelected(false);
		}
		selectedTri = -1;
		v1x.setValue(0f);
		v1y.setValue(0f);
		v1z.setValue(0f);
		v2x.setValue(0f);
		v2y.setValue(0f);
		v2z.setValue(0f);
		v3x.setValue(0f);
		v3y.setValue(0f);
		v3z.setValue(0f);
	}

	public void selectMesh(int num) {
		deselectTri();
		deselectMesh();
		for (int i = 0; i < coll.meshes[num].tris.size(); i++) {
			coll.meshes[num].tris.get(i).setSelected(true);
		}
		selectedMesh = num;
	}

	public void deselectMesh() {
		if (selectedMesh != -1) {
			for (int i = 0; i < coll.meshes[selectedMesh].tris.size(); i++) {
				coll.meshes[selectedMesh].tris.get(i).setSelected(false);
			}
		}
		selectedMesh = -1;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        treeScrollPane = new javax.swing.JScrollPane();
        meshTree = new javax.swing.JTree();
        v1label = new javax.swing.JLabel();
        v1xlabel = new javax.swing.JLabel();
        v1ylabel = new javax.swing.JLabel();
        v1zlabel = new javax.swing.JLabel();
        v1x = new javax.swing.JFormattedTextField();
        v1y = new javax.swing.JFormattedTextField();
        v1z = new javax.swing.JFormattedTextField();
        v2label = new javax.swing.JLabel();
        v2xlabel = new javax.swing.JLabel();
        v2ylabel = new javax.swing.JLabel();
        v2zlabel = new javax.swing.JLabel();
        v2x = new javax.swing.JFormattedTextField();
        v2y = new javax.swing.JFormattedTextField();
        v2z = new javax.swing.JFormattedTextField();
        v3label = new javax.swing.JLabel();
        v3xlabel = new javax.swing.JLabel();
        v3ylabel = new javax.swing.JLabel();
        v3zlabel = new javax.swing.JLabel();
        v3x = new javax.swing.JFormattedTextField();
        v3y = new javax.swing.JFormattedTextField();
        v3z = new javax.swing.JFormattedTextField();
        btnNewTriangle = new javax.swing.JButton();
        btnRemTriangle = new javax.swing.JButton();
        btnFillCoords = new javax.swing.JButton();

        treeScrollPane.setAlignmentX(0.0F);
        treeScrollPane.setAlignmentY(0.0F);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("No map loaded");
        meshTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        treeScrollPane.setViewportView(meshTree);

        v1label.setText("Vertex 1:");

        v1xlabel.setText("X:");

        v1ylabel.setText("Y:");

        v1zlabel.setText("Z:");

        v1x.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        v1y.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        v1z.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        v2label.setText("Vertex 2:");

        v2xlabel.setText("X:");

        v2ylabel.setText("Y:");

        v2zlabel.setText("Z:");

        v2x.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        v2y.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        v2z.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        v3label.setText("Vertex 3:");

        v3xlabel.setText("X:");

        v3ylabel.setText("Y:");

        v3zlabel.setText("Z:");

        v3x.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        v3y.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        v3z.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));

        btnNewTriangle.setText("New triangle");
        btnNewTriangle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewTriangleActionPerformed(evt);
            }
        });

        btnRemTriangle.setText("Remove triangle");
        btnRemTriangle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemTriangleActionPerformed(evt);
            }
        });

        btnFillCoords.setText("New quad from fill tool");
        btnFillCoords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFillCoordsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(treeScrollPane)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(v2label)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(v2xlabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(v2x, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(v2ylabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(v2y, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(v2zlabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(v2z, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(v3label)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(v3xlabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnFillCoords, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(v3x, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(v3ylabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(v3y, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(v3zlabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(v3z, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(btnNewTriangle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnRemTriangle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(v1label)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(v1xlabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(v1x, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(v1ylabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(v1y, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(v1zlabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(v1z, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(treeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(v1label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(v1xlabel)
                    .addComponent(v1ylabel)
                    .addComponent(v1zlabel)
                    .addComponent(v1x, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(v1y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(v1z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(v2label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(v2xlabel)
                    .addComponent(v2ylabel)
                    .addComponent(v2zlabel)
                    .addComponent(v2x, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(v2y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(v2z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(v3label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(v3xlabel)
                    .addComponent(v3ylabel)
                    .addComponent(v3zlabel)
                    .addComponent(v3x, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(v3y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(v3z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNewTriangle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemTriangle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFillCoords))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewTriangleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewTriangleActionPerformed
		if (selectedMesh != -1) {
			coll.meshes[selectedMesh].tris.add(new Triangle(new float[3], new float[3], new float[3]));
			int index = meshNodes.get(collIndex)[selectedMesh].getChildCount();
			meshNodes.get(collIndex)[selectedMesh].add(new DefaultMutableTreeNode("Triangle " + index));
			((DefaultTreeModel) meshTree.getModel()).reload(meshNodes.get(collIndex)[selectedMesh]);
			selectTriangle(selectedMesh, index);
		}
		coll.modified = true;
    }//GEN-LAST:event_btnNewTriangleActionPerformed

    private void btnFillCoordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFillCoordsActionPerformed
		AbstractTool tool = CtrmapMainframe.tool;
		if (tool != null && tool instanceof FillTool && selectedMesh != -1) {
			FillTool t = (FillTool) tool;
			if (t.lastX != -1) {
				int startX = (Math.min(t.originX, t.lastX) % 40) * 18 - 360;
				int startY = (Math.min(t.originY, t.lastY) % 40) * 18 - 360;
				int width = Math.abs(t.lastX - t.originX) * 18 + 18;
				int height = Math.abs(t.lastY - t.originY) * 18 + 18;
				float[] vertex0 = new float[]{startX, 0, startY};
				float[] vertex1 = new float[]{startX, 0, startY + height};
				float[] vertex2 = new float[]{startX + width, 0, startY};
				float[] vertex3 = new float[]{startX + width, 0, startY + height};
				coll.meshes[selectedMesh].tris.add(new Triangle(new float[]{vertex0[0], vertex1[0], vertex2[0]}, new float[3], new float[]{vertex0[2], vertex1[2], vertex2[2]}));
				coll.meshes[selectedMesh].tris.add(new Triangle(new float[]{vertex3[0], vertex1[0], vertex2[0]}, new float[3], new float[]{vertex3[2], vertex1[2], vertex2[2]}));
				int index = meshNodes.get(collIndex)[selectedMesh].getChildCount();
				meshNodes.get(collIndex)[selectedMesh].add(new DefaultMutableTreeNode("Triangle " + index));
				meshNodes.get(collIndex)[selectedMesh].add(new DefaultMutableTreeNode("Triangle " + (index + 1)));
				reloadMeshNode(selectedMesh);
				selectTriangle(selectedMesh, index);
			}
		}
		coll.modified = true;
    }//GEN-LAST:event_btnFillCoordsActionPerformed

    private void btnRemTriangleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemTriangleActionPerformed
		int mesh = selectedMesh;
		int tri = selectedTri;
		if (mesh == -1 || tri == -1) {
			return;
		}
		deselectTri();
		coll.meshes[mesh].tris.remove(tri);
		meshNodes.get(collIndex)[mesh].remove(tri);
		reloadMeshNode(mesh);
		coll.modified = true;
    }//GEN-LAST:event_btnRemTriangleActionPerformed

	private void reloadMeshNode(int num) {
		((DefaultTreeModel) meshTree.getModel()).reload(meshNodes.get(collIndex)[num]);
	}

	private void adjustX(JFormattedTextField source, int vertex) {
		if (selectedTri != -1) {
			coll.meshes[selectedMesh].tris.get(selectedTri).setX(vertex, Utils.getFloatFromDocument(source));
			coll.modified = true;
		}
	}

	private void adjustY(JFormattedTextField source, int vertex) {
		if (selectedTri != -1) {
			coll.meshes[selectedMesh].tris.get(selectedTri).setY(vertex, Utils.getFloatFromDocument(source));
			coll.modified = true;
		}
	}

	private void adjustZ(JFormattedTextField source, int vertex) {
		if (selectedTri != -1) {
			coll.meshes[selectedMesh].tris.get(selectedTri).setZ(vertex, Utils.getFloatFromDocument(source));
			coll.modified = true;
		}
	}

	public void buildTree() {
		root.removeAllChildren();
		meshNodes.clear();
		for (int file = 0; file < files.size(); file++) {
			files.get(file).dedupe();
			meshNodes.add(new DefaultMutableTreeNode[16]);
			meshContainers.get(file).removeAllChildren();
			for (int i = 0; i < 16; i++) {
				meshNodes.get(file)[i] = new DefaultMutableTreeNode("Mesh " + String.valueOf(i));
				for (int j = 0; j < files.get(file).meshes[i].tris.size(); j++) {
					meshNodes.get(file)[i].add(new DefaultMutableTreeNode("Triangle " + String.valueOf(j)));
				}
				meshContainers.get(file).add(meshNodes.get(file)[i]);
			}
			root.add(meshContainers.get(file));
		}
		((DefaultTreeModel) meshTree.getModel()).reload();
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFillCoords;
    private javax.swing.JButton btnNewTriangle;
    private javax.swing.JButton btnRemTriangle;
    private javax.swing.JTree meshTree;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JLabel v1label;
    private javax.swing.JFormattedTextField v1x;
    private javax.swing.JLabel v1xlabel;
    private javax.swing.JFormattedTextField v1y;
    private javax.swing.JLabel v1ylabel;
    private javax.swing.JFormattedTextField v1z;
    private javax.swing.JLabel v1zlabel;
    private javax.swing.JLabel v2label;
    private javax.swing.JFormattedTextField v2x;
    private javax.swing.JLabel v2xlabel;
    private javax.swing.JFormattedTextField v2y;
    private javax.swing.JLabel v2ylabel;
    private javax.swing.JFormattedTextField v2z;
    private javax.swing.JLabel v2zlabel;
    private javax.swing.JLabel v3label;
    private javax.swing.JFormattedTextField v3x;
    private javax.swing.JLabel v3xlabel;
    private javax.swing.JFormattedTextField v3y;
    private javax.swing.JLabel v3ylabel;
    private javax.swing.JFormattedTextField v3z;
    private javax.swing.JLabel v3zlabel;
    // End of variables declaration//GEN-END:variables
}
