package com.example.karim.test_osm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import org.osmdroid.util.GeoPoint;

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

	/**
	 * Méthode de création de l'activity.
	 * @param savedInstanceState L'etat de l'instance précédente.
	 */
    protected void onCreate(Bundle savedInstanceState)
    {
		Utilisateur util = getIntent().getParcelableExtra("Utilisateur");
		System.out.println("Je suis dans localisation: "+util);
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

        //verification de la permission d'accès aux données GPS
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            Log.d("yuyu", "Autorisation accordée !");
        }
        /* fin localisation */
    }

    //je sais pas à quoi ca sert 0.0
    /*public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d("yo", "je suis ici");
        switch (item.getItemId())
        {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

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
        if (v.getId() == R.id.boutonLoc)
        {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.removeUpdates(this);
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
        if(isChecked)
            Log.d("oui", "Tracking activé");
        else
            Log.d("non", "Tracking non-activé");
    }

	/**
	 * Méthode permettant de gérer l'arrivé de nouvelles données GPS.
	 * @param location La localisation actuelle de l'utilisateur.
	 */
	public void onLocationChanged(Location location)
    {
        Log.d("Nouvelle Localisation", "Latitude :"+location.getLatitude()+" Longitude :"+location.getLongitude());
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
	 * Méthode d'obtention du provider GPS le plus efficasse.
	 * @return Le provider GPS le plus efficace.
	 */
	public String getBetterProvider()
	{
		return null;
	}
}
