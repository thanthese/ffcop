Ext.BLANK_IMAGE_URL = "ext-3.4.0/resources/images/default/s.gif";
var app, items = [], controls = [];

Ext.onReady(function() {
  items.push({
    xtype: "gx_mappanel",
    ref: "mapPanel",
    region: "center",
    map: {
      numZoomLevels: 6,
      controls: controls },
    extent: OpenLayers.Bounds.fromArray([
      -122.911, 42.291,
      -122.787,42.398 ]),
    layers: [new OpenLayers.Layer.WMS(
      "Medford",
      "http://localhost/geoserver/wms",
      {layers: "topp:states"},
      {isBaseLayer: false})]
  });

  items.push({
    xtype: "grid",
    ref: "capsGrid", // makes the grid available as app.capsGrid
    title: "New and Better Available Layers",
    region: "north",
    height: 150,
    viewConfig: {forceFit: true},
    store: new GeoExt.data.WMSCapabilitiesStore({
      url: "http://localhost/geoserver/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1",
      autoLoad: true}),
    columns: [
      {header: "Name", dataIndex: "name", sortable: true},
      {header: "Title", dataIndex: "title", sortable: true},
      {header: "Abstract", dataIndex: "abstract"}],
    bbar: [{
      text: "Add to Map",
      handler: function() {
        app.capsGrid.getSelectionModel().each(function(record) {
          var clone = record.clone();
          clone.getLayer().mergeNewParams({
            format: "image/png",
            transparent: true });
          app.mapPanel.layers.add(clone);
          app.mapPanel.map.zoomToExtent(
            OpenLayers.Bounds.fromArray(clone.get("llbbox")));});}}]});

  items.push({
    xtype: "treepanel",
    ref: "tree",
    region: "west",
    width: 200,
    autoScroll: true,
    enableDD: true,
    root: new GeoExt.tree.LayerContainer({expanded: true}),
    bbar: [{
      text: "Remove from Map",
      handler: function() {
        var node = app.tree.getSelectionModel().getSelectedNode();
        if (node && node.layer instanceof OpenLayers.Layer.WMS) {
          app.mapPanel.map.removeLayer(node.layer);}}}]});

  items.push({
    xtype: "gx_legendpanel",
    region: "east",
    width: 200,
    autoScroll: true,
    padding: 5});

  controls.push(new OpenLayers.Control.WMSGetFeatureInfo({
    autoActivate: true,
    infoFormat: "application/vnd.ogc.gml",
    maxFeatures: 3,
    eventListeners: {
      "getfeatureinfo": function(e) {
        var items = [];
        Ext.each(e.features, function(feature) {
          items.push({
            xtype: "propertygrid",
            title: feature.fid,
            source: feature.attributes
          });
        });
        new GeoExt.Popup({
          title: "Feature Info",
          width: 200,
          height: 200,
          layout: "accordion",
          map: app.mapPanel,
          location: e.xy,
          items: items
        }).show();
      }
    }
  }));

  controls.push(
    new OpenLayers.Control.Navigation(),
    new OpenLayers.Control.Attribution(),
    new OpenLayers.Control.PanPanel(),
    new OpenLayers.Control.ZoomPanel()
  );

  app = new Ext.Viewport({
    layout: "border",
    items: items});
});
