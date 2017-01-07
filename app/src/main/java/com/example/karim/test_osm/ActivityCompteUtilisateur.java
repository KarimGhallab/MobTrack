package com.example.karim.test_osm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Classe ActivityCompteUtilisateur, elle représente l'activity du compte personnel de l'utilisateur de l'application.
 * Ici il peut accèder à ses données personnelles et les modifier.
 */

public class ActivityCompteUtilisateur extends AppCompatActivity
{
    private Button valider;
	private ImageButton picLogin;
	private ImageButton picVille;
	private TextView distanceTotale;
	private TextView lienModifMdp;

	/**
	 * Méthode de création de l'activity.
	 * @param savedInstanceState Etat de l'instance précédente.
	 */
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compte_personnel);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
