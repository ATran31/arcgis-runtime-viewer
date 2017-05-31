package app.arcgis.viewer;

import java.awt.EventQueue;
import java.awt.Font;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.esri.runtime.ArcGISRuntime;
import com.esri.toolkit.legend.JLegend;
import com.esri.toolkit.overlays.ScaleBarOverlay;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.map.FeatureLayer;
import com.esri.map.JMap;
import com.esri.map.Layer;
import com.esri.map.MapEvent;
import com.esri.map.MapEventListener;
import com.esri.map.MapOptions;
import com.esri.map.MapOptions.MapType;

public class Viewer {

	private JFrame window;
	private JMap map;
	private static boolean layerListVisible;
	private static JPanel layerList;

	/**** CONSTRUCTOR ****/
	public Viewer() {
		window = new JFrame();
		window.setTitle("Map Viewer Basic");
		window.setSize(800, 600);
		window.setLocationRelativeTo(null); // center on screen
		window.setExtendedState(JFrame.MAXIMIZED_BOTH); // default full screen
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(new BorderLayout(0, 0));

		// dispose map just before application window is closed.
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				super.windowClosing(windowEvent);
				map.dispose();
			}
		});

		// Before this application is deployed you must register the application on 
		// http://developers.arcgis.com and set the Client ID in the application as shown 
		// below. This will license your application to use Basic level functionality.
		// 
		// If you need to license your application for Standard level functionality, please 
		// refer to the documentation on http://developers.arcgis.com
		//
		//ArcGISRuntime.setClientID("your Client ID");

		// Using MapOptions allows for a common online basemap to be chosen
		MapOptions mapOptions = new MapOptions(MapType.TOPO);
		map = new JMap(mapOptions);

		// If you don't use MapOptions, use the empty JMap constructor and add a tiled layer
		//map = new JMap();
		//ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
		//  "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
		//map.getLayers().add(tiledLayer);

		// add the main toolbar
		loadMainToolbar();

		// create and add a scale bar to the map
		ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay();
		map.addMapOverlay(scaleBarOverlay);

		// Add the JMap to the JFrame's content pane
		window.getContentPane().add(map);
		// add map event listeners
		map.addMapEventListener(new MapEventListener() {
			@Override
			public void mapReady(MapEvent event) {
				// zoom to continental US on ready
				map.zoomTo(37.1669, -95.9669, 3);
			}
			@Override
			public void mapExtentChanged(MapEvent event) {
				// TODO Auto-generated method stub
			}
			@Override
			public void mapDispose(MapEvent event) {
				// TODO Auto-generated method stub
			}
		});
	}

	/**** PRIVATE METHODS ****/
	private void toggleLayerList(){
		if (layerListVisible == false){
			// create a legend
			JLegend maplegend = new JLegend(map);
			maplegend.setPreferredSize(new Dimension(200, 200));
			maplegend.setBorder(new LineBorder(new Color(255, 255, 255), 2));

			// create a JLabel for the JPanel that holds the overview map
			JLabel panelLabel = new JLabel("Layers");
			panelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			panelLabel.setFont(new Font("Dialog", Font.BOLD, 14));
			panelLabel.setBackground(Color.white);
			panelLabel.setForeground(Color.black);

			// add a JPanel to hold the overview map
			layerList = new JPanel();
			layerList.setLayout(new BoxLayout(layerList, BoxLayout.Y_AXIS));
			layerList.setSize(200, 200);
			layerList.setBackground(Color.WHITE);
			layerList.setBorder(new LineBorder(Color.BLACK, 1));
			layerList.add(panelLabel);
			layerList.add(maplegend);

			// add to layout
			window.add(layerList, BorderLayout.WEST);
			window.revalidate();

			// update layerListVisible state
			layerListVisible = true;
		} else {
			window.remove(layerList);
			window.revalidate();
			layerListVisible = false;
		}
	}

	private void loadMainToolbar(){
		// init toolbar
		final JToolBar toolBar = new JToolBar("Main Tool Bar");
		toolBar.setPreferredSize(new Dimension(450, 40));
		// init toolbar buttons
		// button icons are used with permission from Flaticons
		final JButton openFileBtn = new JButton(new ImageIcon("icons32/png/folder.png"));
		openFileBtn.setToolTipText("Open Shapefile");
		final JButton zOutBtn = new JButton(new ImageIcon("icons32/png/zoom-out.png"));
		zOutBtn.setToolTipText("Zoom out");
		final JButton zInBtn = new JButton(new ImageIcon("icons32/png/zoom-in.png"));
		zInBtn.setToolTipText("Zoom in");
		final JButton fullExtBtn= new JButton(new ImageIcon("icons32/png/full-ext.png"));
		fullExtBtn.setToolTipText("Zoom to Full Extent");
		final JButton toggleLyrListBtn = new JButton(new ImageIcon("icons32/png/layers.png"));
		toggleLyrListBtn.setToolTipText("Layers");
		// add buttons to toolbar
		toolBar.add(openFileBtn);
		toolBar.add(zInBtn);
		toolBar.add(zOutBtn);
		toolBar.add(fullExtBtn);
		toolBar.add(toggleLyrListBtn);
		// set event listeners for buttons on click
		openFileBtn.addMouseListener(new MouseAdapter() {    	
			public void mouseClicked(MouseEvent evt) { 
				loadShpFile(); 
			}
		});
		zInBtn.addMouseListener(new MouseAdapter() {    	
			public void mouseClicked(MouseEvent evt) { 
				zoomIn();
			}
		});
		zOutBtn.addMouseListener(new MouseAdapter() {    	
			public void mouseClicked(MouseEvent evt) { 
				zoomOut();
			}
		});
		fullExtBtn.addMouseListener(new MouseAdapter() {    	
			public void mouseClicked(MouseEvent evt) { 
				zoomFullExt(); 
			}
		});
		toggleLyrListBtn.addMouseListener(new MouseAdapter() {    	
			public void mouseClicked(MouseEvent evt) { 
				toggleLayerList();
			}
		});
		// insert into UI
		window.add(toolBar, BorderLayout.PAGE_START);
	}

	private void loadShpFile(){
		// load files filtered for .shp extentsion
		// generate an error popup if spatial refrence does not match map spatial refrence
		// get file with GUI selector
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter(".shp","shp"));
		int returnVal = fc.showOpenDialog(null);
		String filePath = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			filePath = file.getAbsolutePath();
		}
		try {
			ShapefileFeatureTable myShape = new ShapefileFeatureTable(filePath);
			String geoType = myShape.getGeometryType().toString();
			if (myShape.getSpatialReference().getID() == map.getSpatialReference().getLatestID()){
				FeatureLayer myFeature=new FeatureLayer(myShape);
				SimpleRenderer myRend = null;
				if (geoType == "POINT"){
					BufferedImage img = null;
					try {
						img = ImageIO.read(new File("icons32/png/red-marker.png"));
					} catch (IOException e) {
					}
					PictureMarkerSymbol marker = new PictureMarkerSymbol(img);
					myRend = new SimpleRenderer(marker);
					myFeature.setRenderer(myRend);
				} else if (geoType == "LINE" || geoType == "POLYLINE") {
					SimpleLineSymbol myLine = new SimpleLineSymbol(Color.RED, (float) 6.0);
					myRend = new SimpleRenderer(myLine);
					myFeature.setRenderer(myRend);
				} else if (geoType == "POLYGON"){
					SimpleFillSymbol myFill=new SimpleFillSymbol(Color.RED);
					myRend = new SimpleRenderer(myFill);
					myFeature.setRenderer(myRend);
				} else {
					Component frame = null;
					JOptionPane.showMessageDialog(frame,
							"Geometry type is currently not supported.",
							geoType, JOptionPane.WARNING_MESSAGE);
				}
				map.getLayers().add(myFeature);
			} else {
				// inform user file spatial refrence does not match that used by the map
				Component frame = null;
				JOptionPane.showMessageDialog(frame,
						"The selected shapefile's spatial refrence WKID " + myShape.getSpatialReference().getID()
						+ "\ndoes not match the map spatial reference WKID " + map.getSpatialReference().getLatestID(),
						"Spatial Reference Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void zoomIn(){
		// zoom the map in by a factor of 2
		map.zoom(0.5);
	}

	private void zoomOut(){
		// zoom the map out by a factor of 2
		map.zoom(2);
	}

	private void zoomFullExt(){
		// zoom to full extent of the layer running on ArcGIS Server
		map.zoomTo(map.getFullExtent());
	}

	/**
	 * Starting point of this application.
	 * @param args
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					Viewer application = new Viewer();
					application.window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
