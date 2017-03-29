package com.example.karim.test_osm;


import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TestUnitaireBaseDeDonnees
{
	@Test
	public void verifierUtilisateur_isCorrect()
	{
		BaseDeDonnees.connexionBD();
		//Test d'un couple login/mot de passe correct
		String login = "florian";
		String mdp = "florian";
		boolean resultat = BaseDeDonnees.verifierUtilisateur(login, mdp);
		assertTrue(resultat);

		//Test d'un couple login/mot de passe incorrect
		login = "Gotta keep Moving";
		mdp = "One step at the time";
		resultat = BaseDeDonnees.verifierUtilisateur(login,mdp);
		assertFalse(resultat);

		login = "";
		mdp = "";
		resultat = BaseDeDonnees.verifierUtilisateur(login,mdp);
		assertFalse(resultat);
	}

	//Test de la méthode getUtilisateur
	@Test
	public void getUtilisateur_isCorrect()
	{
		BaseDeDonnees.connexionBD();

		//Test d'un utilisateur present dans la base de données
		String login = "florian";
		Utilisateur util = BaseDeDonnees.getUtilisateur(login);

		boolean condition = ((util.getID() == 1) && (util.getLogin().equals("florian")) && (util.getMail().equals("florian")) && (util.getVille().equals("Meudon")));
		System.out.print(condition);
		assertTrue(condition);

		//Test d'un utilisateur inconnu dans la base de données
		login = "Luds";
		util = BaseDeDonnees.getUtilisateur(login);

		condition = (util == null);
		assertTrue(condition);
	}

	@Test
	public void insererUtilisateur_isCorrect()
	{
		BaseDeDonnees.connexionBD();

		//Test1: Le login n'est pas deja présent dans la BD
		String login = "login super nouveau";
		String mdp = "super_mot_de_passe";
		String mail = "machin@truc.com";
		String ville = "Chesterfield";
		String IMEI = "nouvel IMEI";
		int res1 = BaseDeDonnees.insererUtilisateur(login, mdp, mail, ville, IMEI);
		boolean resMdp = (mdp == BaseDeDonnees.getMdp(login));
		boolean resVille = (ville == BaseDeDonnees.getUserVille(login));
		boolean resMail = (mail == BaseDeDonnees.getUserMail(login));
		boolean resIMEI = (IMEI == BaseDeDonnees.getIMEI(login));

		assertTrue((res1 == 0) && resMail && resVille && resMdp && resIMEI);

		//Test2: le Login est dejà présent dans la BD
		login = "florian";
		res1 = BaseDeDonnees.insererUtilisateur(login, mdp, mail, ville, IMEI);
		assertTrue(res1 == 2);
	}

	@Test
	public void modifierLogin_isCorrect()
	{
		//Test1: Le login n'est pas déjà présent dans la BD
		String loginActuel = "karim";
		String nouveauLogin = "Arthur Bishop";
		int idUser = 31;
		int res = BaseDeDonnees.modifierLogin(loginActuel, nouveauLogin);
		String nouveauLoginApresInsertion = BaseDeDonnees.getLogin(idUser);
		assertTrue(res == 1 && (nouveauLoginApresInsertion == nouveauLogin));

		//Test2: Le login est déjà présent dans la BD
		nouveauLogin = "florian";
		res = BaseDeDonnees.modifierLogin(loginActuel, nouveauLogin);
		nouveauLoginApresInsertion = BaseDeDonnees.getLogin(idUser);
		assertTrue(res == BaseDeDonnees.LOGIN_EXISTANT && (nouveauLoginApresInsertion != nouveauLogin));
	}

	@Test
	public void loginExist_isCorrect()
	{
		//Le login existe
		BaseDeDonnees.connexionBD();
		String login = "florian";
		assertTrue(BaseDeDonnees.loginExists(login));

		//le login n'existe pas
		login = "adibou";
		assertFalse(!BaseDeDonnees.loginExists(login));

	}
}