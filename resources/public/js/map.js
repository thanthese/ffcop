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
      "States",
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

  // wfs
  items.push({
    xtype: "grid",
    ref: "featureGrid",
    title: "Feature Table",
    region: "south",
    height: 150,
    sm: new GeoExt.grid.FeatureSelectionModel(),
    store: new GeoExt.data.FeatureStore({
      fields: [{ name: "owner", type: "string" },
               { name: "agency", type: "string" },
               { name: "name", type: "string" },
               { name: "usage", type: "string" },
               { name: "parktype", type: "string" },
               { name: "number_fac", type: "int" },
               { name: "area", type: "float" },
               { name: "len", type: "float" }],
      proxy: new GeoExt.data.ProtocolProxy({
        protocol: new OpenLayers.Protocol.WFS({
          url: "/geoserver/ows",
          version: "1.1.0",
          featureType: "parks",
          featureNS: "http://medford.opengeo.org",
          srsName: "EPSG:4326" }) }),
      autoLoad: true
    }),
    columns: [{ header: "owner", dataIndex: "owner" },
              { header: "agency", dataIndex: "agency" },
              { header: "name", dataIndex: "name" },
              { header: "usage", dataIndex: "usage" },
              { header: "parktype", dataIndex: "parktype" },
              { xtype: "numbercolumn", header: "number_fac", dataIndex: "number_fac" },
              { xtype: "numbercolumn", header: "area", dataIndex: "area" },
              { xtype: "numbercolumn", header: "len", dataIndex: "len" }],
    bbar: []
  });

  var rawAttributeData;
  var read = OpenLayers.Format.WFSDescribeFeatureType.prototype.read;
  OpenLayers.Format.WFSDescribeFeatureType.prototype.read = function() {
    rawAttributeData = read.apply(this, arguments);
    return rawAttributeData;
  };

  function reconfigure(store, url) {
    var fields = [],
      columns = [],
      geometryName, geometryType;
    // regular expression to detect the geometry column
    var geomRegex = /gml:(Multi)?(Point|Line|Polygon|Surface|Geometry).*/;
    var types = {
      // mapping of xml schema data types to Ext JS data types
      "xsd:int": "int",
      "xsd:short": "int",
      "xsd:long": "int",
      "xsd:string": "string",
      "xsd:dateTime": "string",
      "xsd:double": "float",
      "xsd:decimal": "float",
      // mapping of geometry types
      "Line": "Path",
      "Surface": "Polygon"
    };
    store.each(function(rec) {
      var type = rec.get("type");
      var name = rec.get("name");
      var match = geomRegex.exec(type);
      if (match) {
        // we found the geometry column
        geometryName = name;
      } else {
        // we have an attribute column
        fields.push({
          name: name,
          type: types[type]
        });
        columns.push({
          xtype: types[type] == "string" ? "gridcolumn" : "numbercolumn",
          dataIndex: name,
          header: name
        });
      }
    });
    app.featureGrid.reconfigure(new GeoExt.data.FeatureStore({
      autoLoad: true,
      proxy: new GeoExt.data.ProtocolProxy({
        protocol: new OpenLayers.Protocol.WFS({
          url: url,
          version: "1.1.0",
          featureType: rawAttributeData.featureTypes[0].typeName,
          featureNS: rawAttributeData.targetNamespace,
          srsName: "EPSG:4326",
          geometryName: geometryName,
          maxFeatures: 250
        })
      }),
      fields: fields
    }), new Ext.grid.ColumnModel(columns));
    app.featureGrid.store.bind(vectorLayer);
    app.featureGrid.getSelectionModel().bind(vectorLayer);
  }

  controls.push(
    new OpenLayers.Control.Navigation(),
    new OpenLayers.Control.Attribution(),
    new OpenLayers.Control.PanPanel(),
    new OpenLayers.Control.ZoomPanel()
  );

  function setLayer(model, node) {
    if (!node || node.layer instanceof OpenLayers.Layer.Vector) {
      return;
    }
    vectorLayer.removeAllFeatures();
    app.featureGrid.reconfigure(
    new Ext.data.Store(), new Ext.grid.ColumnModel([]));
    var layer = node.layer;
    var url = layer.url.split("?")[0]; // the base url without params
    var schema = new GeoExt.data.AttributeStore({
      url: url,
      // request specific params
      baseParams: {
        "SERVICE": "WFS",
        "REQUEST": "DescribeFeatureType",
        "VERSION": "1.1.0",
        "TYPENAME": layer.params.LAYERS
      },
      autoLoad: true,
      listeners: {
        "load": function(store) {
          app.featureGrid.setTitle(layer.name);
          reconfigure(store, url);
        }
      }
    });
  }

  app = new Ext.Viewport({
    layout: "border",
    items: items});

  app.tree.getSelectionModel().on("selectionchange", setLayer);
});

var vectorLayer = new OpenLayers.Layer.Vector("Editable features");
Ext.onReady(function() {
  app.mapPanel.map.addLayer(vectorLayer);
  app.featureGrid.store.bind(vectorLayer);
  app.featureGrid.getSelectionModel().bind(vectorLayer);
});
