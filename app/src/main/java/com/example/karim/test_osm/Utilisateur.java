package com.example.karim.test_osm;


import android.os.Parcelable;

/**
 * Classe Utilisateur, elle représente l'utilisateur actuellement connecté.
 * Cet Utilisateur peut aussi bien être un utilisateur anonyme que connecté avec un login et un mot de passe.
 * Elle est composé du login de l'utilisateur, de sa ville, de son mail et de son ID dans la base de données du projet.
 */

public class Utilisateur implements Parcelable
{
	private String chLogin;
	private String chVille;
	private String chMail;
	private int chID;
	private int estAnonyme;

	/**
	 * Constructeur d'un Utilisateur se connectant à l'application avec un login et un mot de passe.
	 * @param parLogin Le login de l'utilisateur.
	 * @param parVille La ville de l'utilisateur.
	 * @param parMail Le mail de l'utilisateur.
	 * @param parID L'ID dans la base de données du projet de l'utilisateur.
	 */
	public Utilisateur (String parLogin, String parVille, String parMail, int parID)
	{
		chLogin = parLogin;
		chVille = parVille;
		chMail = parMail;
		chID = parID;
		estAnonyme = 0;
	}

	/**
	 * Constructeur d'un utilisateur anonyme, seul son ID dans la base de données est stocké.
	 * @param parID L'ID dans la base de données du projet de l'utilisateur.
	 */
	public Utilisateur(int parID)
	{
		chID = parID;
		estAnonyme = 1;
	}

	/**
	 * Accesseur au login de l'utilisateur.
	 * @return Le login de l'utilisateur.
	 */
	public String getLogin()
	{
		return chLogin;
	}

	/**
	 * Modifieur du login de l'utilisateur.
	 * @param parLogin Le nouveau login de l'utilisateur.
	 */
	public void setLogin(String parLogin)
	{
		chLogin = parLogin;
	}

	/**
	 * Accesseur au mail de l'utilisateur
	 * @return Le mail de l'utilisateur
	 */
	public String getMail()
	{
		return chMail;
	}

	/**
	 * Modifieur du mail de l'utilisateur
	 * @param parMail Le nouveau mail de l'utilisateur
	 */
	public void setMail(String parMail)
	{
		chMail = parMail;
	}

	/**
	 * Accesseur à la ville de l'utilisateur
	 * @return La nouvelle ville de l'utilisateur
	 */
	public String getVille()
	{
		return chVille;
	}

	/**
	 * Modifieur de la ville de l'utilisateur
	 * @param parVille La ville de l'utilisateur
	 */
	public void setVille(String parVille)
	{
		chVille = parVille;
	}

	/**
	 * Accesseur à l'ID dans la base de données de l'utilisateur
	 * @return L'ID dans la base de données de l'utilisateur
	 */
	public int getID()
	{
		return chID;
	}

	public int getAnonymat()
	{
		return estAnonyme;
	}

	public String toString()
	{
		String resultat = "Login Utilsateur: "+chLogin+
				"\n Ville Utilisateur: "+chVille+
				"\n Mail Utilisateur: "+chMail+
				"\n ID Utilisateur: "+chID;
		return resultat;
	}


	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(android.os.Parcel dest, int flags)
	{
		dest.writeString(chLogin);
		dest.writeString(chMail);
		dest.writeString(chVille);
		dest.writeInt(chID);
		dest.writeInt(estAnonyme);
	}

	private Utilisateur(android.os.Parcel in)
	{
		chLogin = in.readString();
		chMail = in.readString();
		chVille = in.readString();
		chID = in.readInt();
		estAnonyme = in.readInt();
	}

	public static final Parcelable.Creator<Utilisateur> CREATOR = new Parcelable.Creator<Utilisateur>()
	{
		public Utilisateur createFromParcel(android.os.Parcel in)
		{
			return new Utilisateur(in);
		}

		public Utilisateur[] newArray(int size) {
			return new Utilisateur[size];
		}
	};
}