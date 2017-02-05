package com.example.karim.test_osm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;

/**
 * Classe ActivityLocalisation, elle classe représente une activty de notre application dans laquelle l'utilisateur peut demander
 * à notre application de démarrer ou d'arréter le tracking.
 * Elle permet aussi d'afficher certaines information sur le trajet en cours.
 */

public class ActivityLocalisation extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, CompoundButton.OnCheckedChangeListener, com.google.android.gms.location.LocationListener
{
    private Switch chSwitch;
    private TextView zone_acces_compte;
    private Location location;
	private GeoPoint currentLocation;
	private String filename = "file.txt";
	private File fichier;
	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;
	private MapView chMap;
	private MapController chController;
	private MyLocationNewOverlay myLocationNewOverlay;
	private OverlayItem overlayItem;


	/**
	 * Méthode de création de l'activity.
	 * @param savedInstanceState L'etat de l'instance précédente.
	 */
    protected void onCreate(Bundle savedInstanceState)
    {
		com.example.karim.test_osm.Utilisateur util = getIntent().getParcelableExtra("Utilisateur");
		Log.i("util", util.toString());
        super.onCreate(savedInstanceState);
		super.setContentView(R.layout.test_localisation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Récuperation des widgets */
        chSwitch = (Switch) findViewById(R.id.switch1);
        zone_acces_compte = (TextView) findViewById(R.id.textAccesCompte);
		/* Création de la carte */
		ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		chMap = new MapView(getApplicationContext(), 256, resourceProxy);
		chMap.setBuiltInZoomControls(true);		//affiche le bouton pour zoomer
		chMap.setMultiTouchControls(true);		//autorise les zooms avec les doigts
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.BELOW, R.id.switch1);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		super.addContentView(chMap, params);

		GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(this);
		gpsMyLocationProvider.
		myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()), chMap, resourceProxy);
		myLocationNewOverlay.enableMyLocation();
		GeoPoint gpt = new GeoPoint(48.82193150638453, 2.2615885611938893);
		chMap.getOverlays().add(myLocationNewOverlay);
		/*OverlayItem nouvelleItem = new OverlayItem("My Location", "My Location", gpt);
		myLocationNewOverlay.*/


		chController = (MapController) chMap.getController();
		chController.setZoom(20);
		chController.setCenter(gpt);

		/* Ajout des listener aux widgets */
        chSwitch.setOnCheckedChangeListener(this);
        zone_acces_compte.setOnClickListener(this);

		buildGoogleApiClient();
    }

	/**
	 * Méthode de gestion des cliques utilisateurs. Elle permet de gérer la demande de redirection vers l'activity du compte personnel de l'utilisateur.
	 * @param v Le composant graphique sur lequel l'utilisateur a cliqué.
	 */
    public void onClick(View v)
    {
        if (v.getId() == zone_acces_compte.getId())		//L'utilisateur souhaite accèder à son compte
        {
            Intent intent = new Intent(this, ActivityCompteUtilisateur.class);
            startActivity(intent);
        }
    }

	/**
	 * Méthode permettant de gérer les changements d'état des objets de la classe switch de l'interface graphique.
	 * @param buttonView Le switch sur lequel a appuyé l'utilisateur.
	 * @param isChecked L'etat du switch après le clique.
	 */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        Toast messageTemporaire;
		String message;
		if(isChecked)
		{
			//verification de la permission d'accès aux données GPS
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
			{
				message = "Activation du tracking";
				fichier = new File(this.getFilesDir(), filename);
				/*
		   			Connection du téléphone au Google Play Services
		   			En cas de succès, la méthode onConnected() est appelée
		   			Dans le cas contraire la méthode onConnectionFailed est appelée
		 		*/
				googleApiClient.connect();
			}
			else
			{
				message = "L'application n'a pas l'autorisation d'accèder au GPS de votre appareil";
				Log.e("erreur", "Impossiblité de récupérer les données");
			}
		}
        else
		{
			message = "Désactivation du tracking";
			googleApiClient.disconnect();
		}
		messageTemporaire = Toast.makeText(ActivityLocalisation.this, message, Toast.LENGTH_SHORT);
		messageTemporaire.show();
    }

	/**
	 * Méthode permettant de gérer l'arrivé de nouvelles données GPS.
	 * @param location La localisation actuelle de l'utilisateur.
	 */
	public void onLocationChanged(Location location)
    {
		GeoPoint position = new GeoPoint(location);
		String text = "Latitude :"+position.getLatitude()+" Longitude :"+position.getLongitude();
		FileOutputStream output;
		try		//ecriture dans le fichier
		{
			output = openFileOutput(filename, Context.MODE_PRIVATE);
			output.write(text.getBytes());
		}
		catch(Exception e)
		{
			Log.e("Error", e.getMessage());
		}
		Log.d("Nouvelle Localisation", text);
    }

	/**
	 * Méthode appelée après si la methode connect() s'est exécutée avec succès
	 * @param bundle Bundle de données fournis par les services Google Play pour le client. Il peut être null si aucune données n'est fournises au client
	 */
	@Override
	public void onConnected(Bundle bundle)
	{
		locationRequest = LocationRequest.create();		//Requête de relevé de données GPS
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);	//On fixe la priorité pour le relevé
		locationRequest.setInterval(10000); // Mise à jour de la position toutes les secondes

		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);		//Demande de reception régulière de données GPS

		location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);		//on cherche à obtenir une données GPS si celle-ci existe
		if (location != null)
		{
			currentLocation = new GeoPoint(location);
			String text = "Latitude :"+currentLocation.getLatitude()+" Longitude :"+currentLocation.getLongitude();
			Log.d("1er loc", text);
		}
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
		googleApiClient = new GoogleApiClient.Builder(this)
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
		googleApiClient.disconnect();
	}
}