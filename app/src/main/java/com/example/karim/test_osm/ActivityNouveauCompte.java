package com.example.karim.test_osm;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Classe ActivityNouveauCompte, elle permet à l'utilisateur de se créer un compte sur notre application.
 */

public class ActivityNouveauCompte extends AppCompatActivity implements View.OnClickListener
{
    private EditText zoneLogin, zoneMdp1, zoneMdp2, zoneVille, zoneMail;
    private Button boutonCreate;

	/**
	 * Methode de création de l'activity.
	 * @param savedInstanceState L'etat de l'instance précédente.
	 */
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nouveau_compte);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* recuperation de widgets */
        zoneLogin = (EditText) findViewById(R.id.zone_login);
        zoneMdp1 = (EditText) findViewById(R.id.zone_mdp1);
        zoneMdp2 = (EditText) findViewById(R.id.zone_mdp2);
        zoneVille = (EditText) findViewById(R.id.zone_ville);
        zoneMail = (EditText) findViewById(R.id.zone_mail);
        boutonCreate = (Button) findViewById(R.id.boutonCreate);

        boutonCreate.setOnClickListener(this);
    }

	/**
	 * Called when a view has been clicked.
	 *
	 * @param v The view that was clicked.
	 */
	@Override
	public void onClick(View v)
	{
		if(v.getId() == boutonCreate.getId())
		{
			final Toast messageTemporaire;	//message temporaire s'affichant en bas de l'ecran
			AlertDialog.Builder message = new AlertDialog.Builder(ActivityNouveauCompte.this);	//message ne s'effancant que une fois l'action finie ou s'il y a une erreur

			TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
			final String IMEI = telephonyManager.getDeviceId();
			final Activity myActivity = this;
			final String login = zoneLogin.getText().toString();
			final String mdp1 = zoneMdp1.getText().toString();
			final String mdp2 = zoneMdp2.getText().toString();
			final String ville = zoneVille.getText().toString();
			final String mail = zoneMail.getText().toString();

			if ((IMEI.equals("")) || (login.equals("")) || (mdp1.equals("")) || (mdp2.equals("")) || (ville.equals("")) || (mail.equals("")))		//Au moins un champ vide
			{
				messageTemporaire = Toast.makeText(ActivityNouveauCompte.this, R.string.champs_pas_remplis, Toast.LENGTH_LONG);
				messageTemporaire.show();
			}
			else if (mdp1.equals(mdp2) == false)	//mots de passe différents
			{
				messageTemporaire = Toast.makeText(ActivityNouveauCompte.this, R.string.mots_de_passe_ne_correspondent_pas, Toast.LENGTH_LONG);
				messageTemporaire.show();
			}
			else if (mdp1.length() < 8)		//mot de passe trop courts
			{
				messageTemporaire = Toast.makeText(ActivityNouveauCompte.this, R.string.mot_de_passe_trop_court, Toast.LENGTH_LONG);
				messageTemporaire.show();
			}
			else if ((Patterns.EMAIL_ADDRESS.matcher(mail).matches()) == false)	//Mail non valide La fonction utilise une expression réguliere afin de vérifier si le mail est valide
			{
				messageTemporaire = Toast.makeText(ActivityNouveauCompte.this, R.string.mail_non_valide, Toast.LENGTH_LONG);
				messageTemporaire.show();
			}
			else	//Il n'y a aucunes erreurs de saisies
			{
				message.setMessage(R.string.co_bd);
				final AlertDialog dialog = message.create();
				dialog.setCancelable(false);    //annule la suppresion de la boite de dialogue lors de retour ou d'un clique en dehord de la boite
				dialog.show();      //ici la boite de dialogue s'affiche

				new Thread(new Runnable()
				{
					public void run()
					{
						int codeErreur = 0;	//0: tous s'est bien déroulé, 1: erreur ed connexion avec la BD, 2 si le login est déjà présent dans la BD
						boolean connexionBD = BaseDeDonnees.connexionBD();
						if (connexionBD == true)
							codeErreur = BaseDeDonnees.insererUtilisateur(login, mdp1, mail, ville, IMEI);
						else	//erreur de co avec la BD
							codeErreur = 1;
						myActivity.runOnUiThread(createRunnableForUI(codeErreur));
					}

					//Ici on gere l'affichage du message en fonction du code d'erreur recçu
					private Runnable createRunnableForUI(final int parCodeErreur)
					{
						Log.d("Code erreur", "Pendant UI :"+parCodeErreur);
						Runnable r = new Runnable()
						{
							Toast message;
							public void run()
							{
								Log.d("Code erreur", "Dans le run :"+parCodeErreur);
								switch (parCodeErreur)
								{
									case 0: message = Toast.makeText(ActivityNouveauCompte.this, R.string.bonne_insertion, Toast.LENGTH_LONG);
										break;
									case 1: message = Toast.makeText(ActivityNouveauCompte.this, R.string.erreur_co_bd, Toast.LENGTH_LONG);
										break;
									case 2: message = Toast.makeText(ActivityNouveauCompte.this, R.string.login_deja_present, Toast.LENGTH_LONG);
										break;
								}
								dialog.cancel();
								message.show();
							}
						};
						return r;
					}
				}).start();
			}
		}
	}

	@Override
	protected void onDestroy()
	{
		BaseDeDonnees.deconnexionBD();
		super.onDestroy();
	}
}