package com.example.karim.test_osm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.gesture.GestureOverlayView;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

/**
 * Classe ActivityLocalisation, elle classe représente une activty de notre application dans laquelle l'utilisateur peut demander
 * à notre application de démarrer ou d'arréter le tracking.
 * Elle permet aussi d'afficher certaines information sur le trajet en cours.
 */

public class ActivityLocalisation extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, ItemizedIconOverlay.OnItemGestureListener
{
    //private Switch chSwitch;
	private Button trackMe, untrackMe;
    private TextView zone_acces_compte;
	private String chNomFichier = "position.txt";
	private File chFichier;
	private FileOutputStream chOutput;
	private GoogleApiClient chGoogleApiClient;
	private LocationRequest chLocationRequest;
	private MapView chMap;
	private IMapController chController;
	private ArrayList<OverlayItem> chItems;
	private ItemizedIconOverlay chLocationOverlay;
	private ImageButton chZoomIn, chZoomOut;
	private ArrayList<GeoPoint> chPoints;
	private RoadManager roadManager;
	private boolean tracking = false;


	/**
	 * Méthode de création de l'activity.
	 * @param savedInstanceState L'etat de l'instance précédente.
	 */
    protected void onCreate(Bundle savedInstanceState)
    {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		Utilisateur util = getIntent().getParcelableExtra("Utilisateur");
		Log.i("util", util.toString());
        super.onCreate(savedInstanceState);
		super.setContentView(R.layout.test_localisation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Récuperation des widgets */
        //chSwitch = (Switch) findViewById(R.id.switch1);
		trackMe = (Button) findViewById(R.id.track_me);
		untrackMe = (Button) findViewById(R.id.untrack_me);
        zone_acces_compte = (TextView) findViewById(R.id.textAccesCompte);
		chZoomIn = (ImageButton) findViewById(R.id.zoom_in);
		chZoomOut = (ImageButton) findViewById(R.id.zoom_out);
		chMap = (MapView) findViewById(R.id.carte);

		//Parametrage de la carte
		chMap.setTileSource(TileSourceFactory.MAPNIK);
		chMap.setMultiTouchControls(true);
		chController = chMap.getController();
		chController.setZoom(chMap.getMaxZoomLevel() - 5);

		roadManager = new MapQuestRoadManager("G8hj0jdN4i6ZYDCnfK9AQLyAuCjTJb7z");
		roadManager.addRequestOption("routeType=pedestrian");

		chPoints = new ArrayList<>();

		/* Ajout des listener aux widgets */
        //chSwitch.setOnCheckedChangeListener(this);
		trackMe.setOnClickListener(this);
		untrackMe.setOnClickListener(this);
        zone_acces_compte.setOnClickListener(this);
		chZoomIn.setOnClickListener(this);
		chZoomOut.setOnClickListener(this);

		buildGoogleApiClient();
    }

	/**
	 * Called when a view has been clicked.
	 *
	 * @param v The view that was clicked.
	 */
	@Override
    public void onClick(View v)
    {
        if (v.getId() == zone_acces_compte.getId())		//L'utilisateur souhaite accèder à son compte
        {
            Intent intent = new Intent(this, ActivityCompteUtilisateur.class);
            startActivity(intent);
        }

		else if (v.getId() == R.id.track_me)
		{
			String message;
			if (tracking)
			{
				message = "Le tracking est déjà activé, allez y courez !";
			}
			//Le tracking n'etait pas déjà activé
			else
			{
				LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
				//Le GPS n'est pas activé
				if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ))
				{
					message = "Le GPS ne semble pas activé, activer le, cela vous evitera une facture salée à la fin du mois !";
				}
				//Le GPS est activé
				else
				{
					//verification de la permission d'accès aux données GPS
					if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
					{
						message = "Activation du tracking";
						tracking = true;
						chGoogleApiClient.connect();
						try
						{
							String text = "Jason";
							FileOutputStream chOutput = openFileOutput(chNomFichier, Context.MODE_APPEND);
							chOutput.write(text.getBytes());
							text = "\nStatham";
							chOutput.write(text.getBytes());
						}
						catch (Exception e)
						{
							Log.e("Pb creation fichier", e.getMessage());
						}
					}
					else
					{
						message = "L'application n'a pas l'autorisation d'accèder au GPS de votre appareil";
						Log.e("erreur", "Impossiblité de récupérer les données");
					}
				}
			}
			Toast messageTemporaire;
			messageTemporaire = Toast.makeText(ActivityLocalisation.this, message, Toast.LENGTH_LONG);
			messageTemporaire.show();
		}

