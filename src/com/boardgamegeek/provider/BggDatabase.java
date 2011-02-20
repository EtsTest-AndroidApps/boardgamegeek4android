package com.boardgamegeek.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.boardgamegeek.provider.BggContract.Artists;
import com.boardgamegeek.provider.BggContract.BuddiesColumns;
import com.boardgamegeek.provider.BggContract.Collection;
import com.boardgamegeek.provider.BggContract.CollectionColumns;
import com.boardgamegeek.provider.BggContract.Designers;
import com.boardgamegeek.provider.BggContract.GameRanks;
import com.boardgamegeek.provider.BggContract.GameRanksColumns;
import com.boardgamegeek.provider.BggContract.Games;
import com.boardgamegeek.provider.BggContract.GamesColumns;
import com.boardgamegeek.provider.BggContract.Publishers;
import com.boardgamegeek.provider.BggContract.SyncColumns;
import com.boardgamegeek.provider.BggContract.SyncListColumns;
import com.boardgamegeek.util.CreateTableBuilder;
import com.boardgamegeek.util.CreateTableBuilder.COLUMN_TYPE;

public class BggDatabase extends SQLiteOpenHelper {
	private static final String TAG = "BggDatabase";

	private static final String DATABASE_NAME = "bgg.db";

	private static final int DATABASE_VERSION = 24;

	interface Tables {
		String DESIGNERS = "designers";
		String ARTISTS = "artists";
		String PUBLISHERS = "publishers";
		String GAMES = "games";
		String GAME_RANKS = "game_ranks";
		String GAMES_DESIGNERS = "games_designers";
		String GAMES_ARTISTS = "games_artists";
		String GAMES_PUBLISHERS = "games_publishers";
		String COLLECTION = "collection";
		String BUDDIES = "buddies";

		String GAMES_DESIGNERS_JOIN_DESIGNERS = GAMES_DESIGNERS + " "
			+ "LEFT OUTER JOIN designers ON games_designers.designer_id=designers.designer_id";

		String GAMES_ARTISTS_JOIN_ARTISTS = GAMES_ARTISTS + " "
			+ "LEFT OUTER JOIN artists ON games_artists.artist_id=artists.artist_id";

		String GAMES_PUBLISHERS_JOIN_PUBLISHERS = createJoin(GAMES_PUBLISHERS, PUBLISHERS, Publishers.PUBLISHER_ID);

		String COLLECTION_JOIN_GAMES = "collection "
			+ "LEFT OUTER JOIN games ON collection.game_id=games.game_id ";
	}
	
	private static String createJoin(String table1, String table2, String column){
		return table1 + " LEFT OUTER JOIN " + table2 + " ON " + table1 + "." + column + "=" + table2 + "." + column;
	}

	public interface GamesDesigners {
		String GAME_ID = "game_id";
		String DESIGNER_ID = "designer_id";
	}

	public interface GamesArtists {
		String GAME_ID = "game_id";
		String ARTIST_ID = "artist_id";
	}

	public interface GamesPublishers {
		String GAME_ID = Games.GAME_ID;
		String PUBLISHER_ID = Publishers.PUBLISHER_ID;
	}

	private interface References {
		String GAME_ID = "REFERENCES " + Tables.GAMES + "(" + Games.GAME_ID + ")";
		String DESIGNER_ID = "REFERENCES " + Tables.DESIGNERS + "(" + Designers.DESIGNER_ID + ")";
		String ARTIST_ID = "REFERENCES " + Tables.ARTISTS + "(" + Artists.ARTIST_ID + ")";
	}

	public BggDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		CreateTableBuilder builder = new CreateTableBuilder();

		db.execSQL("CREATE TABLE " + Tables.DESIGNERS + " ("
			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ SyncColumns.UPDATED + " INTEGER,"
			+ Designers.DESIGNER_ID + " INTEGER NOT NULL,"
			+ Designers.DESIGNER_NAME + " TEXT NOT NULL,"
			+ Designers.DESIGNER_DESCRIPTION + " TEXT,"
			+ "UNIQUE (" + Designers.DESIGNER_ID + ") ON CONFLICT IGNORE)");

		db.execSQL("CREATE TABLE " + Tables.ARTISTS + " ("
			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ SyncColumns.UPDATED + " INTEGER,"
			+ Artists.ARTIST_ID + " INTEGER NOT NULL,"
			+ Artists.ARTIST_NAME + " TEXT NOT NULL,"
			+ Artists.ARTIST_DESCRIPTION + " TEXT,"
			+ "UNIQUE (" + Artists.ARTIST_ID + ") ON CONFLICT IGNORE)");

