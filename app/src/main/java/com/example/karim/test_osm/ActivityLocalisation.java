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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Classe ActivityLocalisation, elle classe représente une activty de notre application dans laquelle l'utilisateur peut demander
 * à notre application de démarrer ou d'arréter le tracking.
 * Elle permet aussi d'afficher certaines information sur le trajet en cours.
 */

public class ActivityLocalisation extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, CompoundButton.OnCheckedChangeListener, com.google.android.gms.location.LocationListener
{
    private Switch chSwitch;
    private TextView zone_longitude, zone_latitude, zone_acces_compte;
    private Location location;
	private GeoPoint currentLocation;
    private Button boutonLoc;
	private String filename = "file.txt";
	private File fichier;
	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;


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
        zone_latitude = (TextView) findViewById(R.id.zone_latitude);
        zone_longitude = (TextView) findViewById(R.id.zone_longitude);
        zone_acces_compte = (TextView) findViewById(R.id.textAccesCompte);
        boutonLoc = (Button) findViewById(R.id.boutonLoc);

		/* Ajout des listener aux widgets */
        chSwitch.setOnCheckedChangeListener(this);
        zone_acces_compte.setOnClickListener(this);
        boutonLoc.setOnClickListener(this);

		//Création d'une instance de GoogleApiClient
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
        if ((v.getId() == R.id.boutonLoc) && (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
        {
			if (location != null)
			{
				currentLocation = new GeoPoint(location);
				zone_latitude.setText(""+currentLocation.getLatitude());
				zone_longitude.setText(""+currentLocation.getLongitude());
			}
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

	@Override
	public void onConnected(Bundle bundle)
	{
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(10000); // Update location every second

		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);		//Demande de reception régulière de données GPS

		location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
		if (location != null)
		{
			currentLocation = new GeoPoint(location);
			String text = "Latitude :"+currentLocation.getLatitude()+" Longitude :"+currentLocation.getLongitude();
			Log.d("1er loc", text);
		}
	}

	public void onConnectionSuspended(int i)
	{

	}

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

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		googleApiClient.disconnect();
	}
}