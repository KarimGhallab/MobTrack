package com.example.karim.test_osm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Classe MainActivity, Activity lancé par défaut lors du démarrage de l'application.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	private Button connexion;
	private TextView coAnonyme;
	private TextView pasDeCompte;
	private EditText zoneLogin;
	private EditText zoneMdp;

	/**
	 * Methode de création de l'activity.
	 *
	 * @param savedInstanceState L'etat de l'instance précédente.
	 */
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accueil);

		connexion = (Button) findViewById(R.id.bouton_connexion);
		connexion.setOnClickListener(this);

		pasDeCompte = (TextView) findViewById(R.id.pas_de_compte);
		pasDeCompte.setOnClickListener(this);

		zoneLogin = (EditText) findViewById(R.id.login_connexion);
		zoneMdp = (EditText) findViewById((R.id.mdp_connexion));

		coAnonyme = (TextView) findViewById(R.id.connexion_anonyme);
		coAnonyme.setOnClickListener(this);
	}

	/**
	 * Called when a view has been clicked.
	 *
	 * @param v The view that was clicked.
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == connexion.getId()) {
			final String login = zoneLogin.getText().toString().toString();
			final String mdp = zoneMdp.getText().toString();

			final Activity myActivity = this;

			AlertDialog.Builder messageVerification = new AlertDialog.Builder(MainActivity.this);
			messageVerification.setMessage(R.string.co_util);
			final AlertDialog dialog = messageVerification.create();
			dialog.setCancelable(false);
			dialog.setView(new ProgressBar(this));
			dialog.show();

			new Thread(new Runnable() {
				public void run() {
					boolean resultat = BaseDeDonnees.connexionBD() && BaseDeDonnees.verifierUtilisateur(login, mdp);
					dialog.cancel();
					if (resultat == true) {
						Utilisateur util = BaseDeDonnees.getUtilisateur(login);
						Intent intent = new Intent(MainActivity.this, ActivityLocalisation.class);
						intent.putExtra("utilisateur", util);
						startActivity(intent);
					}
					else
					{
						myActivity.runOnUiThread(new Runnable()
						{
							public void run()
							{
								Toast message = Toast.makeText(MainActivity.this, R.string.erreur_co_util, Toast.LENGTH_LONG);
								message.show();
							}
						});
					}
				}
			}).start();
		}
		else if (v.getId() == pasDeCompte.getId())
		{
			Intent intent = new Intent(this, ActivityNouveauCompte.class);
			startActivity(intent);
		}
		else if (v.getId() == coAnonyme.getId())
		{
			AlertDialog.Builder messageVerification = new AlertDialog.Builder(MainActivity.this);
			messageVerification.setMessage("Connexion en cours...");
			final AlertDialog dialog = messageVerification.create();
			dialog.setCancelable(false);
			dialog.setView(new ProgressBar(this));
			dialog.show();
			new Thread(connexionAnonyme(dialog)).start();
		}
	}

	private Runnable connexionAnonyme(final AlertDialog dialog)
	{
		final AppCompatActivity monActivity = this;
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				String IMEI = telephonyManager.getDeviceId();

				Log.d("IMEI", IMEI);
				if (!BaseDeDonnees.IMEIExists(IMEI))
					BaseDeDonnees.insererUtilisateurAnonyme(IMEI);

				int id = BaseDeDonnees.getUserIDViaIMEI(IMEI);
				if (id == -1)
					monActivity.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							dialog.cancel();
							Toast.makeText(getApplicationContext(), "Erreur de connexion", Toast.LENGTH_LONG).show();
						}
					});
				else
				{
					Utilisateur user = new Utilisateur(id);
					Intent intent = new Intent(monActivity, ActivityLocalisation.class);
					intent.putExtra("utilisateur", user);
					dialog.cancel();
					startActivity(intent);
				}
			}
		};
		return r;
	}

	@Override
	public void onBackPressed()
	{
		finishAffinity();
	}
}