		builder.reset().table(Tables.PUBLISHERS).defaultPrimaryKey()
			.column(SyncColumns.UPDATED, COLUMN_TYPE.INTEGER)
			.column(Publishers.PUBLISHER_ID, COLUMN_TYPE.INTEGER, true, true)
			.column(Publishers.PUBLISHER_NAME, COLUMN_TYPE.TEXT, true)
			.column(Publishers.PUBLISHER_DESCRIPTION, COLUMN_TYPE.TEXT)
			.create(db);

		db.execSQL("CREATE TABLE " + Tables.GAMES + " ("
			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ SyncColumns.UPDATED + " INTEGER,"
			+ SyncListColumns.UPDATED_LIST + " INTEGER NOT NULL,"
			+ GamesColumns.GAME_ID + " INTEGER NOT NULL,"
			+ GamesColumns.GAME_NAME + " TEXT NOT NULL,"
			+ GamesColumns.GAME_SORT_NAME + " TEXT NOT NULL,"
			+ GamesColumns.YEAR_PUBLISHED + " INTEGER,"
			+ GamesColumns.IMAGE_URL + " TEXT,"
			+ GamesColumns.THUMBNAIL_URL + " TEXT,"
			+ GamesColumns.MIN_PLAYERS + " INTEGER,"
			+ GamesColumns.MAX_PLAYERS + " INTEGER,"
			+ GamesColumns.PLAYING_TIME + " INTEGER,"
			+ GamesColumns.NUM_OWNED + " INTEGER,"
			+ GamesColumns.NUM_PLAYS + " INTEGER NOT NULL DEFAULT 0,"
			+ GamesColumns.MINIMUM_AGE + " INTEGER,"
			+ GamesColumns.DESCRIPTION + " TEXT,"
			+ GamesColumns.STATS_USERS_RATED + " INTEGER,"
			+ GamesColumns.STATS_AVERAGE + " REAL,"
			+ GamesColumns.STATS_BAYES_AVERAGE + " REAL,"
			+ GamesColumns.STATS_STANDARD_DEVIATION + " REAL,"
			+ GamesColumns.STATS_MEDIAN + " INT,"
			+ GamesColumns.STATS_NUMBER_OWNED + " INT,"
			+ GamesColumns.STATS_NUMBER_TRADING + " INT,"
			+ GamesColumns.STATS_NUMBER_WANTING + " INT,"
			+ GamesColumns.STATS_NUMBER_WISHING + " INT,"
			+ GamesColumns.STATS_NUMBER_COMMENTS + " INT,"
			+ GamesColumns.STATS_NUMBER_WEIGHTS + " INT,"
			+ GamesColumns.STATS_AVERAGE_WEIGHT + " REAL,"
			+ "UNIQUE (" + GamesColumns.GAME_ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.GAME_RANKS + " ("
			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ GameRanks.GAME_ID + " TEXT " + References.GAME_ID + ","
			+ GameRanksColumns.GAME_RANK_ID + " INTEGER NOT NULL,"
			+ GameRanksColumns.GAME_RANK_TYPE + " TEXT NOT NULL,"
			+ GameRanksColumns.GAME_RANK_NAME + " TEXT NOT NULL,"
			+ GameRanksColumns.GAME_RANK_FRIENDLY_NAME + " TEXT NOT NULL,"
			+ GameRanksColumns.GAME_RANK_VALUE + " INTEGER NOT NULL,"
			+ GameRanksColumns.GAME_RANK_BAYES_AVERAGE + " REAL,"
			+ "UNIQUE (" + GameRanksColumns.GAME_RANK_ID + "," + GameRanks.GAME_ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Tables.GAMES_DESIGNERS + " ("
			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ GamesDesigners.GAME_ID + " INTEGER NOT NULL " + References.GAME_ID + ","
			+ GamesDesigners.DESIGNER_ID + " INTEGER NOT NULL " + References.DESIGNER_ID + ","
			+ "UNIQUE (" + GamesDesigners.GAME_ID + "," + GamesDesigners.DESIGNER_ID + ") ON CONFLICT IGNORE)");

