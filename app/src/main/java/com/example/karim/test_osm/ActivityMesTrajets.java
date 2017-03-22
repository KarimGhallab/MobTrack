package com.example.karim.test_osm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mysql.jdbc.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Coach on 14/03/2017.
 */

public class ActivityMesTrajets extends AppCompatActivity implements AdapterView.OnItemClickListener
{
	private Utilisateur chUtil;

	private final String NOUVEAU_TRAJET = "Faire un nouveau parcours";
	private final String TITRE_COMPTE = "Mon compte";
	private final String TITRE_DECO = "Déconnexion";
	private String[] titresPanelNavigation = {NOUVEAU_TRAJET, TITRE_COMPTE, TITRE_DECO};
	private DrawerLayout chDrawerLayout;
	private ListView chDrawerList;
	private ActionBarDrawerToggle chDrawerToggle;

	private ArrayList< ArrayList<String> > chTrajets;
	private ArrayList<String> listTrajet;

	private ListView chListTrajet;

	private int chCle;

	protected void onCreate(Bundle savedInstanceState)
	{
		chUtil = getIntent().getParcelableExtra("utilisateur");
		Log.d("ActivityMesTrajets", chUtil.toString());

		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.les_trajets);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		/* Le panel de gauche */
		chDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_trajet);
		chDrawerList = (ListView) findViewById(R.id.left_drawer_trajet);

		chDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titresPanelNavigation));
		chDrawerList.setOnItemClickListener(this);

		this.setupDrawer();

		chListTrajet = (ListView) findViewById(R.id.list_trajet);

		final AppCompatActivity myActivity = this;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				chTrajets = BaseDeDonnees.getTrajets(chUtil.getID());
				listTrajet = new ArrayList<String>();

				for (ArrayList<String> trajet : chTrajets)
					listTrajet.add(trajet.get(1));

				myActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						chListTrajet.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, listTrajet));
						chListTrajet.setOnItemClickListener(new AdapterView.OnItemClickListener()
						{
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id)
							{
								//Ici on obtient la clé associée à la valeur
								String idLoc = chTrajets.get(position).get(0);
								chCle = Integer.parseInt(idLoc);

								Intent intent = new Intent(ActivityMesTrajets.this, ActivityTrajet.class);
								intent.putExtra("idLocalisation", chCle);
								intent.putExtra("utilisateur", chUtil);
								startActivity(intent);
							}
						});
					}
				});
			}
		}).start();
	}

	@Override
	protected void onDestroy()
	{
		BaseDeDonnees.deconnexionBD();
		super.onDestroy();
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		switch(titresPanelNavigation[position])
		{
			case NOUVEAU_TRAJET:
				Log.d("panel", NOUVEAU_TRAJET);
				Intent intent = new Intent(this, ActivityLocalisation.class);
				intent.putExtra("utilisateur", chUtil);
				startActivity(intent);
				break;

			case TITRE_COMPTE:
				if (chUtil.getAnonymat() == 0)
				{
					intent = new Intent(this, ActivityCompteUtilisateur.class);
					intent.putExtra("utilisateur", chUtil);
					startActivity(intent);
				}
				else
					Toast.makeText(getApplicationContext(), "Vous êtes connecté anonymement, vous ne pouvez donc accèder la rubrique de compte", Toast.LENGTH_LONG).show();
				break;

			case TITRE_DECO:
				intent = new Intent(this, MainActivity.class);
				BaseDeDonnees.deconnexionBD();
				startActivity(intent);
				break;
		}
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
}