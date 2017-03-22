package com.example.karim.test_osm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import java.util.ArrayList;

/**
 * Created by Coach on 18/03/2017.
 */

public class ActivityTrajet extends AppCompatActivity implements View.OnClickListener
{

	private ImageButton boutonZoomIn, boutonZoomOut;

	private MapView chMap;
	private IMapController chController;
	private RoadManager roadManager;
	private int chIdParcours;
	private ArrayList<GeoPoint> chLocalisation;
	private Utilisateur chUtil;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trajet);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		chUtil = getIntent().getParcelableExtra("utilisateur");
		chIdParcours = getIntent().getIntExtra("idLocalisation", -1);

		boutonZoomIn = (ImageButton) findViewById(R.id.zoom_in_trajet_selectionne);
		boutonZoomOut = (ImageButton) findViewById(R.id.zoom_out_trajet_selectionne);

		boutonZoomIn.setOnClickListener(this);
		boutonZoomOut.setOnClickListener(this);

		chMap = (MapView) findViewById(R.id.carte_trajet_selectionne);
		chMap.setTileSource(TileSourceFactory.MAPNIK);
		chMap.setMultiTouchControls(true);
		chController = chMap.getController();
		chController.setZoom(chMap.getMaxZoomLevel() - 5);

		roadManager = new MapQuestRoadManager("G8hj0jdN4i6ZYDCnfK9AQLyAuCjTJb7z");
		roadManager.addRequestOption("routeType=pedestrian");

		new Thread(afficherCarte(chIdParcours)).start();
	}

	@Override
	protected void onDestroy()
	{
		BaseDeDonnees.deconnexionBD();
		super.onDestroy();
	}

	@Override
	public void onClick(View v)
	{
		Log.d("debug", "Click sur un bouton");
		if (v.getId() == R.id.zoom_in_trajet_selectionne)		//agrandissement
		{
			chController.zoomIn();
		}

		else if (v.getId() == R.id.zoom_out_trajet_selectionne)		//rétrécissement
		{
			chController.zoomOut();
		}

	}

	private Runnable afficherCarte(final int parIdParcours)
	{
		final AppCompatActivity activity = this;
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				chLocalisation = BaseDeDonnees.getLocalisation(parIdParcours);
				new Thread(calculerRoute()).start();
				chController.setCenter(chLocalisation.get(0));


				int compteur = 1;
				for (GeoPoint point : chLocalisation)
				{
					Marker nodeMarker = new Marker(chMap);
					nodeMarker.setPosition(point);
					nodeMarker.setTitle("etape "+compteur);
					chMap.getOverlays().add(nodeMarker);
					compteur++;
					Log.d("Marker", "Ajout d'un marker");
				}
				activity.runOnUiThread(new Runnable()
				{
					public void run()
					{
						chMap.invalidate();
					}
				});
			}
		};
		return r;
	}

	private Runnable calculerRoute()
	{
		Runnable r = new Runnable() {
			@Override
			public void run()
			{
				Road road = roadManager.getRoad(chLocalisation);		//Obtention de la route
				Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
				chMap.getOverlays().add(roadOverlay);	//On ajoute la route
			}
		};
		return r;
	}

	@Override
	public void onBackPressed()
	{
		Intent intent = new Intent(this, ActivityMesTrajets.class);
		intent.putExtra("utilisateur", chUtil);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				Intent intent = new Intent(this, ActivityMesTrajets.class);
				intent.putExtra("utilisateur", chUtil);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
