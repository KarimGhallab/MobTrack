package com.example.karim.test_osm;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Classe ActivityCompteUtilisateur, elle représente l'activity du compte personnel de l'utilisateur de l'application.
 * Ici il peut accèder à ses données personnelles et les modifier.
 */

public class ActivityCompteUtilisateur extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener
{
	private final int MODIFIER_LOGIN = 0;
	private final int MODIFIER_VILLE = 1;
	private final int MODIFIER_MAIL = 2;
	private final int MODIFIER_MDP = 3;

	private final String NOUVEAU_TRAJET = "Fair un nouveau parcours";
	private final String MES_TRAJETS = "Mes trajets";
	private final String TITRE_DECO = "Déconnexion";
	private String[] titresPanelNavigation = {NOUVEAU_TRAJET, MES_TRAJETS, TITRE_DECO};
	private DrawerLayout chDrawerLayout;
	private ListView chDrawerList;
	private ActionBarDrawerToggle chDrawerToggle;

	private ImageButton boutonLogin, boutonVille, boutonMail;
	private TextView champLogin, champMail, champVille, distanceTotale, lienModifMdp;

	private Utilisateur chUtil;

	/**
	 * Méthode de création de l'activity.
	 * @param savedInstanceState Etat de l'instance précédente.
	 */
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compte_personnel);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		chUtil = getIntent().getParcelableExtra("utilisateur");

		recupererWidget();
		afficherDonnees();

		/* Le panel de gauche */

		chDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titresPanelNavigation));
		chDrawerList.setOnItemClickListener(this);

		this.setupDrawer();
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

			case MES_TRAJETS:
				intent = new Intent(this, ActivityMesTrajets.class);
				intent.putExtra("utilisateur", chUtil);
				startActivity(intent);
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

	private void afficherDonnees()
	{
		champLogin.setText("Login : "+chUtil.getLogin());
		champMail.setText("Mail : "+chUtil.getMail());
		champVille.setText("Ville : "+chUtil.getVille());
		new Thread(obtenirEtAfficherDistanceTotale()).start();
	}

	private void recupererWidget()
	{
		chDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_trajet);
		chDrawerList = (ListView) findViewById(R.id.left_drawer_trajet);

		boutonLogin = (ImageButton) findViewById(R.id.imageButtonLogin);
		boutonVille = (ImageButton) findViewById(R.id.imageButtonVille);
		boutonMail = (ImageButton) findViewById(R.id.imageButtonMail);

		boutonLogin.setOnClickListener(this);
		boutonVille.setOnClickListener(this);
		boutonMail.setOnClickListener(this);

		champLogin = (TextView) findViewById(R.id.textLogin);
		champMail = (TextView) findViewById(R.id.textMail);
		champVille = (TextView) findViewById(R.id.textVille);
		distanceTotale = (TextView) findViewById(R.id.textDist);
		lienModifMdp = (TextView) findViewById(R.id.textUpdatePassword);

		lienModifMdp.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == boutonLogin.getId())
		{
			afficherBuilderModification(MODIFIER_LOGIN);
		}
		else if (v.getId() == boutonVille.getId())
		{
			afficherBuilderModification(MODIFIER_VILLE);
		}
		else if (v.getId() == boutonMail.getId())
		{
			afficherBuilderModification(MODIFIER_MAIL);
		}
		else if (v.getId() == lienModifMdp.getId())
		{
			afficherBuilderModification(MODIFIER_MDP);
		}
	}

	private Runnable obtenirEtAfficherDistanceTotale()
	{
		final AppCompatActivity monActivity = this;
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				double distance = BaseDeDonnees.getDistanceParcourue(chUtil.getID());
				if (distance == -1)
					monActivity.runOnUiThread(afficherErreur());
				else
					monActivity.runOnUiThread(afficherDistance(distance));
			}
		};
		return r;
	}

	private Runnable afficherErreur()
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				Toast message = Toast.makeText(getApplicationContext(), "Erreur réception de la distance parcourue", Toast.LENGTH_LONG);
				message.show();
			}
		};
		return r;
	}

	private Runnable afficherDistance(final double distance)
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				distanceTotale.setText("Distance totale parcourue : "+distance);
			}
		};
		return r;
	}

	private void afficherBuilderModification(final int champAModifier)
	{
		//Les deux editText necessaire pour changer de mot de passe
		final EditText inputMdp1 = new EditText(this);
		final EditText inputMdp2 = new EditText(this);
		inputMdp1.setHint("Veuillez saisir votre mot de passe...");
		inputMdp2.setHint("Veuillez confirmer votre mot de passe...");
		inputMdp1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		inputMdp2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

		final EditText input = new EditText(this);
		String champ = "";
		if (champAModifier == MODIFIER_LOGIN)
			champ = "login";
		else if (champAModifier == MODIFIER_VILLE)
			champ = "ville";
		else if (champAModifier == MODIFIER_MAIL)
		{
			champ = "mail";
			input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		}
		else if (champAModifier == MODIFIER_MDP)
			champ = "mot de passe";
		input.setHint("Votre nouveau "+champ+"...");

		DialogInterface.OnClickListener dialogClickListener;
		if (champAModifier != MODIFIER_MDP)
		{
			dialogClickListener = new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					switch (which)
					{
						case DialogInterface.BUTTON_POSITIVE:
							String valeurSaisie = input.getText().toString();
							if (valeurSaisie.length() > 0)
								new Thread(modifierChamp(champAModifier, valeurSaisie)).start();
							else
								Toast.makeText(getApplicationContext(), "La zone de saisie est vide", Toast.LENGTH_LONG).show();
							break;
					}
				}
			};
		}
		//Dialog pour un mot de passe
		else
		{
			dialogClickListener = new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					switch (which)
					{
						case DialogInterface.BUTTON_POSITIVE:
							String valeurSaisie = inputMdp1.getText().toString();
							String valeurSaisie2 = inputMdp2.getText().toString();

							Log.d("get values", "EditText 1 : "+valeurSaisie);
							Log.d("get values", "EditText 2 : "+valeurSaisie2);
							if (valeurSaisie.equals(valeurSaisie2))
							{
								if (valeurSaisie.length() >= 8)
									new Thread(modifierChamp(champAModifier, valeurSaisie)).start();
								//Le mot de passe saisie est trop court
								else
								{
									Toast.makeText(getApplicationContext(), "Le mot de passe doit faire une taille au moins égale à 8", Toast.LENGTH_LONG).show();
								}
							}
							//Les deux mot de passes ne coresspondent pase
							else
							{
								Toast.makeText(getApplicationContext(), "Les deux mot de passe saisis ne correspondent pas", Toast.LENGTH_LONG).show();
							}
							break;
					}
				}
			};
		}
		String article = "";
		String adjectif = "";
		if (champAModifier == MODIFIER_LOGIN || champAModifier == MODIFIER_MAIL)
		{
			article = "du";
			adjectif = "nouveau";
		}
		else if (champAModifier == MODIFIER_VILLE)
		{
			article = "de la";
			adjectif = "nouvelle";
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Modification "+article+" "+champ);
		builder.setMessage("Veuillez saisir votre "+adjectif+" "+champ);
		builder.setPositiveButton("Valider !", dialogClickListener);
		builder.setNegativeButton("Annuler...", dialogClickListener);
		if (champAModifier != MODIFIER_MDP)
		{
			builder.setView(input);
		}
		else
		{
			LinearLayout lay = new LinearLayout(this);
			lay.setOrientation(LinearLayout.VERTICAL);
			lay.addView(inputMdp1);
			lay.addView(inputMdp2);
			builder.setView(lay);
			builder.setTitle("");
		}
		builder.show();
	}

	private Runnable modifierChamp(final int champAModifier, final String nouvelleValeur)
	{
		final AppCompatActivity monActiviy = this;
		Log.d("debug", "modifier champ");
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				if (champAModifier == MODIFIER_LOGIN)
				{
					final int res = BaseDeDonnees.modifierLogin(chUtil.getLogin(), nouvelleValeur);
					monActiviy.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							Toast message = null;
							if (res == BaseDeDonnees.LOGIN_EXISTANT)
								message = Toast.makeText(getApplicationContext(), "Le login est déjà pris...", Toast.LENGTH_LONG);
							else if (res == 1)
							{
								message = Toast.makeText(getApplicationContext(), "Login modifié avec succès !", Toast.LENGTH_LONG);
								chUtil.setLogin(nouvelleValeur);
								champLogin.setText("Login : "+nouvelleValeur);
							}
							else
								message = Toast.makeText(getApplicationContext(), "Erreur lors de l'envoi de votre nouveau login...", Toast.LENGTH_LONG);
							message.show();
						}
					});
				}
				else if (champAModifier == MODIFIER_MAIL)
				{
					final Toast messageMailNonValide;
					if ((Patterns.EMAIL_ADDRESS.matcher(nouvelleValeur).matches()) == false)
					{
						monActiviy.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								Toast.makeText(getApplicationContext(), "Le mail saisi n'est pas valide", Toast.LENGTH_LONG).show();
							}
						});
					}
					else
					{
						final boolean res = BaseDeDonnees.modifierMail(chUtil.getLogin(), nouvelleValeur);
						monActiviy.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								Toast message = null;
								if (res)
								{
									message = Toast.makeText(getApplicationContext(), "Mail modifié avec succès !", Toast.LENGTH_LONG);
									chUtil.setMail(nouvelleValeur);
									champMail.setText("Mail : "+nouvelleValeur);
								}
								else
									message = Toast.makeText(getApplicationContext(), "Erreur lors de l'envoi de votre nouveau mail...", Toast.LENGTH_LONG);
								message.show();
							}
						});
					}
				}
				else if (champAModifier == MODIFIER_VILLE)
				{
					final boolean res = BaseDeDonnees.modifierVille(chUtil.getLogin(), nouvelleValeur);
					monActiviy.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							Toast message = null;
							if (res)
							{
								message = Toast.makeText(getApplicationContext(), "Ville modifiée avec succès !", Toast.LENGTH_LONG);
								chUtil.setLogin(nouvelleValeur);
								champVille.setText("Ville : "+nouvelleValeur);
							}
							else
								message = Toast.makeText(getApplicationContext(), "Erreur lors de l'envoi de votre nouvelle ville...", Toast.LENGTH_LONG);
							message.show();
						}
					});
				}
				else if (champAModifier == MODIFIER_MDP)
				{
					Log.d("debug", "Nouveau thread : modification du mdp");
					final boolean res = BaseDeDonnees.modifierMdp(chUtil.getLogin(), nouvelleValeur);
					monActiviy.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							Toast message;
							if (res)
								message = Toast.makeText(getApplicationContext(), "Mot de passe modifiée avec succès !", Toast.LENGTH_LONG);
							else
								message = Toast.makeText(getApplicationContext(), "Erreur lors de l'envoi de votre nouvelle ville...", Toast.LENGTH_LONG);
							message.show();
						}
					});
				}
			}
		};
		return r;
	}
}