		db.execSQL("CREATE TABLE " + Tables.GAMES_ARTISTS + " ("
			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ GamesArtists.GAME_ID + " INTEGER NOT NULL " + References.GAME_ID + ","
			+ GamesArtists.ARTIST_ID + " INTEGER NOT NULL " + References.ARTIST_ID + ","
			+ "UNIQUE (" + GamesArtists.GAME_ID + "," + GamesArtists.ARTIST_ID + ") ON CONFLICT IGNORE)");
		
		builder.reset().table(Tables.GAMES_PUBLISHERS).defaultPrimaryKey()
			.column(GamesPublishers.GAME_ID, COLUMN_TYPE.INTEGER, true, true, Tables.GAMES, Games.GAME_ID)
			.column(GamesPublishers.PUBLISHER_ID, COLUMN_TYPE.INTEGER, true, true, Tables.PUBLISHERS, Publishers.PUBLISHER_ID)
			.create(db);

		db.execSQL("CREATE TABLE " + Tables.COLLECTION + " ("
			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ SyncColumns.UPDATED + " INTEGER,"
			+ SyncListColumns.UPDATED_LIST + " INTEGER NOT NULL,"
			+ Collection.GAME_ID + " TEXT " + References.GAME_ID + ","
			+ CollectionColumns.COLLECTION_ID + " INTEGER NOT NULL,"
			+ CollectionColumns.COLLECTION_NAME + " TEXT NOT NULL,"
			+ CollectionColumns.COLLECTION_SORT_NAME + " TEXT NOT NULL,"
			+ CollectionColumns.STATUS_OWN + " INTEGER NOT NULL DEFAULT 0,"
			+ CollectionColumns.STATUS_PREVIOUSLY_OWNED + " INTEGER NOT NULL DEFAULT 0,"
			+ CollectionColumns.STATUS_FOR_TRADE + " INTEGER NOT NULL DEFAULT 0,"
			+ CollectionColumns.STATUS_WANT + " INTEGER NOT NULL DEFAULT 0,"
			+ CollectionColumns.STATUS_WANT_TO_PLAY + " INTEGER NOT NULL DEFAULT 0,"
			+ CollectionColumns.STATUS_WANT_TO_BUY + " INTEGER NOT NULL DEFAULT 0,"
			+ CollectionColumns.STATUS_WISHLIST + " INTEGER NOT NULL DEFAULT 0,"
			+ CollectionColumns.STATUS_PREORDERED + " INTEGER NOT NULL DEFAULT 0,"
			+ CollectionColumns.COMMENT + " TEXT,"
			+ CollectionColumns.PRIVATE_INFO_PRICE_PAID_CURRENCY + " TEXT,"
			+ CollectionColumns.PRIVATE_INFO_PRICE_PAID + " REAL,"
			+ CollectionColumns.PRIVATE_INFO_CURRENT_VALUE_CURRENCY + " TEXT,"
			+ CollectionColumns.PRIVATE_INFO_CURRENT_VALUE + " REAL,"
			+ CollectionColumns.PRIVATE_INFO_QUANTITY + " INTEGER,"
			+ CollectionColumns.PRIVATE_INFO_ACQUISITION_DATE + " TEXT,"
			+ CollectionColumns.PRIVATE_INFO_ACQUIRED_FROM + " TEXT,"
			+ CollectionColumns.PRIVATE_INFO_COMMENT + " TEXT,"
			+ "UNIQUE (" + CollectionColumns.COLLECTION_ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.BUDDIES + " ("
			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ SyncColumns.UPDATED + " INTEGER,"
			+ SyncListColumns.UPDATED_LIST + " INTEGER NOT NULL,"
			+ BuddiesColumns.BUDDY_ID + " INTEGER NOT NULL,"
			+ BuddiesColumns.BUDDY_NAME + " TEXT NOT NULL,"
			+ BuddiesColumns.BUDDY_FIRSTNAME + " TEXT,"
			+ BuddiesColumns.BUDDY_LASTNAME + " TEXT,"
			+ BuddiesColumns.AVATAR_URL + " TEXT,"
			+ "UNIQUE (" + BuddiesColumns.BUDDY_ID + ") ON CONFLICT REPLACE)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

		if (oldVersion != DATABASE_VERSION) {
			Log.w(TAG, "Destroying old data during upgrade");

			db.execSQL("DROP TABLE IF EXISTS " + Tables.DESIGNERS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.ARTISTS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.PUBLISHERS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.GAMES);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.GAME_RANKS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.GAMES_DESIGNERS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.GAMES_ARTISTS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.GAMES_PUBLISHERS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.COLLECTION);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.BUDDIES);

			onCreate(db);
		}
	}
}
