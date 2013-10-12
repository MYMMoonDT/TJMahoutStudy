package org.tongji.mahoutplatform.mahout_recommender.data;

public enum Genre {
	UNKNOWN,
	ACTION,
	ADVENTURE,
	ANIMATION,
	CHILDREN,
	COMEDY,
	CRIME,
	DOCUMENTARY,
	DRAMA,
	FANTASY,
	FILM_NOIR,
	HORROR,
	MUSICAL,
	MYSTERY,
	ROMANCE,
	SCI_FI,
	THRILLER,
	WAR,
	WESTERN;
	private final static int GENRE_NUM = 19;
	public static int getGenreNum(){
		return GENRE_NUM;
	}
}
