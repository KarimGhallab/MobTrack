package com.example.karim.test_osm;

import android.app.Activity;
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

import com.mysql.jdbc.Util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Coach on 14/03/2017.
 */

public class ActivityTrajet extends AppCompatActivity implements AdapterView.OnItemClickListener
{
	private Utilisateur chUtil;

	private final String NOUVEAU_TRAJET = "Mes trajets";
	private final String TITRE_COMPTE = "Mon compte";
	private final String TITRE_DECO = "Déconnexion";
	private String[] titresPanelNavigation = {NOUVEAU_TRAJET, TITRE_COMPTE, TITRE_DECO};
	private DrawerLayout chDrawerLayout;
	private ListView chDrawerList;
	private ActionBarDrawerToggle chDrawerToggle;

	private HashMap<Integer, String> chTrajets;
	private ArrayList<String> listTrajet;

	private ListView chListTrajet;

	private int chCle;

	protected void onCreate(Bundle savedInstanceState)
	{
		chUtil = getIntent().getParcelableExtra("utilisateur");

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

		final AppCompatActivity myActivity = this;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				chTrajets = BaseDeDonnees.getTrajets(chUtil.getID());
				listTrajet = new ArrayList<String>();

				for (String trajet : chTrajets.values())
				{
					listTrajet.add(trajet);
				}

				myActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						for (String s : listTrajet)
							Log.d("debug", s);
						chListTrajet.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, listTrajet));
						chListTrajet.setOnItemClickListener(new AdapterView.OnItemClickListener()
						{
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id)
							{
								//Ici on obtient la clé associée à la valeur
								String valeur = listTrajet.get(position);
								for (int cle : chTrajets.keySet())
								{
									if (chTrajets.get(cle).equals(valeur))
									{
										chCle = cle;
									}
								}
								Log.d("Click", chTrajets.get(chCle));
							}
						});
					}
				});
			}
		}).start();
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
				break;
			case TITRE_COMPTE:
				Log.d("panel", TITRE_COMPTE);
				break;
			case TITRE_DECO:
				Log.d("panel", TITRE_DECO);
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