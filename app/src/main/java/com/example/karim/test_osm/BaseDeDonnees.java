package com.example.karim.test_osm;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.util.Log;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;

/** Classe Abstraite permettant toutes les interactions avec la base de données.
 */

public abstract class BaseDeDonnees
{
    private static Connection chConn;
    private static Statement chStmt;
    private static String chUrl = "jdbc:mysql://89.234.180.47:3306/w7zgqz_mobtrack";
    private static String chUser = "w7zgqz_mobtrack";
	private static String chPassword = "trackmob";

	/**
	 * Méthode statique de connexion à la base de données.
	 * @return true si la connexion s'est bien déroulée, false sinon.
	 */
	public static boolean connexionBD()
    {
		/*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);*/
		try
		{
			Class.forName("com.mysql.jdbc.Driver");		//chargement du driver
			chConn = DriverManager.getConnection(chUrl, chUser, chPassword);
			chStmt = chConn.createStatement();
			return true;	//La connexion s'est bien déroulée
		}
		catch (Exception excep)
		{
			return false;	//erreur de connexion
		}
    }

	/**
	 * Méthode statique modifiant le login de l'utilisateur dans la base de données.
	 * @param parLoginUtilisateur Le login actuel de l'utilisateur.
	 * @param parNouveauLogin Le nouveau login souhaité par l'utilisateur.
	 * @return true si la modification s'est bien déroulée, false dans le cas contraire.
	 */
	public static boolean modifierLogin(String parLoginUtilisateur, String parNouveauLogin)
	{
		return true;
	}

	/**
	 * Méthode statique modifiant la ville de l'utilisateur dans la base de données.
	 * @param parLoginUtilisateur Le login de l'utilisateur.
	 * @param parNouvelleVille La nouvelle ville souhaitée par l'utilisateur.
	 * @return true si la modification s'est bien déroulée, false dans le cas contraire.
	 */
	public static boolean modifierVille(String parLoginUtilisateur, String parNouvelleVille)
	{
		return true;
	}

	/**
	 * Méthode statique modifiant le mot de passe de l'utilisateur dans la base de données.
	 * @param parLoginUtilisateur Le login actuel de l'utilisateur.
	 * @param parNouveauMotDePasse Le nouveau mot de passe souhaité par l'utilisateur.
	 * @return true si la modification s'est bien déroulée, false dans le cas contraire.
	 */
	public static boolean modifierMdp(String parLoginUtilisateur, String parNouveauMotDePasse)
	{
		return true;
	}

	/**
	 * Méthode statique d'insertion d'un utilisateur dans la base de données.
	 * @param parLogin Le login de l'utilisateur.
	 * @param parMdp Le mot de passe de l'utilisateur.
	 * @param parMail Le mail de l'utilisateur.
	 * @param parVille La ville de l'utilisateur.
	 * @param  parIMEI L'IMEI de l'utilisateur.
	 * @return 0 si l'insertion s'est bien déroulée, 1 si il y à eu une erreur dans la connexion avec la base de données, ou 2 si le login existe déjà.
	 */
    public static int insererUtilisateur(String parLogin, String parMdp, String parMail, String parVille, String parIMEI)
    {
		if (BaseDeDonnees.loginExists(parLogin) == true)
			return 2;
		else
		{
			String sql = "INSERT INTO utilisateur(pseudo, IMEI, pass, mail, ville) VALUES ('"+parLogin+"', '"+parIMEI+"', '"+parMdp+"', '"+parMail+"', '"+parVille+"');";
			try
			{
				chStmt.executeUpdate(sql);
				return 0;
			}
			catch (Exception excep)
			{
				return 1;
			}
		}
    }

	/**
	 * Méthode statique d'insertion dans la base de données d'un utilisateur anonyme.
	 * @param parIMEI L'IMEI du nouvel utilisateur anonyme.
	 * @return true si l'utilisateur anonyme a bien été créé dans la base de données, false dans le cas contraire.
	 */
	public static boolean insererUtilisateurAnonyme(String parIMEI)
	{
		return true;
	}

	/**
	 * Méthode statique de vérification de connexion d'un utilisateur.
	 * @param parLogin Le login de l'utilisateur qui souhaite se connecter.
	 * @param parMotDePasse Le mot de passe de l'utilisateur qui souhaite se connecter.
	 * @return true si l'utilisateur est autorisé à se connecter, false dans le cas contraire.
	 */
	public static boolean verifierUtilisateur(String parLogin, String parMotDePasse)
	{
		try
		{
			String sql = "select count(*) from (select * from utilisateur where pseudo = '" + parLogin + "' and pass = '" + parMotDePasse + "') as resultat;";
			ResultSet rs = chStmt.executeQuery(sql);
			rs.next();
			int nb_resultat = rs.getInt(1);    //obtention du nombre de ligne retourné par la requete
			if (nb_resultat == 1)	//Le couple login/mot de passe existe dans la base de données
				return true;
			else
				return false;
		}
		catch(Exception excep)
		{
			return false;
		}
	}

	/**
	 * Méthode statique de vérification de présence dans la base de données d'un utilisateur anonyme.
	 * @param parIMEI L'IMEI de l'utilisateur qui souhaite se connecter.
	 * @return true si l'utilisateur Anonyme existe dans la base de données, false dans le cas contraire.
	 */
	public static boolean verifierUtilisateurAnonyme(String parIMEI)
	{
		return true;
	}

