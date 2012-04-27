package app.android.bustime.local;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbOpenHelper extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "BusTime";

	public DbOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();

		try {
			db.execSQL(buildRoutesTableCreationQuery());
			db.execSQL(buildTripsTableCreationQuery());
			db.execSQL(buildStationsTableCreationQuery());
			db.execSQL(buildRoutesAndStationsTableCreationQuery());

			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}

	private String buildRoutesTableCreationQuery() {
		StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append(String.format("create table %s ", DbTableNames.ROUTES));

		queryBuilder.append("(");
		queryBuilder.append(String.format("%s %s, ", DbFieldNames.ID, DbFieldParameters.ID));
		queryBuilder.append(String.format("%s %s", DbFieldNames.NAME, DbFieldParameters.ROUTE_NAME));
		queryBuilder.append(")");

		return queryBuilder.toString();
	}

	private String buildTripsTableCreationQuery() {
		StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append(String.format("create table %s ", DbTableNames.TRIPS));

		queryBuilder.append("(");
		queryBuilder.append(String.format("%s %s, ", DbFieldNames.ID, DbFieldParameters.ID));
		queryBuilder.append(String.format("%s %s, ", DbFieldNames.ROUTE_ID,
			DbFieldParameters.TRIP_ROUTE_ID));
		queryBuilder.append(String.format("%s %s", DbFieldNames.DEPARTURE_TIME,
			DbFieldParameters.TRIP_DEPARTURE_TIME));
		queryBuilder.append(")");

		return queryBuilder.toString();
	}

	private String buildStationsTableCreationQuery() {
		StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append(String.format("create table %s ", DbTableNames.STATIONS));

		queryBuilder.append("(");
		queryBuilder.append(String.format("%s %s, ", DbFieldNames.ID, DbFieldParameters.ID));
		queryBuilder.append(String.format("%s %s", DbFieldNames.NAME, DbFieldParameters.STATION_NAME));
		// TODO: Do not forget about coordinates later
		queryBuilder.append(")");

		return queryBuilder.toString();
	}

	private String buildRoutesAndStationsTableCreationQuery() {
		StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append(String.format("create table %s ", DbTableNames.ROUTES_AND_STATIONS));

		queryBuilder.append("(");
		queryBuilder.append(String.format("%s %s, ", DbFieldNames.ID, DbFieldParameters.ID));
		queryBuilder.append(String.format("%s %s, ", DbFieldNames.ROUTE_ID,
			DbFieldParameters.ROUTES_AND_STATIONS_ROUTE_ID));
		queryBuilder.append(String.format("%s %s, ", DbFieldNames.STATION_ID,
			DbFieldParameters.ROUTES_AND_STATIONS_STATION_ID));
		queryBuilder.append(String.format("%s %s", DbFieldNames.TIME_SHIFT,
			DbFieldParameters.ROUTES_AND_STATIONS_TIME_SHIFT));
		queryBuilder.append(")");

		return queryBuilder.toString();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldDbVersion, int newDbVersion) {
		throw new DatabaseException(String.format(
			"%s database is currently not intended to be upgraded", DATABASE_NAME));
	}
}
