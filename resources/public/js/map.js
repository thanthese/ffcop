Ext.onReady(function() {
  var map = new OpenLayers.Map();
  var layer = new OpenLayers.Layer.WMS(
    "Global Imagery",
    "http://maps.opengeo.org/geowebcache/service/wms",
    {layers: "bluemarble"}
    );
  map.addLayer(layer);

  new GeoExt.MapPanel({
    renderTo: 'gxmap',
      height: 400,
      width: 600,
      map: map,
      title: 'A Simple GeoExt Map'
  });
});
