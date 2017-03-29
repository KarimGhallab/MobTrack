package com.example.karim.test_osm;

import android.os.StrictMode;
import android.util.Log;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.osmdroid.util.GeoPoint;
import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/** Classe Abstraite permettant toutes les interactions avec la base de données.
 */

public abstract class BaseDeDonnees
{
    public static final int LOGIN_EXISTANT = -5;

	private static Connection chConn = null;
    private static Statement chStmt = null;
    private static String chUrl = "jdbc:mysql://89.234.180.47:3306/w7zgqz_mobtrack";
    private static String chUser = "w7zgqz_mobtrack";
	private static String chPassword = "trackmob";

	/**
	 * Méthode statique de connexion à la base de données.
	 * @return true si la connexion s'est bien déroulée, false sinon.
	 */
	public static boolean connexionBD()
    {
		try
		{
			//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().build());

			Class.forName("com.mysql.jdbc.Driver");		//chargement du driver
			chConn = DriverManager.getConnection(chUrl, chUser, chPassword);
			chStmt = chConn.createStatement();
			return true;	//La connexion s'est bien déroulée
		}
		catch (Exception excep)
		{
			Log.d("error connexion", excep.getMessage());
			return false;	//erreur de connexion
		}
    }

	/**
	 * Méthode statique modifiant le login de l'utilisateur dans la base de données.
	 * @param parLoginUtilisateur Le login actuel de l'utilisateur.
	 * @param parNouveauLogin Le nouveau login souhaité par l'utilisateur.
	 * @return 1 si la modification s'est bien déroulée, LOGIN_DEJA_EXISTANT si le login existe déjà dans la base de données ou -1 si'il y a une autre erreur.
	 */

	public static int modifierLogin(String parLoginUtilisateur, String parNouveauLogin)
	{
		if (BaseDeDonnees.loginExists(parNouveauLogin))
			return BaseDeDonnees.LOGIN_EXISTANT;
		else
		{
			try
			{
				if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
					BaseDeDonnees.connexionBD();
				String sql = "UPDATE utilisateur SET pseudo = '"+parNouveauLogin+"' where pseudo = '"+parLoginUtilisateur+"';";
				chStmt.executeUpdate(sql);
				return 1;
			}
			catch (Exception e)
			{
				Log.e("error modif login", e.getMessage());
				return -1;
			}
		}
	}

