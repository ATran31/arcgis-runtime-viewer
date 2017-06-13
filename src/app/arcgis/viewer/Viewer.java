package app.arcgis.viewer;

import java.awt.EventQueue;
import java.awt.Font;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.esri.runtime.ArcGISRuntime;
import com.esri.toolkit.legend.JLegend;
import com.esri.toolkit.overlays.ScaleBarOverlay;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.map.ArcGISTiledMapServiceLayer;
import com.esri.map.FeatureLayer;
import com.esri.map.JMap;
import com.esri.map.Layer;
import com.esri.map.LayerList;
import com.esri.map.MapEvent;
import com.esri.map.MapEventListener;
import com.esri.map.MapOptions;
import com.esri.map.MapOptions.MapType;

public class Viewer {
	
	/**** MEMBERS ****/
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
			}
			@Override
			public void mapDispose(MapEvent event) {
			}
		});
	}

	/*** METHODS ***/
	
	/**
	 * Toggles the table of contents to display layers currently available in the map.
	 */
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
	
	/**
	 * Loads the main toolbar containing primary buttons and menus
	 */
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
		final JButton geocodeBtn = new JButton(new ImageIcon("icons32/png/plot.png"));
		geocodeBtn.setToolTipText("Geocoding");
		final JButton routeBtn = new JButton(new ImageIcon("icons32/png/route.png"));
		routeBtn.setToolTipText("Routing");
		
		// add buttons to toolbar
		toolBar.add(openFileBtn);
		toolBar.add(zInBtn);
		toolBar.add(zOutBtn);
		toolBar.add(fullExtBtn);
		toolBar.add(toggleLyrListBtn);
		toolBar.add(geocodeBtn);
		toolBar.add(routeBtn);
		
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
		geocodeBtn.addMouseListener(new MouseAdapter() {    	
			public void mouseClicked(MouseEvent evt) { 
				final JFrame geocodeForm = new JFrame();
				geocodeForm.setTitle("Geocoding");
				geocodeForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				
				final JPanel mainPanel = new JPanel();
				mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
				
				JTextArea inputParams = new JTextArea(15,30);
				JScrollPane scrollPane = new JScrollPane(inputParams);
				
				JRadioButton addressOpt = new JRadioButton("Address Search");
				addressOpt.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e){
						inputParams.setText("Enter one address per line...");
					}
				});
				JRadioButton coordOpt = new JRadioButton("Coordinate Search");
				coordOpt.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						inputParams.setText("Enter one comma separated lat,lon pair per line...");
						
					}
				});
				ButtonGroup gcOptions = new ButtonGroup();
				gcOptions.add(addressOpt);
				gcOptions.add(coordOpt);
				
				
				JButton runBtn = new JButton("Run");
				
				mainPanel.add(addressOpt);
				mainPanel.add(coordOpt);
				mainPanel.add(scrollPane);
				mainPanel.add(runBtn);
				
			    geocodeForm.add(mainPanel);
			    geocodeForm.pack();
			    geocodeForm.setLocationRelativeTo(null);
			    geocodeForm.setVisible(true);
			}
		});
		routeBtn.addMouseListener(new MouseAdapter() {    	
			public void mouseClicked(MouseEvent evt) { 
				final JFrame gpRoutingForm = new JFrame();
				gpRoutingForm.setTitle("Direction Routing");
				gpRoutingForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				final JPanel mainPanel = new JPanel();
				mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
				JLabel tempMsg = new JLabel("Routing feature has not been implemented.");
				mainPanel.add(tempMsg);
			    gpRoutingForm.add(mainPanel);
			    gpRoutingForm.pack();
			    gpRoutingForm.setLocationRelativeTo(null);
			    gpRoutingForm.setVisible(true);
			}
		});
		
		// add menu bar
		JMenuBar menuBar = new JMenuBar();
		
		// add basemap menu
		JMenu basemapMenu = new JMenu("Basemap");
		basemapMenu.setMinimumSize(new Dimension(64,32));
		basemapMenu.setBorder(BorderFactory.createRaisedBevelBorder());
		
		JMenuItem osm = new JMenuItem("Open Street Map");
		osm.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				MapOptions o = new MapOptions(MapType.OSM);
				map.getLayers().remove(0);
				map.setMapOptions(o);
			}
		});
		
		JMenuItem esriWorldTopo = new JMenuItem("World Topo Map");
		esriWorldTopo.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				MapOptions o = new MapOptions(MapType.TOPO);
				map.getLayers().remove(0);
				map.setMapOptions(o);
			}
		});
		
		JMenuItem esriWorldStreet = new JMenuItem("World Street Map");
		esriWorldStreet.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				MapOptions o = new MapOptions(MapType.STREETS);
				map.getLayers().remove(0);
				map.setMapOptions(o);
			}
		});
			
		JMenuItem esriWorldImagery = new JMenuItem("World Imagery");
		esriWorldImagery.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				MapOptions o = new MapOptions(MapType.SATELLITE);
				map.getLayers().remove(0);
				map.setMapOptions(o);
			}
		});
		
		JMenuItem esriOceanBasemap = new JMenuItem("Ocean Basemap");
		esriOceanBasemap.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				MapOptions o = new MapOptions(MapType.OCEANS);
				map.getLayers().remove(0);
				map.setMapOptions(o);
			}
		});
		
		JMenuItem esriNatGeoWorld = new JMenuItem("National Geographic World Map");
		esriNatGeoWorld.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				MapOptions o = new MapOptions(MapType.NATIONAL_GEOGRAPHIC);
				map.getLayers().remove(0);
				map.setMapOptions(o);
			}
		});
		
		JMenuItem esriWorldGrayCanvas = new JMenuItem("World Gray Canvas");
		esriWorldGrayCanvas.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				MapOptions o = new MapOptions(MapType.GRAY_BASE);
				map.getLayers().remove(0);
				map.setMapOptions(o);
			}
		});

		JMenuItem esriWorldTerrain = new JMenuItem("World Terrain");
		esriWorldTerrain.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/arcgis/rest/services/World_Terrain_Base/MapServer");
				map.getLayers().remove(0);
				map.getLayers().add(0, tiledLayer);
				}
		});
		
		JMenuItem esriWorldShadedRelief = new JMenuItem("World Shaded Relief");
		esriWorldShadedRelief.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/arcgis/rest/services/World_Shaded_Relief/MapServer");
				map.getLayers().remove(0);
				map.getLayers().add(0, tiledLayer);	
			}
		});
		
		JMenuItem esriWorldPhysical = new JMenuItem("World Physical");
		esriWorldPhysical.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/arcgis/rest/services/World_Physical_Map/MapServer");
				map.getLayers().remove(0);
				map.getLayers().add(0, tiledLayer);		
			}
		});
		
		JMenu esriSpecialtySubMenu = new JMenu("Specialty Maps");
		
		JMenuItem esriDelorne = new JMenuItem("Delorne");
		esriDelorne.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/arcgis/rest/services/Specialty/DeLorme_World_Base_Map/MapServer");
				map.getLayers().remove(0);
				map.getLayers().add(0, tiledLayer);
			}
		});
		
		JMenuItem esriSoilSurvey = new JMenuItem("World Soil Survey");
		esriSoilSurvey.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/arcgis/rest/services/Specialty/Soil_Survey_Map/MapServer");
				map.getLayers().remove(0);
				map.getLayers().add(0, tiledLayer);
			}
		});
		
		JMenuItem esriNavChart = new JMenuItem("World Navigation Chart");
		esriNavChart.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/arcgis/rest/services/Specialty/World_Navigation_Charts/MapServer");
				map.getLayers().remove(0);
				map.getLayers().add(0, tiledLayer);
			}
		});
		
		JMenu referenceLayers = new JMenu("Reference Layers");
		
		String[] refLyrs = {"World Boundaries & Places Reference", "World Boundaries & Places Reference Alt.", "World Reference Overlay", "World Transportation Reference"};
		
		JMenuItem esriRefWorldPlacesBoundaries = new JMenuItem("World Boundaries & Places");
		esriRefWorldPlacesBoundaries.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/arcgis/rest/services/Reference/World_Boundaries_and_Places/MapServer");
				tiledLayer.setName("World Boundaries & Places Reference");
				LayerList L = map.getLayers();
				for (int i = 0; i < L.size(); i++){
					if (Arrays.asList(refLyrs).contains(L.get(i).getName())){
						L.remove(i);
					}
				}
				L.add(1, tiledLayer);
			}
		});
		
		JMenuItem esriRefWorldPlacesBoundariesAlt = new JMenuItem("World Boundaries & Places Alt.");
		esriRefWorldPlacesBoundariesAlt.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/arcgis/rest/services/Reference/World_Boundaries_and_Places_Alternate/MapServer");
				tiledLayer.setName("World Boundaries & Places Reference Alt.");
				LayerList L = map.getLayers();
				for (int i = 0; i < L.size(); i++){
					if (Arrays.asList(refLyrs).contains(L.get(i).getName())){
						L.remove(i);
					}
				}
				L.add(1, tiledLayer);
			}
		});

		JMenuItem esriRefWorldOverlay = new JMenuItem("World Reference Overlay");
		esriRefWorldOverlay.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/arcgis/rest/services/Reference/World_Reference_Overlay/MapServer");
				tiledLayer.setName("World Reference Overlay");
				LayerList L = map.getLayers();
				for (int i = 0; i < L.size(); i++){
					if (Arrays.asList(refLyrs).contains(L.get(i).getName())){
						L.remove(i);
					}
				}
				L.add(1, tiledLayer);
			}
		});
		
		JMenuItem esriRefWorldTransportation = new JMenuItem("World Transportation");
		esriRefWorldTransportation.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/arcgis/rest/services/Reference/World_Transportation/MapServer");
				tiledLayer.setName("World Transportation Reference");
				LayerList L = map.getLayers();
				for (int i = 0; i < L.size(); i++){
					if (Arrays.asList(refLyrs).contains(L.get(i).getName())){
						L.remove(i);
					}
				}
				L.add(1, tiledLayer);
			}
		});
		
		basemapMenu.add(osm);
		basemapMenu.add(esriWorldGrayCanvas);
		basemapMenu.add(esriWorldStreet);
		basemapMenu.add(esriWorldImagery);
		basemapMenu.add(esriOceanBasemap);
		basemapMenu.add(esriNatGeoWorld);
		basemapMenu.add(esriWorldTopo);
		basemapMenu.add(esriWorldTerrain);
		basemapMenu.add(esriWorldShadedRelief);
		basemapMenu.add(esriWorldPhysical);
		basemapMenu.add(esriSpecialtySubMenu);
			esriSpecialtySubMenu.add(esriDelorne);
			esriSpecialtySubMenu.add(esriSoilSurvey);
			esriSpecialtySubMenu.add(esriNavChart);
		basemapMenu.add(referenceLayers);
			referenceLayers.add(esriRefWorldPlacesBoundaries);
			referenceLayers.add(esriRefWorldPlacesBoundariesAlt);
			referenceLayers.add(esriRefWorldOverlay);
			referenceLayers.add(esriRefWorldTransportation);

		// add geoprocessing menu
		JMenu gpMenu = new JMenu("Geoprocessing");
		gpMenu.setMinimumSize(new Dimension(64, 32));
		gpMenu.setBorder(BorderFactory.createRaisedBevelBorder());
	
		JMenuItem gpBuffer = new JMenuItem("Buffer");
		gpBuffer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JFrame gpBufferForm = new JFrame();
				gpBufferForm.setTitle("Geoprocessing - Buffer");
				gpBufferForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				final JPanel mainPanel = new JPanel();
				mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
				JLabel tempMsg = new JLabel("Buffer feature has not been implemented.");
				mainPanel.add(tempMsg);
			    gpBufferForm.add(mainPanel);
			    gpBufferForm.pack();
			    gpBufferForm.setLocationRelativeTo(null);
			    gpBufferForm.setVisible(true);

			}
		});
		
		JMenuItem gpClip = new JMenuItem("Clip");
		gpClip.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JFrame gpClipForm = new JFrame();
				gpClipForm.setTitle("Geoprocessing - Clip");
				gpClipForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				final JPanel mainPanel = new JPanel();
				mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
				JLabel tempMsg = new JLabel("Clip feature has not been implemented.");
				mainPanel.add(tempMsg);
			    gpClipForm.add(mainPanel);
			    gpClipForm.pack();
			    gpClipForm.setLocationRelativeTo(null);
			    gpClipForm.setVisible(true);

			}
		});
		
		gpMenu.add(gpBuffer);
		gpMenu.add(gpClip);
		
		// add menus to menu bar
		menuBar.add(basemapMenu);
		menuBar.add(gpMenu);
		
		// add menu bar to main toolbar
		toolBar.add(menuBar);
		
		// insert into UI
		window.add(toolBar, BorderLayout.PAGE_START);
	}

	/**
	 * Launches a JFileChooser to allow users to select a file. Filtered for .shp files.
	 */
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
	
	/**
	 * zoom the map in by a factor of 2
	 */
	private void zoomIn(){
		map.zoom(0.5);
	}

	/**
	 * zoom the map out by a factor of 2
	 */
	private void zoomOut(){
		map.zoom(2);
	}

	/**
	 * zoom the map to the full extent. current sent to CONUS.
	 */
	private void zoomFullExt(){
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