		else if (v.getId() == R.id.untrack_me)
		{
			String message;
			//Le tracking est déjà désactivé
			if (!tracking)
			{
				message = "Voyons le tracking est déjà désactivé mon ami";
			}
			else
			{
				tracking = false;
				afficherFichier();
				message = "Désactivation du tracking";
			}
			Toast messageTemporaire;
			messageTemporaire = Toast.makeText(ActivityLocalisation.this, message, Toast.LENGTH_LONG);
			messageTemporaire.show();
			chGoogleApiClient.disconnect();
		}

		else if (v.getId() == R.id.zoom_in)		//agrandissement
		{
			chController.zoomIn();
		}

		else if (v.getId() == R.id.zoom_out)		//rétrécissement
		{
			chController.zoomOut();
		}
    }

	/**
	 * Méthode permettant de gérer l'arrivé de nouvelles données GPS.
	 * @param location La localisation actuelle de l'utilisateur.
	 */
	public void onLocationChanged(Location location)
    {
		GeoPoint point = new GeoPoint(location);
		updateMap(point);
		String text = "Latitude :"+point.getLatitude()+" Longitude :"+point.getLongitude();
		Log.d("nouvelle localisation", text);
		FileOutputStream output;
		try		//ecriture dans le fichier
		{
			output = openFileOutput(chNomFichier, Context.MODE_PRIVATE);
			output.write(text.getBytes());
		}
		catch(Exception e)
		{
			Log.e("Error", e.getMessage());
		}
    }

	/**
	 * Méthode appelée après si la methode connect() s'est exécutée avec succès
	 * @param bundle Bundle de données fournis par les services Google Play pour le client. Il peut être null si aucune données n'est fournises au client
	 */
	@Override
	public void onConnected(Bundle bundle)
	{
		chLocationRequest = LocationRequest.create();		//Requête de relevé de données GPS
		chLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);	//On fixe la priorité pour le relevé
		chLocationRequest.setInterval(60000); // Mise à jour de la position toutes les 60 secondes

		LocationServices.FusedLocationApi.requestLocationUpdates(chGoogleApiClient, chLocationRequest, this);		//Demande de reception régulière de données GPS
	}

	/**
	 * Méthode appelée lorsque la connexion aux services Google Play est suspendue
	 * @param i La cause de la suspension
	 */
	public void onConnectionSuspended(int i)
	{

	}

	/**
	 * Méthode appelée si la connexion aux services Google Play a echoué
	 * @param connectionResult Le résultat de la connexion. Peut être utilisé pour résoudre le problème
	 */
	public void onConnectionFailed(ConnectionResult connectionResult)
	{

	}

	/**
	 * Méthode de construction d'un objet de la classe GoogleApiClient
	 */
	synchronized void buildGoogleApiClient()
	{
		chGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
	}

	/**
	 * Méthode appelée lors de la déstruction de l'activité courante
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		chGoogleApiClient.disconnect();
		BaseDeDonnees.deconnexionBD();
		Log.d("Deco", "Déconnection");
	}

	/**
	 * Met à jour la carte
	 * @param point Le nouveau point qu'il faut ajouter à la carte
	 */
	private void updateMap(GeoPoint point)
	{
		//Ajout du point à la carte
		chPoints.add(point);
		Road road = roadManager.getRoad(chPoints);
		Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
		chMap.getOverlays().add(roadOverlay);	//On ajoute la route
		chController.setCenter(point);

		//On place un marker sur la carte
		Marker nodeMarker = new Marker(chMap);
		nodeMarker.setPosition(point);
		nodeMarker.setTitle("etape "+chPoints.size());
		chMap.getOverlays().add(nodeMarker);
		chMap.invalidate();
	}

	@Override
	public boolean onItemSingleTapUp(int index, Object item)
	{
		return false;
	}

	@Override
	public boolean onItemLongPress(int index, Object item)
	{
		return false;
	}

	public void afficherFichier()
	{
		try {
			InputStream inputStream = openFileInput(chNomFichier);
			if ( inputStream != null )
			{
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();

				while ( (receiveString = bufferedReader.readLine()) != null )
				{
					Log.d("ligne", "Nouvelle ligne");
					stringBuilder.append(receiveString);
				}

				inputStream.close();
				Log.d("Contenu fichier", stringBuilder.toString());
			}
		}
		catch (FileNotFoundException e) {
			Log.e("login activity", "File not found: " + e.toString());
		} catch (IOException e) {
			Log.e("login activity", "Can not read file: " + e.toString());
		}
	}
}