package com.example.karim.test_osm;

import android.app.Activity;
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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
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
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Classe ActivityLocalisation, elle classe représente une activty de notre application dans laquelle l'utilisateur peut demander
 * à notre application de démarrer ou d'arréter le tracking.
 * Elle permet aussi d'afficher certaines information sur le trajet en cours.
 */

public class ActivityLocalisation extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, ItemizedIconOverlay.OnItemGestureListener, AdapterView.OnItemClickListener
{
	//Le panel de navigation
	private final String TITRE_TRAJET = "Mes trajets";
	private final String TITRE_COMPTE = "Mon compte";
	private final String TITRE_DECO = "Déconnexion";
	private String[] titresPanelNavigation = {TITRE_TRAJET, TITRE_COMPTE, TITRE_DECO};
	private DrawerLayout chDrawerLayout;
	private ListView chDrawerList;
	private ActionBarDrawerToggle chDrawerToggle;

	private final int MIL_SEC_INTERVALLE = 60000;		//60 secondes
	private Utilisateur util;
	private int chIdParcours;
	private double chDistanceTotale = 0;
	private GeoPoint chPointPrecedent;
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
		util = getIntent().getParcelableExtra("Utilisateur");

        super.onCreate(savedInstanceState);
		super.setContentView(R.layout.test_localisation);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		/* Le panel de gauche */
		chDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_localisation);
		chDrawerList = (ListView) findViewById(R.id.left_drawer);

		chDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titresPanelNavigation));
		chDrawerList.setOnItemClickListener(this);

		this.setupDrawer();


        /* Récuperation des widgets */
		trackMe = (Button) findViewById(R.id.track_me);
		untrackMe = (Button) findViewById(R.id.untrack_me);
        zone_acces_compte = (TextView) findViewById(R.id.textAccesCompte);
		chZoomIn = (ImageButton) findViewById(R.id.zoom_in);
		chZoomOut = (ImageButton) findViewById(R.id.zoom_out);
		chMap = (MapView) findViewById(R.id.carte);

		desactiverBouton(R.id.untrack_me);

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
						new Thread(insererNouveauParcours(util.getID(), this)).start();

						//TODO gestion des fichiers
						/*try
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
						}*/
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
				desactiverBouton(R.id.untrack_me);

				activerBouton(R.id.track_me);
				afficherFichier();
				message = "Désactivation du tracking";
				new Thread(finirParcours(chIdParcours, chDistanceTotale)).start();
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
		if (chIdParcours != 0)
		{
			GeoPoint point = new GeoPoint(location);
			double latitude = point.getLatitude();
			double longitude = point.getLongitude();

			updateMap(point);
			String text = "Latitude :"+point.getLatitude()+" Longitude :"+point.getLongitude();
			FileOutputStream output;
			try
			{
				//Envoi des données
				//Le premier point du parcours
				if (chPointPrecedent == null)
				{
					new Thread(envoiLocalisation(chIdParcours, latitude, longitude, 0)).start();
				}
				else
				{
					double distance = chPointPrecedent.distanceTo(point);
					chDistanceTotale += distance;
					new Thread(envoiLocalisation(chIdParcours, latitude, longitude, distance)).start();
					chPointPrecedent = point;
				}

				//Fichier
				output = openFileOutput(chNomFichier, Context.MODE_PRIVATE);
				output.write(text.getBytes());
			}
			catch(Exception e)
			{
				Log.e("Error", e.getMessage());
			}
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
		chLocationRequest.setInterval(MIL_SEC_INTERVALLE); // Mise à jour de la position toutes les 60 secondes

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
		chGoogleApiClient.disconnect();
		BaseDeDonnees.deconnexionBD();
		Log.d("Deco", "Déconnection");
		super.onDestroy();
	}

	/**
	 * Met à jour la carte
	 * @param point Le nouveau point qu'il faut ajouter à la carte
	 */
	private void updateMap(GeoPoint point)
	{
		//Ajout du point à la carte
		chPoints.add(point);

		new Thread(calculerRoute()).start();

		chController.setCenter(point);

		//On place un marker sur la carte
		Marker nodeMarker = new Marker(chMap);
		nodeMarker.setPosition(point);
		nodeMarker.setTitle("etape "+chPoints.size());
		chMap.getOverlays().add(nodeMarker);
		chMap.invalidate();
	}

	/**
	 * Crée un flot d'éxécution afin d'insérer un nouveau parcours dans la BD
	 * @param chIDUser L'id de l'utilisateur souhaitant créer un parcours.
	 * @param context Le context de l'interface (nécéssaire pour afficher des messages à l'utilisateur).
	 * @return Un flot d'éxécution insérant un nouveau parcours.
	 */
	private Runnable insererNouveauParcours(final int chIDUser, final Activity context)
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				Log.d("Thread", "Dans le nouveau Thread");
				context.runOnUiThread(new Runnable() {
					@Override
					public void run()
					{
						Log.d("Thread", "Désactivation du bouton dans le nouveau Thread");
						desactiverBouton(R.id.track_me);
					}
				});
				int res = BaseDeDonnees.insererNouveauParcours(chIDUser);
				//L'insertion du nouveau parcours ne s'est pas bien déroulé
				if (res == -1)
				{
					context.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							Toast message = Toast.makeText(context, "Nous n'avons pas reussi à créer votre parcours...", Toast.LENGTH_LONG);
							message.show();
							activerBouton(R.id.track_me);
						}
					});
				}
				else
				{
					chIdParcours = res;
					context.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							activerBouton(R.id.untrack_me);
						}
					});
				}
			}
		};
		return r;
	}


	//INSERT INTO localisations (idparcours, latitude, longitude, distance) VALUES (idparcours, latitude, longitude, distance)
	private Runnable envoiLocalisation(final int parIdParcours, final double parLatitude, final double parLongitude, final double parDistance)
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				//TODO Gérer les possibles erreurs liées au réseau et la gestion du fichier
				boolean res = BaseDeDonnees.insererNouvelleLocalisation(parIdParcours, parLatitude, parLongitude, parDistance);
			}
		};
		return r;
	}

	private Runnable finirParcours(final int idParcours, final double distanceTotale)
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				//TODO Gérer les possibles erreurs liées au réseau et la gestion du fichier
				boolean res = BaseDeDonnees.updateDistanceTotale(idParcours, distanceTotale);
			}
		};
		return r;
	}

	private void activerBouton(int parIdBouton)
	{
		if (parIdBouton == R.id.track_me)
		{
			trackMe.setAlpha(1);
			trackMe.setEnabled(true);
		}
		else if (parIdBouton == R.id.untrack_me)
		{
			untrackMe.setAlpha(1);
			untrackMe.setEnabled(true);
		}
	}

	private void desactiverBouton(int parIdBouton)
	{
		if (parIdBouton == R.id.track_me)
		{
			trackMe.setAlpha(.5f);
			trackMe.setEnabled(false);
		}
		else if (parIdBouton == R.id.untrack_me)
		{
			untrackMe.setAlpha(0.5f);
			untrackMe.setEnabled(false);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		Intent intent;
		switch(titresPanelNavigation[position])
		{
			case TITRE_TRAJET:
				Log.d("panel", util.toString());
				intent = new Intent(ActivityLocalisation.this, ActivityTrajet.class);
				intent.putExtra("utilisateur", util);
				startActivity(intent);
				break;

			case TITRE_COMPTE:
				Log.d("panel", TITRE_COMPTE);
				intent = new Intent(this, ActivityCompteUtilisateur.class);
				intent.putExtra("utilisateur", util);
				startActivity(intent);
				break;

			case TITRE_DECO:
				intent = new Intent(this, MainActivity.class);
				BaseDeDonnees.deconnexionBD();
				startActivity(intent);
				break;
		}
	}

	private void setupDrawer()
	{
		chDrawerToggle = new ActionBarDrawerToggle(this, chDrawerLayout, R.string.drawer_open, R.string.drawer_close)
		{
			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView)
			{
				super.onDrawerOpened(drawerView);
				chDrawerToggle.syncState();
				invalidateOptionsMenu();
			}

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View drawerView)
			{
				super.onDrawerOpened(drawerView);
				chDrawerToggle.syncState();
				invalidateOptionsMenu();
			}
		};
		chDrawerToggle.setDrawerIndicatorEnabled(true);
		chDrawerLayout.setDrawerListener(chDrawerToggle);
		chDrawerToggle.syncState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (chDrawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	private Runnable calculerRoute()
	{
		Runnable r = new Runnable() {
			@Override
			public void run()
			{
				Road road = roadManager.getRoad(chPoints);		//Obtention de la route
				Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
				chMap.getOverlays().add(roadOverlay);	//On ajoute la route
			}
		};
		return r;
	}
}