	/**
	 * Méthode statique modifiant la ville de l'utilisateur dans la base de données.
	 * @param parLoginUtilisateur Le login de l'utilisateur.
	 * @param parNouvelleVille La nouvelle ville souhaitée par l'utilisateur.
	 * @return true si la modification s'est bien déroulée, false dans le cas contraire.
	 */
	public static boolean modifierVille(String parLoginUtilisateur, String parNouvelleVille)
	{
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			String sql = "UPDATE utilisateur SET ville = '"+parNouvelleVille+"' where pseudo = '"+parLoginUtilisateur+"';";
			chStmt.executeUpdate(sql);
			return true;
		}
		catch (Exception e)
		{
			Log.e("error modif ville", e.getMessage());
			return false;
		}
	}

	/**
	 * Méthode statique modifiant le mail de l'utilisateur dans la base de données.
	 * @param parLoginUtilisateur Le login actuel de l'utilisateur.
	 * @param parNouveauMail Le nouveau mail souhaité par l'utilisateur.
	 * @return true si la modification s'est bien déroulée, false dans le cas contraire.
	 */
	public static boolean modifierMail(String parLoginUtilisateur, String parNouveauMail)
	{
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			String sql = "UPDATE utilisateur SET mail = '"+parNouveauMail+"' where pseudo = '"+parLoginUtilisateur+"';";
			chStmt.executeUpdate(sql);
			return true;
		}
		catch (Exception e)
		{
			Log.e("error modif mail", e.getMessage());
			return false;
		}
	}

	/**
	 * Méthode statique modifiant le mot de passe de l'utilisateur dans la base de données.
	 * @param parLoginUtilisateur Le login actuel de l'utilisateur.
	 * @param parNouveauMotDePasse Le nouveau mot de passe souhaité par l'utilisateur.
	 * @return true si la modification s'est bien déroulée, false dans le cas contraire.
	 */
	public static boolean modifierMdp(String parLoginUtilisateur, String parNouveauMotDePasse)
	{
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			String mdpChiffre = BaseDeDonnees.chiffreViaSha256(parNouveauMotDePasse);
			String sql = "UPDATE utilisateur SET pass = '"+mdpChiffre+"' where pseudo = '"+parLoginUtilisateur+"';";
			chStmt.executeUpdate(sql);
			return true;
		}
		catch (Exception e)
		{
			Log.e("error modif mdp", e.getMessage());
			return false;
		}
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
			String mdpChiffre = chiffreViaSha256(parMdp);
			String sql = "INSERT INTO utilisateur(pseudo, IMEI, pass, mail, ville) VALUES ('"+parLogin+"', '"+parIMEI+"', '"+mdpChiffre+"', '"+parMail+"', '"+parVille+"');";
			try
			{
				if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
					BaseDeDonnees.connexionBD();

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
		Log.d("dans BD", parIMEI);
		String sql = "INSERT INTO utilisateur(IMEI) VALUES ('"+parIMEI+"');";
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			chStmt.executeUpdate(sql);
			return true;
		}
		catch (Exception excep)
		{
			Log.e("error", excep.getMessage());
			return false;
		}
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
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();
			String mdpChiffre = chiffreViaSha256(parMotDePasse);
			Log.d("BD", "MDP chiffré : "+mdpChiffre);
			String sql = "select count(*) from (select * from utilisateur where pseudo = '" + parLogin + "' and pass = '"+ mdpChiffre +"') as resultat;";
			Log.d("BD", sql);
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
			Log.e("error", excep.getMessage());
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
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

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

	public static int getUserIDViaIMEI(String parIMEI)
	{
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			String sql = "SELECT id FROM utilisateur WHERE IMEI = '"+parIMEI+"';";
			ResultSet rs = chStmt.executeQuery(sql);
			rs.next();
			return rs.getInt(1);
		}
		catch(Exception excep)
		{
			Log.e("error", excep.getMessage());
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
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

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
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

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
	 * @return L'id du parcours qui est créé, -1 s'il y a eu une erreur.
	 */
	public static int insererNouveauParcours(int parIdUser)
	{
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			String sql = "INSERT INTO parcours(idutilisateur) VALUES ('"+parIdUser+"');";
			chStmt.executeUpdate(sql);
			Log.d("Insertion SQL", sql);
			return BaseDeDonnees.getIdParcoursActuel(parIdUser);
		}
		catch (Exception e)
		{
			Log.e("insertion parcours", e.getMessage());
			return -1;
		}
	}

	/**
	 * Méthode statique renvoyant le numero du parcours actuellement effectué par l'utilisateur.
	 * @param parIdUser L'Id de l'utilisateur dans la base de données.
	 * @return L'id du parcours que réalise l'utilisateur, -1 s'il y a une erreur.
	 */
	public static int getIdParcoursActuel(int parIdUser)
	{
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			String sql = "SELECT idparcours FROM parcours p1 WHERE idutilisateur='"+parIdUser+"' and date in (SELECT max(date) FROM parcours p2 WHERE p2.idutilisateur='"+parIdUser+"')";
			ResultSet rs = chStmt.executeQuery(sql);
			rs.next();
			int idParcours = rs.getInt(1);    //Obtention de l'id du nouveau parcours
			return idParcours;
		}
		catch (Exception e)
		{
			Log.e("Erreur id parcours", e.getMessage());
			return -1;
		}
	}

	/**
	 * Méthode d'insertion d'une nouvelle localisation dans un parcours.
	 * @param parIdParcours Le parcours auquel on ajoute la localisation.
	 * @param parLatitude La latitude de la nouvelle localisation.
	 * @param parLongitude La longitude de la nouvelle localisation
	 * @param parDistance La distance avec la précédente localisation.
	 * @return true si l'insertion s'est bien déroulé, false dans la cas contraire.
	 */
	public static boolean insererNouvelleLocalisation(int parIdParcours, double parLatitude, double parLongitude, double parDistance)
	{
		//INSERT INTO localisations (idparcours, latitude, longitude, distance) VALUES (idparcours, latitude, longitude, distance)
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			String sql = "INSERT INTO localisations (idparcours, latitude, longitude, distance) VALUES ('"+parIdParcours+"', '"+parLatitude+"', '"+parLongitude+"', '"+parDistance+"');";
			chStmt.executeUpdate(sql);
			return true;
		}
		catch (Exception e)
		{
			Log.e("nouvelle loc", e.getMessage());
			return false;
		}
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
		try
		{
			if (chConn != null &&!chConn.isClosed())
				chConn.close();
			return true;
		}
		catch (SQLException e)
		{
			Log.e("Erreur déco", e.getMessage());
			return false;
		}
	}

	/**
	 * Méthode permettant de mettre à jour la distance totale effectuée lors d'un parcours.
	 * @param idParcours L'id du parcours auquel on souhaite modifié la distance totale.
	 * @param parDistance La distance que l"on souhaite envoyer à la base de données.
	 * @return true si l'insertion s'est bien déroulée, false dans le cas contraire.
	 */
	public static boolean updateDistanceTotale(int idParcours, double parDistance)
	{
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			String sql = "UPDATE parcours SET distance_totale = "+parDistance+" where idparcours = "+idParcours+";";
			Log.d("Mise à jour sql", sql);
			chStmt.executeUpdate(sql);
			return true;
		}
		catch (Exception e)
		{
			Log.e("error distanceT", e.getMessage());
			return false;
		}
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
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

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

	public static boolean IMEIExists(String parIMEI)
	{
		try
		{
			Log.d("BD", "chConn est null : "+(chConn == null));
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			String sql = "SELECT count(*) from utilisateur where IMEI = '"+parIMEI+"';";
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

	public static ArrayList getTrajets(int parIdUser)
	{
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			ArrayList< ArrayList<String> > trajets = new ArrayList<>();

			String sql = "SELECT idparcours, date FROM parcours WHERE idutilisateur = "+parIdUser+" ORDER BY date DESC;";
			ResultSet rs = chStmt.executeQuery(sql);
			while(rs.next())
			{
				int idparcours = rs.getInt(1);
				Timestamp date = rs.getTimestamp(2);
				String dateFormatee = new SimpleDateFormat("EEEE dd MMMM HH:mm y").format(date);

				ArrayList<String> ligne = new ArrayList<>(2);
				ligne.add(String.valueOf(idparcours));
				ligne.add(dateFormatee);
				trajets.add(ligne);
			}
			return trajets;
		}
		catch (Exception ex)
		{
			Log.e("error", ex.getMessage());
			return null;
		}
	}

	public static ArrayList getLocalisation(int parIdParcours)
	{
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			ArrayList <GeoPoint> localisations = new ArrayList<>();

			String sql = "SELECT latitude, longitude FROM localisations WHERE idparcours = "+parIdParcours+";";
			ResultSet rs = chStmt.executeQuery(sql);
			while(rs.next())
			{
				double latitude = rs.getDouble(1);
				double longitude = rs.getDouble(2);
				localisations.add(new GeoPoint(latitude, longitude));
			}
			Log.d("BD", localisations.toString());
			return localisations;
		}
		catch (Exception ex)
		{
			Log.e("error", ex.getMessage());
			return null;
		}
	}

	/**
	 * Renvoie la distance totale parcourue par un utilisateur.
	 * @param parIdUtilisateur L'id de l'utilisateur.
	 * @return La distance totale parcourue par l'utilisateur. Ou -1 s'il y a eu une erreur.
	 */
	public static double getDistanceParcourue(int parIdUtilisateur)
	{
		try
		{
			if (BaseDeDonnees.chConn == null || BaseDeDonnees.chConn.isClosed())
				BaseDeDonnees.connexionBD();

			String sql = "SELECT sum(distance_totale) FROM parcours WHERE idutilisateur = "+parIdUtilisateur+";";
			ResultSet rs = chStmt.executeQuery(sql);

			rs.next();
			return rs.getDouble(1);
		}
		catch (Exception ex)
		{
			Log.e("error", ex.getMessage());
			return -1;
		}
	}

	private static String chiffreViaSha256(String aChiffrer)
	{
		return new String(Hex.encodeHex(DigestUtils.sha256(aChiffrer)));
	}
}