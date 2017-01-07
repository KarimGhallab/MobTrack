package com.example.karim.test_osm;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.*;
import java.util.Properties;

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
	 * Méthode de gestion des cliques utilisateurs. Elle permet de gérer la demande de création de compte de l'utilisateur
	 * @param v Le composant graphique sur lequel l'utilisateur a cliqué.
	 */
    public void onClick(View v)
    {
        if(v.getId() == boutonCreate.getId())
        {
			TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
			final String IMEI = telephonyManager.getDeviceId();
			final Activity myActivity = this;
            final String login = zoneLogin.getText().toString();
            final String mdp1 = zoneMdp1.getText().toString();
            final String mdp2 = zoneMdp2.getText().toString();
            final String ville = zoneVille.getText().toString();
            final String mail = zoneMail.getText().toString();

            AlertDialog.Builder message_co_bd = new AlertDialog.Builder(ActivityNouveauCompte.this);
            message_co_bd.setMessage(R.string.co_bd);

			final AlertDialog dialog = message_co_bd.create();
            dialog.setCancelable(false);    //annule la suppresion de la boite de dialogue lors de retour ou d'un clique en dehord de la boite
            dialog.show();      //ici la boite de dialogue s'affiche
            new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        BaseDeDonnees.connexionBD();
                        BaseDeDonnees.insererUtilisateur(login, mdp1, mail, ville, IMEI);
                    }
                    catch (Exception exp)
                    {
                        Log.e("erreur connexion", exp.getMessage());
                        myActivity.runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                dialog.cancel();
                                Toast message = Toast.makeText(ActivityNouveauCompte.this, R.string.erreur_co_bd, Toast.LENGTH_LONG);
                                message.show();
                            }
                        });
                    }
                }
            }).start();
        }
    }
}
