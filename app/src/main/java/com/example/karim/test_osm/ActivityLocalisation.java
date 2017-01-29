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

public class ActivityLocalisation extends AppCompatActivity implements View.OnClickListener, LocationListener, CompoundButton.OnCheckedChangeListener
{
    private Switch chSwitch;
    private TextView zone_longitude, zone_latitude, zone_acces_compte;
    private Location location;
	private GeoPoint currentLocation;
    private LocationManager locationManager;
    private Button boutonLoc;
	private String filename = "file.txt";
	private File fichier;

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

        /* recuperation des widgets */
        chSwitch = (Switch) findViewById(R.id.switch1);
        zone_latitude = (TextView) findViewById(R.id.zone_latitude);
        zone_longitude = (TextView) findViewById(R.id.zone_longitude);
        zone_acces_compte = (TextView) findViewById(R.id.textAccesCompte);
        boutonLoc = (Button) findViewById(R.id.boutonLoc);

        chSwitch.setOnCheckedChangeListener(this);
        zone_acces_compte.setOnClickListener(this);
        boutonLoc.setOnClickListener(this);

        /* localisation */
        //getSystemService permet l'obtention des ressources GPS du systeme
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

	/**
	 * Méthode de gestion des cliques utilisateurs. Elle permet de gérer la demande de redirection vers l'activity du compte personnel de l'utilisateur.
	 * @param v Le composant graphique sur lequel l'utilisateur a cliqué.
	 */
    public void onClick(View v)
    {
        if (v.getId() == zone_acces_compte.getId())
        {
            Intent intent = new Intent(this, ActivityCompteUtilisateur.class);
            startActivity(intent);
        }
        if ((v.getId() == R.id.boutonLoc) && (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
        {
			//test d'obtention d'une ancienne donnée GPS
			location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
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
				Log.d("recupération", "Récupération des données, chemin: "+this.getFilesDir());
				fichier = new File(this.getFilesDir(), filename);

				String provider = locationManager.getBestProvider(new Criteria(), true);
				if (provider == null)	//On ne peut pas relever les coordonnées GPS
				{
					message = "Il est impossible de relever les coordonées GPS, Veuiller activer votre GPS";
				}
				else	//On peut relever les coordonnées GPS
				{
					message = "Activation du tracking";
					locationManager.requestLocationUpdates(provider, 0, 0, this);
				}
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
			locationManager.removeUpdates(this);
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
		try
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
	 * Méthode afin de gérer les changements d'état du provider des données GPS.
	 * @param provider Le provider fournissant les données GPS.
	 * @param status Le nouveau status du provider.
	 * @param extras Des données supplementaires sur le provider.
	 */
    public void onStatusChanged(String provider, int status, Bundle extras)
    {}

	/**
	 * Méthode s'activant si le provider est activé par l'utilisateur.
	 * @param provider Nom du provider GPS.
	 */
    public void onProviderEnabled(String provider)
    {}

	/**
	 * Méthode s'activant si le provider est désactivé par l'utilisateur.
	 * @param provider Nom du provider GPS.
	 */
    public void onProviderDisabled(String provider)
    {}

	/**
	 * Méthode d'obtention du provider GPS le plus efficace.
	 * @return Le provider GPS le plus efficace.
	 */
	public String getBetterProvider()
	{
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			return LocationManager.GPS_PROVIDER;
		else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			return LocationManager.NETWORK_PROVIDER;
		return null;
	}
}