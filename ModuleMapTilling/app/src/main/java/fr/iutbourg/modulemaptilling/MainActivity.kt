package fr.iutbourg.modulemaptilling

import android.app.PendingIntent.getActivity
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MainActivity : AppCompatActivity() {

    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var compassOverlay: CompassOverlay
    private lateinit var gestureOverlay: RotationGestureOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        setContentView(R.layout.activity_main)


        initMapView()

        val permissions = mutableListOf<String>()
        permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        requestPermissions(permissions.toTypedArray(), PERMISSIONS_REQUEST_CODE)
    }

    private fun initMapView() {
        //region Init - MapView
        val mapController = mapView.controller
        mapView.setMultiTouchControls(true)
        mapController.setZoom(9.5)
        mapController.setCenter(GeoPoint(46.2, 5.2167))
        //endregion

        //region Location - Overlay
        locationOverlay =
            MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), mapView).apply {
                enableMyLocation()
            }
        mapView.overlays.add(locationOverlay)
        //endregion

        //region Compass - Overlay
        compassOverlay = CompassOverlay(
            applicationContext,
            InternalCompassOrientationProvider(applicationContext),
            mapView
        ).apply {
            enableCompass()
        }
        mapView.overlays.add(compassOverlay)
        //endregion

        //region Grid Line - Overlay
        mapView.overlays.add(LatLonGridlineOverlay2())
        //endregion

        //region Rotation - Gesture - Overlay
        val test = RotationGestureOverlay(mapView)
        gestureOverlay = RotationGestureOverlay(mapView).apply {
            isEnabled = true
        }
        mapView.overlays.add(gestureOverlay)
        //endregion

        //region Map Scale Bar - Overlay
        scaleBarOverlay = ScaleBarOverlay(mapView).apply {
            setCentred(true)
            setScaleBarOffset(
                applicationContext.resources.displayMetrics.widthPixels / 2,
                10
            )
        }
        mapView.overlays.add(scaleBarOverlay)
        //endregion

        //region Map - Icon
        val items = ArrayList<OverlayItem>()
        items.add(
            OverlayItem(
                "Marker",
                "Test Markeur on Map",
                GeoPoint(46.2, 5.2167)
            ).run {
                this.setMarker(
                    resources.getDrawable(R.drawable.custom_marker, null)
                )
                this
            }
        )
        val iconOverlay = ItemizedIconOverlay(
            applicationContext,
            items,
            object : OnItemGestureListener<OverlayItem> {

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                    Toast.makeText(applicationContext, "Clique Long", Toast.LENGTH_LONG).show()
                    return true
                }

                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    Toast.makeText(applicationContext, "Clique Court", Toast.LENGTH_LONG).show()
                    return true
                }
            })
        mapView.overlays.add(iconOverlay)

        //endregion

        //region TileSource - Online
        mapView.setTileSource(object : OnlineTileSourceBase(
            "USGS Topo",
            0,
            18,
            256,
            "",
            arrayOf("http://basemap.nationalmap.gov/ArcGIS/rest/services/USGSTopo/MapServer/tile/")
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                return (baseUrl
                        + MapTileIndex.getZoom(pMapTileIndex)
                        + "/" + MapTileIndex.getY(pMapTileIndex)
                        + "/" + MapTileIndex.getX(pMapTileIndex)
                        + mImageFilenameEnding)
            }

        })
        //endregion
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (element in grantResults) {
            permissionsToRequest.add(permissions[element])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mapView.onDetach()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1
    }
}