	/**
	 * Méthode statique d'obtention d'un objet de la classe Utilisateur à partir du login d'un tutilisateur présent dans la base de données.
	 * @param parLogin Le login de l'utilisateur.
	 * @return Un Utilisateur ou null en cas d'erreur.
	 */
	public static Utilisateur getUtilisateur(String parLogin)
	{
		int id = getUserID(parLogin);
		String ville = getUserVille(parLogin);
		String mail = getUserMail(parLogin);
		if ((id == -1) || (ville == null) || (mail == null))
			return null;
		else
			return new Utilisateur(parLogin, ville, mail, id);
	}
	/**
	 * Méthode statique de réception de l'id de l'utilisateur dans la base de données.
	 * @param parLogin Le login de l'utilisateur.
	 * @return L'id de l'utilisateur dans la base de donnée ou -1 s'il y à un problème.
	 */
	public static int getUserID(String parLogin)
	{
		try
		{
			String sql = "SELECT id FROM utilisateur WHERE pseudo = '"+parLogin+"';";
			ResultSet rs = chStmt.executeQuery(sql);
			rs.next();
			return rs.getInt(1);
		}
		catch(Exception excep)
		{
			return -1;
		}
	}

	/**
	 * Méthode statique de réception de la ville de l'utilisateur.
	 * @param parLogin Le login de l'utilisateur.
	 * @return La ville de l'utilisateur ou null s'il y à un problème.
	 */
	public static String getUserVille(String parLogin)
	{
		try
		{
			String sql = "SELECT ville FROM utilisateur WHERE pseudo = '"+parLogin+"';";
			ResultSet rs = chStmt.executeQuery(sql);
			rs.next();
			return rs.getString(1);
		}
		catch(Exception excep)
		{
			return null;
		}
	}

	/**
	 * Méthode statique de réception du mail de l'utilisateur.
	 * @param parLogin Le login de l'utilisateur.
	 * @return Le mail de l'utilisateur ou null s'il y à un problème.
	 */
	public static String getUserMail(String parLogin)
	{
		try
		{
			String sql = "SELECT mail FROM utilisateur WHERE pseudo = '"+parLogin+"';";
			ResultSet rs = chStmt.executeQuery(sql);
			rs.next();
			return rs.getString(1);
		}
		catch(Exception excep)
		{
			return null;
		}
	}

	/**
	 * Méthode statique d'insertion d'un nouveau parcours dans la base de données pour un utilisateur.
	 * @param parIdUser L'id de l'utilisateur dans la base de données.
	 * @return true si l'insertion s'est bien déroulée, false dans la cas contraire.
	 */
	public static boolean insererNouveauParcours(int parIdUser)
	{
		return true;
	}

	/**
	 * Méthode statique renvoyant le numero du parcours actuellement effectué par l'utilisateur.
	 * @param parIdUser L'Id de l'utilisateur dans la base de données.
	 * @return L'id du parcours que réalise l'utilisateur.
	 */
	public static int getIdParcoursActuel(int parIdUser)
	{
		return 0;
	}

	/**
	 * Méthode d'insertion d'une nouvelle localisation dans un parcours.
	 * @param parIdParcours Le parcours auquel on ajoute la localisation.
	 * @param parLatitude La latitude de la nouvelle localisation.
	 * @param parLongitude La longitude de la nouvelle localisation
	 * @param parDistance La distance avec la précédente localisation.
	 * @return true si l'insertion s'est bien déroulé, false dans la cas contraire.
	 */
	public static boolean insererNouvelleLocalisation(int parIdParcours, double parLatitude, double parLongitude, int parDistance)
	{
		return true;
	}

	/**
	 * Méthode statique permettant d'inserer des localisation pour un parcours à partir d'un fichier.
	 * @param parIdParcours Le parcours auquel on ajoute la localisation.
	 * @param parFichier Le fichier contenant le(s) localisation(s).
	 * @return true si l'insertion s'est bien déroulés, false dans le cas contraire.
	 */
	public static boolean insererNouvelleLocalisation(int parIdParcours, File parFichier)
	{
		return true;
	}

	/**
	 * Méthode de déconnexion à la base de données.
	 * @return true si la déconexion s'est bien déroulée.
	 */
	public static boolean deconnexionBD()
	{
		return true;
	}

	/**
	 * Méthode permettant de mettre à jour la distance totale effectuée lors d'un parcours.
	 * @param idParcours L'id du parcours auquel on souhaite modifié la distance totale.
	 * @param parDistance La distance que l"on souhaite envoyer à la base de données.
	 * @return true si l'insertion s'est bien déroulée, false dans le cas contraire.
	 */
	public static boolean updateDistanceTotale(int idParcours, int parDistance)
	{
		return true;
	}

	/**
	 * Méthode permettant de verifier si un login est présent dans la base de données.
	 * @param parLogin Le login à vérifier.
	 * @return trus si le login est présent, false sinon.
	 */
	public static boolean loginExists(String parLogin)
	{
		try
		{
			String sql = "SELECT count(*) from utilisateur where pseudo = '"+parLogin+"';";
			ResultSet rs = chStmt.executeQuery(sql);
			rs.next();
			int nbResultat = rs.getInt(1);
			if (nbResultat >= 1 )
				return true;
			else
				return false;
		}
		catch (Exception excep)
		{
			return true;
		}
	}

	/* Methodes utilisées pour les tests unitaires*/

	public static String getLogin(int parIdUser)
	{
		return null;
	}

	public static String getMdp(String parLogin)
	{
		return null;
	}

	public static String getIMEI(String parLogin)
	{
		return null;
	}
}
